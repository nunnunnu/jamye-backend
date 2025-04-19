package org.jy.jamye.application.group

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.post.dto.PostDto
import org.jy.jamye.application.user.dto.UserInGroupDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.exception.AlreadyDeleteVoting
import org.jy.jamye.common.exception.GroupDeletionPermissionException
import org.jy.jamye.common.exception.InvalidInviteCodeException
import org.jy.jamye.common.listener.NotifyInfo
import org.jy.jamye.domain.group.service.GroupService
import org.jy.jamye.domain.group.service.GroupVoteService
import org.jy.jamye.domain.post.service.PostService
import org.jy.jamye.domain.post.service.VisionService
import org.jy.jamye.domain.user.service.UserService
import org.jy.jamye.infra.group.GroupRepository
import org.jy.jamye.infra.user.GroupUserRepository
import org.jy.jamye.ui.post.GroupPostDto
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime.now

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

    fun getGroupSimple(userId: String, groupSeq: Long): GroupDto {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSeq)
        return groupService.getGroupSimpleInfo(groupSeq)
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
        val userSeqsInGroup = groupService.getUserSeqsInGroup(groupSeq)
        userSeqsInGroup.forEach { groupService.getDeleteVoteMapInMyGroup(it) }
        deleteVoteMap.remove(groupSeq)
        return false
    }

    fun deleteGroupVote(userId: String, type: DeleteVote.VoteType, groupSeq: Long) {
        val user = userService.getUser(userId)
        val deleteVoteMap = redisClient.getDeleteVoteMap()
        val deleteVote = deleteVoteMap[groupSeq] ?: throw IllegalArgumentException("그룹 삭제투표 진행중인 그룹이 아닙니다.")
        val userSeq = user.sequence!!

        if(deleteVote.alreadyVoteCheck(userSeq)) {
            throw IllegalArgumentException("이미 투표에 참여하셨습니다.")
        }
        deleteVote.addVote(type, userSeq)

        if (deleteVote.resultCheck()) {
            val group = groupService.getGroupSimpleInfo(groupSeq)
            val userSeqsInGroup = groupService.getUserSeqsInGroup(groupSeq)
            val message = "${group.name}의 삭제 투표가 완료되었습니다. 과반수 삭제 동의(${deleteVote.agreeUserSeqs.size}/${deleteVote.standardVoteCount}명)로 인해 그룹이 자동 삭제되었습니다."
            log.info("$groupSeq: 과반수 삭제 동의 ${deleteVote.agreeUserSeqs.size}/${deleteVote.standardVoteCount}명")
            groupService.deleteGroup(groupSeq)
            val event = NotifyInfo(groupSeq = groupSeq, userSeqs = userSeqsInGroup, message = message)
            publisher.publishEvent(event)
            userSeqsInGroup.forEach { groupService.getDeleteVoteMapInMyGroup(it) }
            deleteVoteMap.remove(groupSeq)
        }
        redisClient.setValueObject("deleteVotes", deleteVoteMap)
        groupService.getDeleteVoteMapInMyGroup(userSeq = user.sequence)
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

    fun isGroupDeletionVoteInProgress(groupSeq: Long, userId: String): DeleteVote.Detail {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(groupSeq = groupSeq, userSeq = user.sequence!!)

        val deleteVoteMap = redisClient.getDeleteVoteMap()
        val deleteVoteInfo = deleteVoteMap[groupSeq]
        val detail = DeleteVote.Detail(deleteVoteInfo, user.sequence)
        detail.isWaitingDeleteReVoted = redisClient.reVoteCheck("waitingReVote-${groupSeq}")
        return detail
    }

    fun updateGroupInfo(userId: String, groupSeq: Long, data: GroupPostDto.Update, imageUri: String?): GroupDto {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSeq)
        return groupService.updateGroupInfo(groupSeq, data, imageUri)
    }

    fun isDeletionVoteInProgress(groupSeq: Long, userId: String): DeleteVote.Detail {
        val user = userService.getUser(userId)
        val deleteVote = groupService.isDeletionVoteInProgress(groupSeq)
        val voteDto = deleteVote.let {
            DeleteVote.Detail(
                deleteVote = it,
                userSeq = user.sequence!!,
                hasUserInDeletionVote = groupService.hasParticipatedInDeletionVote(
                    groupSeq = groupSeq,
                    userSequence = user.sequence
                )
            )
        }
        return voteDto
    }

    fun getAllPostCountInGroup(groupSeq: Long, userId: String): PostDto.Count {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSeq)
        return postService.postCountInGroup(groupSeq, user.sequence)
    }

    fun getDeleteVoteInMyGroup(userId: String): Map<Long, DeleteVote.Detail> {
        val user = userService.getUser(userId)
        val filterMap = groupService.getDeleteVoteMapInMyGroup(user.sequence!!)
        val groupSeqs = filterMap.keys
        val groupNameMap = groupRepository.findAllById(groupSeqs).associate { it.sequence to it.name }
        val result = mutableMapOf<Long, DeleteVote.Detail>()
        filterMap.entries.forEach { (groupSeq, voteInfo) ->
            result[groupSeq] = DeleteVote.Detail(
                groupSeq = groupSeq,
                deleteVote = voteInfo,
                userSeq = user.sequence,
                alreadyVoteCheck = voteInfo.alreadyVoteCheck(userSeq = user.sequence),
                groupName = groupNameMap[groupSeq]
            )
        }
        return result
    }

}