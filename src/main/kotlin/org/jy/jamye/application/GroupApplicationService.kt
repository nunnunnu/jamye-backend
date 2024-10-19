package org.jy.jamye.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jy.jamye.application.dto.DeleteVote
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.exception.GroupDeletionPermissionException
import org.jy.jamye.common.exception.InvalidInviteCodeException
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.infra.GroupUserRepository
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@Service
class GroupApplicationService(private val userService: UserService, private val groupService: GroupService, private val groupUserRepository: GroupUserRepository, private val redisClient: RedisClient) {
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

    fun deleteGroup(id: String, groupSeq: Long): Long {
        val user = userService.getUser(id)
        if (!groupService.userIsMaster(user.sequence!!, groupSeq)) {
            throw GroupDeletionPermissionException()
        }
        val mapper = ObjectMapper()

        val deleteVoteMap: MutableMap<Long, DeleteVote> = if (redisClient.getValue("deleteVotes").isNullOrBlank()) HashMap()
            else mapper.readValue(redisClient.getValue("deleteVotes"), object : TypeReference<MutableMap<Long, DeleteVote>>() {})

        if (deleteVoteMap.containsKey(groupSeq)) {
            throw InvalidInviteCodeException("이미 투표진행중입니다")
        } else {
            val voteAbleNumber =
                groupUserRepository.countByGroupSequenceAndCreateDateGreaterThan(groupSeq, now().minusDays(7))

            deleteVoteMap.put(groupSeq, DeleteVote(startDate = now().toString(), standardVoteCount = voteAbleNumber, agreeUserSeqs = setOf(user.sequence), disagreeUserSeqs = setOf(), hasRevoted = false))
        }
        val jsonString = mapper.writeValueAsString(deleteVoteMap)
        redisClient.setValue("deleteVotes", jsonString)
        return user.sequence
    }
}