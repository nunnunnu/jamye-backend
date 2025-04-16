package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.DeleteVote
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.exception.AlreadyJoinedGroupException
import org.jy.jamye.common.exception.DuplicateGroupNicknameException
import org.jy.jamye.common.exception.MemberNotInGroupException
import org.jy.jamye.common.listener.PostDeleteEvent
import org.jy.jamye.domain.model.Grade
import org.jy.jamye.domain.model.Group
import org.jy.jamye.domain.model.GroupUser
import org.jy.jamye.infra.GroupFactory
import org.jy.jamye.infra.GroupReader
import org.jy.jamye.infra.GroupRepository
import org.jy.jamye.infra.GroupUserRepository
import org.jy.jamye.ui.post.GroupPostDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class GroupService(
    private val groupRepo: GroupRepository,
    private val groupUserRepo: GroupUserRepository,
    private val groupFactory: GroupFactory,
    private val publisher: ApplicationEventPublisher,
    private val groupUserRepository: GroupUserRepository,
    private val redisClient: RedisClient,
    private val groupReader: GroupReader,
    private val messagingTemplate: SimpMessagingTemplate
) {

    var log: Logger = LoggerFactory.getLogger(GroupService::class.java)
    @Transactional(readOnly = true)
    fun getGroupInUser(userSequence: Long): List<GroupDto.UserInfo> {
        val groupConnection = groupUserRepo.findAllByUserSequence(userSequence)
        val totalUserCount = groupUserRepo.countGroupInUser(groupConnection.map { it.groupSequence }).associate { group -> group.groupSeq to group.totalUser }
        return groupConnection.map {
            val group = it.group
            GroupDto.UserInfo(
                groupSequence = group.sequence, name = group.name, description = group.description, createDate = group.createDate, updateDate = group.updateDate, userNickName = it.nickname, imageUrl = group.imageUrl,
                totalUsers = totalUserCount.getOrDefault(group.sequence, 0)
            )
        }
    }

    fun getAllMyGroupSeqs(userSeq: Long): Set<Long> {
        return groupUserRepo.findAllByUserSequence(userSeq).map { it.groupSequence }.toSet()
    }

    @Transactional
    fun createGroup(userSeq: Long, data: GroupDto, masterUserInfo: UserInGroupDto.Simple): GroupDto.Detail {
        val group = groupFactory.createGroup(userSeq, data)
        groupRepo.save(group)
        val groupMaster =
            groupFactory.createGroupMasterConnection(group.sequence!!, userSeq, masterUserInfo, group)
        groupUserRepo.save(groupMaster)
        return GroupDto.Detail(groupSequence = group.sequence!!, name = group.name, description = group.description,
            imageUrl = group.imageUrl, createDate = group.createDate, updateDate = group.updateDate,
            users = listOf(UserInGroupDto(
                nickname = groupMaster.nickname,
                imageUrl = groupMaster.imageUrl,
                createDate = groupMaster.createDate,
                updateDate = groupMaster.updateDate,
                grade = groupMaster.grade,
                groupUserSequence = groupMaster.groupUserSequence!!,
                userSequence = groupMaster.userSequence,
                groupSequence = group.sequence!!))
        )
    }

    @Transactional(readOnly = true)
    fun getGroup(userSequence: Long, groupSequence: Long): GroupDto.Detail {
        val usersInGroup = groupUserRepo.findAllByGroupSequence(groupSequence)
        val filter = usersInGroup.filter { it.userSequence == userSequence }

        if(filter.isEmpty()) throw MemberNotInGroupException()

        val masterInfo = usersInGroup.first { it.grade == Grade.MASTER }

        val group = groupReader.findByIdOrThrow(groupSequence)

        return GroupDto.Detail(
            groupSequence = group.sequence!!,
            name = group.name,
            description = group.description,
            imageUrl = group.imageUrl,
            createDate = group.createDate,
            updateDate = group.updateDate,
            isMaster = masterInfo.userSequence == userSequence,
            users = usersInGroup.map {
                UserInGroupDto(
                userSequence = it.userSequence,
                groupSequence = it.groupSequence,
                groupUserSequence = it.groupUserSequence!!,
                nickname = it.nickname,
                imageUrl = it.imageUrl,
                grade = it.grade,
                createDate = it.createDate,
                updateDate = it.updateDate
            ) })
    }

    @Transactional(readOnly = true)
    fun inviteCodePublish(userSequence: Long, groupSequence: Long): String {
        userInGroupCheckOrThrow(userSequence, groupSequence)
        return "INVITE_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + UUID.randomUUID()
    }

    fun userInGroupCheckOrThrow(userSeq: Long, groupSeq: Long) {
        groupReader.userInGroupCheckOrThrow(userSeq, groupSeq)
    }

    fun usersInGroupCheckOrThrow(usersSeqInGroup: Set<Long>, groupSequence: Long) {
        val userInGroupCount = groupUserRepo.countByUserSequenceInAndGroupSequence(usersSeqInGroup, groupSequence)
        if (userInGroupCount != usersSeqInGroup.size) {
            throw MemberNotInGroupException()
        }
    }

    @Transactional
    fun inviteUser(userSequence: Long, data: GroupPostDto.Invite): Long {
        val groupNormalUser = groupFactory.createGroupNormalUser(
            userSequence,
            getGroupOrThrow(data.groupSequence),
            data.nickName,
            data.profileImageUrl
        )
        groupUserRepo.save(groupNormalUser)
        return groupNormalUser.groupUserSequence!!
    }

    private fun getGroupOrThrow(groupSequence: Long): Group {
        return groupRepo.findById(groupSequence).orElseThrow { throw EntityNotFoundException() }
    }

    @Transactional(readOnly = true)
    fun userIsMaster(userSequence: Long, groupSequence: Long): Boolean {
        return groupUserRepo.existsByUserSequenceAndGroupSequenceAndGrade(userSequence, groupSequence, Grade.MASTER)
    }

    fun groupUserInfo(groupSequence: Long, userSequence: Long): UserInGroupDto? {
        return groupUserRepo.findByGroupSequenceAndUserSequence(groupSequence, userSequence)
            .orElse(null)?.let { userInfo ->
                UserInGroupDto(
                    userSequence = userInfo.userSequence,
                    groupSequence = userInfo.groupSequence,
                    groupUserSequence = userInfo.groupUserSequence!!,
                    nickname = userInfo.nickname,
                    imageUrl = userInfo.imageUrl,
                    grade = userInfo.grade,
                    createDate = userInfo.createDate,
                    updateDate = userInfo.updateDate
                )
            }
    }

    fun groupUserInfoOrThrow(groupSequence: Long, userSequence: Long): UserInGroupDto {
        val userInfo = groupUserRepo.findByGroupSequenceAndUserSequence(groupSequence, userSequence)
            .orElseThrow { EntityNotFoundException("그룹 내 존재하는 유저가 아닙니다") }
        return UserInGroupDto(
                    userSequence = userInfo.userSequence,
                    groupSequence = userInfo.groupSequence,
                    groupUserSequence = userInfo.groupUserSequence!!,
                    nickname = userInfo.nickname,
                    imageUrl = userInfo.imageUrl,
                    grade = userInfo.grade,
                    createDate = userInfo.createDate,
                    updateDate = userInfo.updateDate
                )

    }

    fun getGroupInUsersNickName(groupSeq: Long, userSeqs: List<Long>): Map<Long, String> {
        val groupUsers =
            groupUserRepo.findByGroupSequenceAndUserSequenceIn(groupSeq, userSeqs)
        return groupUsers.associate { user -> user.userSequence to user.nickname }

    }

    fun autoTransferMasterPrivileges(userSeq: Long, deleteAgree: Set<Long> = setOf(), groupSeqs: MutableSet<Long> = mutableSetOf()) {
        val masterInfo = mutableListOf<GroupUser>()
        if(groupSeqs.isEmpty()) {
            masterInfo.addAll(groupUserRepo.findAllByUserSequenceAndGrade(userSeq, Grade.MASTER))
            groupSeqs.addAll(masterInfo.map { it.groupSequence })
        } else {
            masterInfo.addAll(groupUserRepo.findAllByUserSequenceAndGradeAndGroupSequenceIn(userSeq, Grade.MASTER, groupSeqs))
        }

        if (masterInfo.isEmpty()) { //해당 유저가 운영자인 그룹이 없음
            return
        }
        groupUserRepo.deleteAllById(masterInfo.map { it.groupUserSequence })

        val groupOldestUser = groupUserRepo.findByGroupOldestUser(groupSeqs, deleteAgree)
        if (groupOldestUser.isNotEmpty()) {
            groupUserRepo.assignMasterToOldestUser(groupOldestUser.map { it.groupUserSequence!! })
            groupUserRepo.flush()
        }
    }

    fun deleteGroup(groupSeq: Long) {
        log.info("---{}번 그룹 삭제 유저 과반수 동의---", groupSeq)
        groupUserRepo.deleteAllByGroupSequence(groupSeq)
        groupRepo.deleteById(groupSeq)
        val event = PostDeleteEvent(groupSeq)
        publisher.publishEvent(event)
    }

    fun deleteUsers(groupSeq: Long, deleteAgree: Set<Long>) {
        val masterSeq = groupUserRepo.findGroupMasterSeq(groupSeq, deleteAgree)
        autoTransferMasterPrivileges(masterSeq, deleteAgree, groupSeqs = mutableSetOf(groupSeq))
        groupUserRepo.deleteAllByGroupSequenceAndUserSequenceIn(groupSeq, deleteAgree)
    }

    fun getInviteGroupInfo(userSeq: Long, groupSeq: Long) : GroupDto {
        val usersInGroup = groupUserRepo.findAllByGroupSequence(groupSeq)
        val filter = usersInGroup.filter { it.userSequence == userSeq }
        if(filter.isNotEmpty()) {
            throw AlreadyJoinedGroupException()
        }
        val group = groupRepo.findById(groupSeq).orElseThrow{ throw EntityNotFoundException() }
        return GroupDto(name = group.name, description = group.description, imageUrl = group.imageUrl, groupSequence = group.sequence)

    }

    fun getUsersInGroup(groupSeq: Long, userSeq: Long): List<UserInGroupDto> {
        userInGroupCheckOrThrow(groupSeq = groupSeq, userSeq = userSeq)
        val groupConnection = groupUserRepo.findAllByGroupSequence(groupSeq)
        return groupConnection.map { it -> UserInGroupDto(
            userSequence = it.userSequence,
            groupSequence = it.groupSequence,
            groupUserSequence = it.groupUserSequence!!,
            nickname = it.nickname,
            imageUrl = it.imageUrl,
            grade = it.grade,
            createDate = it.createDate,
            updateDate = it.updateDate
        ) }
    }

    fun userInfoInGroup(groupUserSeqs: Set<Long>): Map<Long, UserInGroupDto.Simple>  {
        return groupUserRepo.findAllById(groupUserSeqs)
            .associate { it.groupUserSequence!! to UserInGroupDto.Simple(nickname = it.nickname, imageUrl = it.imageUrl) }
            .toMap()
    }

    fun updateUserInGroupInfo(groupSeq: Long, userInGroupSeq: Long, userSeq: Long, nickName: String?, saveFile: String?) {
        userInGroupCheckOrThrow(groupSeq = groupSeq, userSeq = userSeq)

        val userInGroup =
            groupUserRepo.findById(userInGroupSeq).orElseThrow { EntityNotFoundException("유저 정보를 찾을 수 없습니다") }

        userInGroup.updateInfo(nickName, saveFile)

        groupUserRepo.save(userInGroup)
    }

    @Transactional
    @CacheEvict(cacheNames = ["groupExistCache"], key = "#groupSeq+#userSeq")
    fun leaveGroup(groupSeq: Long, userSeq: Long) {
        val countByGroupSequence = groupUserRepo.countByGroupSequence(groupSeq)
        if(countByGroupSequence == 1L) {
            deleteGroup(groupSeq)
        } else {
            autoTransferMasterPrivileges(userSeq = userSeq, groupSeqs = mutableSetOf(groupSeq))
        }
        groupUserRepo.deleteAllByGroupSequenceAndUserSequence(groupSeq, userSeq)
    }

    fun nickNameDuplicateCheck(groupSeq: Long, nickName: String) {
        if (groupUserRepository.existsByGroupSequenceAndNickname(groupSeq, nickName)) {
            throw DuplicateGroupNicknameException()
        }
    }

    @Transactional
    @CacheEvict(value = ["groupCache"])
    fun updateGroupInfo(groupSeq: Long, data: GroupPostDto.Update, imageUri: String?): GroupDto {
        val group = groupReader.findByIdOrThrow(groupSeq)
        group.updateInfo(data.name, imageUri, data.description)
        return GroupDto(
            name = group.name,
            imageUrl = group.imageUrl,
            description = group.description,
            createDate = group.createDate,
            updateDate = group.updateDate,
            groupSequence = group.sequence
        )
    }

    fun getGroupSimpleInfo(groupSeq: Long): GroupDto {
        val group = getGroupOrThrow(groupSeq)
        return GroupDto(
            name = group.name,
            imageUrl = group.imageUrl,
            description = group.description,
            createDate = group.createDate,
            updateDate = group.updateDate,
            groupSequence = group.sequence
        )
    }

    fun getUserSeqsInGroup(groupSeq: Long): Set<Long> {
        val totalUser: Set<Long> = groupUserRepository.findAllByGroupSequence(groupSeq).map { it.userSequence }.toSet()
        return totalUser
    }

    fun isDeletionVoteInProgress(groupSeq: Long): DeleteVote {
        return redisClient.getDeleteVoteMap()[groupSeq]?: throw IllegalArgumentException()
    }

    fun hasParticipatedInDeletionVote(groupSeq: Long, userSequence: Long): Boolean {
        val group = redisClient.getDeleteVoteMap()[groupSeq] ?: return false
        return group.agreeUserSeqs.contains(userSequence) || group.disagreeUserSeqs.contains(userSequence)
    }

    fun getDeleteVoteMapInMyGroup(userSeq: Long): Map<Long, DeleteVote> {
        val allMyGroupSeqs = getAllMyGroupSeqs(userSeq)
        val deleteVoteMap = redisClient.getDeleteVoteMap()
        val filterMap = deleteVoteMap.filter { allMyGroupSeqs.contains(it.key) }
        log.info("[그룹 삭제 투표]socket 알람 전송]")
        messagingTemplate.convertAndSend(
            "/alarm/group/delete/$userSeq",
            filterMap
        )
        return filterMap
    }
}

