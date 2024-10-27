package org.jy.jamye.application

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.DeleteVote
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.exception.GroupDeletionPermissionException
import org.jy.jamye.common.exception.InvalidInviteCodeException
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.GroupVoteService
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.infra.GroupUserRepository
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime.now

@Suppress("CAST_NEVER_SUCCEEDS")
@Service
class GroupApplicationService(private val userService: UserService,
                              private val groupService: GroupService,
                              private val groupUserRepository: GroupUserRepository,
                              private val redisClient: RedisClient,
    private val groupVoteService: GroupVoteService
) {
    fun getGroupsInUser(id: String): List<GroupDto.UserInfo> {
        val user = userService.getUser(id)
        return groupService.getGroupInUser(user.sequence!!)
    }

    fun createGroup(id: String, data: GroupDto, masterUserInfo: UserInGroupDto.Simple): GroupDto.Detail {
        val user = userService.getUser(id)
        return groupService.createGroup(user.sequence!!, data, masterUserInfo);
    }

    fun getGroup(userId: String, groupSeq: Long): GroupDto.Detail {
        val user = userService.getUser(userId)
        return groupService.getGroup(user.sequence!!, groupSeq)
    }

    fun inviteGroupCode(userId: String, groupSeq: Long) : String{
        val user = userService.getUser(userId)
        val groupInviteCode = groupService.inviteCodePublish(user.sequence!!, groupSeq)
        redisClient.setValueAndExpireTimeMinutes(groupInviteCode, groupSeq.toString(), 60)
        return groupInviteCode
    }

    fun inviteGroupUser(userId: String, data: GroupPostDto.Invite): Long {
        val groupSeq = redisClient.getAndDelete(data.inviteCode)
        if (groupSeq == null || groupSeq != data.groupSequence.toString()) {
            throw InvalidInviteCodeException()
        }
        val user = userService.getUser(userId)
        return groupService.inviteUser(user.sequence!!, data)
    }

    fun deleteGroup(id: String, groupSeq: Long) {
        val user = userService.getUser(id)
        if (!groupService.userIsMaster(user.sequence!!, groupSeq)) {
            throw GroupDeletionPermissionException()
        }
        val deleteVoteMap = redisClient.getDeleteVoteMap()

        if (deleteVoteMap.containsKey(groupSeq)) {
            throw InvalidInviteCodeException("이미 투표진행중입니다")
        } else {
            val voteAbleNumber =
                groupUserRepository.countByGroupSequenceAndCreateDateGreaterThan(groupSeq, now().minusDays(7))

            val endDateTime = now().plusMinutes(1)
            val deleteVote = DeleteVote(
                startDateTime = now().toString(),
                endDateTime = endDateTime.toString(),
                standardVoteCount = voteAbleNumber,
                agreeUserSeqs = mutableSetOf(user.sequence),
                disagreeUserSeqs = mutableSetOf(),
                hasRevoted = redisClient.reVoteCheck("waitingReVote-${groupSeq}")
            )
            deleteVoteMap[groupSeq] = deleteVote
            groupVoteService.scheduleVoteEndJob(groupSeq, endDateTime = endDateTime)
        }
        redisClient.setValueObject("deleteVotes", deleteVoteMap)

    }

    fun deleteGroupVote(userId: String, type: String, groupSeq: Long) {
        val user = userService.getUser(userId)
        val deleteVoteMap = redisClient.getDeleteVoteMap()
        val deleteVote = deleteVoteMap[groupSeq]
        deleteVote?.let {
            val userSeq = user.sequence!!

            if(it.agreeUserSeqs.contains(userSeq) || it.disagreeUserSeqs.contains(userSeq)) {
                throw IllegalArgumentException("이미 투표에 참여하셨습니다.")
            }
            if(type == "agree") it.agreeUserSeqs.add(userSeq)
            else it.disagreeUserSeqs.add(userSeq)
        }
        redisClient.setValueObject("deleteVotes", deleteVoteMap)
    }

    fun getInviteGroup(userId: String, inviteCode: String): GroupDto {
        val user = userService.getUser(userId)
        val groupSeq = redisClient.getValue(inviteCode) ?: throw EntityNotFoundException("그룹 정보를 찾을 수 없습니다.")
        return groupService.getInviteGroupInfo(user.sequence!!, groupSeq.toLong())

    }
}