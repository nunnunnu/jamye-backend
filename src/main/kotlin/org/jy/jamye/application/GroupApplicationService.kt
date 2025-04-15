package org.jy.jamye.application

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.*
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.exception.AlreadyDeleteVoting
import org.jy.jamye.common.exception.GroupDeletionPermissionException
import org.jy.jamye.common.exception.InvalidInviteCodeException
import org.jy.jamye.common.listener.NotifyInfo
import org.jy.jamye.domain.service.*
import org.jy.jamye.infra.GroupRepository
import org.jy.jamye.infra.GroupUserRepository
import org.jy.jamye.ui.post.GroupPostDto
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime.now

@Suppress("CAST_NEVER_SUCCEEDS")
@Service
class GroupApplicationService(
    private val userService: UserService,
    private val groupService: GroupService,
    private val groupUserRepository: GroupUserRepository,
    private val redisClient: RedisClient,
    private val groupVoteService: GroupVoteService,
    private val fileService: VisionService,
    private val publisher: ApplicationEventPublisher,
    private val postService: PostService,
    private val groupRepository: GroupRepository,
) {
    private val log = LoggerFactory.getLogger(GroupApplicationService::class.java)
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

    @Transactional
    fun deleteGroupWithResult(id: String, groupSeq: Long): Boolean {
        val user = userService.getUser(id)
        if (!groupService.userIsMaster(user.sequence!!, groupSeq)) {
            throw GroupDeletionPermissionException()
        }
        val totalUser: Set<Long> = groupService.getUserSeqsInGroup(groupSeq)
        if (totalUser.count() <= 2) { //과반수 동의로 간주함
            //todo: 마스터 양도 로직 추가
            groupService.deleteGroup(groupSeq)
            return true
        }
        val deleteVoteMap = redisClient.getDeleteVoteMap()

        if (deleteVoteMap.containsKey(groupSeq)) {
            throw AlreadyDeleteVoting()
        } else {
            val voteAbleUser =
                groupUserRepository.findByGroupSequenceAndCreateDateGreaterThan(groupSeq, now().minusDays(7)).map { it.userSequence }

            val endDateTime = now().plusDays(7)
            val deleteVote = DeleteVote(
                startDateTime = now().toString(),
                endDateTime = endDateTime.toString(),
                standardVoteCount = voteAbleUser.size,
                agreeUserSeqs = mutableSetOf(user.sequence),
                disagreeUserSeqs = mutableSetOf(),
                hasRevoted = redisClient.reVoteCheckAndDeleteReVoteInfo("waitingReVote-${groupSeq}"),
            )
            deleteVoteMap[groupSeq] = deleteVote
            voteAbleUser.forEach { groupService.getDeleteVoteMapInMyGroup(it) }
            try {
                groupVoteService.scheduleVoteEndJob(groupSeq, endDateTime = endDateTime)
            } catch (e: Exception) {
               e.printStackTrace()
            }
        }
        redisClient.setValueObject("deleteVotes", deleteVoteMap)
        val group = groupService.getGroupSimpleInfo(groupSeq)
        val event = NotifyInfo(message = group.name + "그룹의 그룹 삭제 투표가 실시되었습니다. 과반수 동의 시 자동 삭제됩니다.", groupSeq = groupSeq, userSeqs = totalUser)
        publisher.publishEvent(event)
        return false
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

    fun getUsersInGroup(groupSeq: Long, userId: String): List<UserInGroupDto> {
        val user = userService.getUser(userId)
        return groupService.getUsersInGroup(groupSeq, user.sequence!!)
    }

    fun getUserInGroup(groupSeq: Long, userId: String): UserInGroupDto {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(groupSeq = groupSeq, userSeq = user.sequence!!)

        return groupService.groupUserInfoOrThrow(groupSequence = groupSeq, userSequence = user.sequence)

    }

    fun updateUserInGroupInfo(
        groupSeq: Long,
        userInGroupSeq: Long,
        nickName: String?,
        profile: MultipartFile?,
        userId: String
    ) {
        val user = userService.getUser(userId)
        val saveFile = profile?.let { fileService.saveFile(it) }
        nickName?.let { groupService.nickNameDuplicateCheck(groupSeq, nickName) }
        groupService.updateUserInGroupInfo(groupSeq, userInGroupSeq, user.sequence!!, nickName, saveFile)
    }

    fun leaveGroup(groupSeq: Long, userId: String) {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(groupSeq = groupSeq, userSeq = user.sequence!!)

        groupService.leaveGroup(groupSeq, user.sequence)


    }

    fun isGroupDeletionVoteInProgress(groupSeq: Long, userId: String): DeleteVote {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(groupSeq = groupSeq, userSeq = user.sequence!!)

        val deleteVoteMap = redisClient.getDeleteVoteMap()
        val deleteVoteInfo = deleteVoteMap.getOrDefault(groupSeq, DeleteVote())
        deleteVoteInfo.isWaitingDeleteReVoted = redisClient.reVoteCheck("waitingReVote-${groupSeq}")
        return deleteVoteInfo
    }

    fun updateGroupInfo(userId: String, groupSeq: Long, data: GroupPostDto.Update, imageUri: String?): GroupDto {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSeq)
        return groupService.updateGroupInfo(groupSeq, data, imageUri)
    }

    fun isDeletionVoteInProgress(groupSeq: Long, userId: String): DeleteVote.VoteDto {
        val user = userService.getUser(userId)
        val voteDto = DeleteVote.VoteDto(
            isNowVoting = groupService.isDeletionVoteInProgress(groupSeq),
            hasUserInDeletionVote = groupService.hasParticipatedInDeletionVote(
                groupSeq = groupSeq,
                userSequence = user.sequence!!
            )
        )
        return voteDto
    }

    fun getAllPostCountInGroup(groupSeq: Long, userId: String): PostDto.Count {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSeq)
        return postService.postCountInGroup(groupSeq, user.sequence)
    }

    fun getDeleteVoteInMyGroup(userId: String): Map<Long, DeleteVote> {
        val user = userService.getUser(userId)
        val filterMap = groupService.getDeleteVoteMapInMyGroup(user.sequence!!)
        val groupSeqs = filterMap.keys
        val groupNameMap = groupRepository.findAllById(groupSeqs).associate { it.sequence to it.name }
        filterMap.entries.forEach { (groupSeq, voteInfo) -> voteInfo.groupName = groupNameMap[groupSeq] }
        return filterMap
    }

}