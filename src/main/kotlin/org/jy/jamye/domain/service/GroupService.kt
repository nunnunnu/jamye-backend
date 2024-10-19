package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.exception.GroupDeletionPermissionException
import org.jy.jamye.common.exception.MemberNotInGroupException
import org.jy.jamye.domain.model.Grade
import org.jy.jamye.domain.model.Group
import org.jy.jamye.infra.GroupFactory
import org.jy.jamye.infra.GroupRepository
import org.jy.jamye.infra.GroupUserRepository
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class GroupService(
    private val groupRepo: GroupRepository,
    private val groupUserRepo: GroupUserRepository,
    private val groupFactory: GroupFactory
) {
    @Transactional(readOnly = true)
    fun getGroupInUser(userSequence: Long): List<GroupDto.UserInfo> {
        val groupConnection = groupUserRepo.findAllByUserSequence(userSequence)
        return groupConnection.map {
            val group = it.group
            GroupDto.UserInfo(
                groupSequence = group.sequence, name = group.name, description = group.description, createDate = group.createDate, updateDate = group.updateDate, userNickName = it.nickname
            )
        }
    }

    @Transactional
    fun createGroup(userSequence: Long, data: GroupDto, masterUserInfo: UserInGroupDto.Simple): GroupDto.Detail {
        val group = groupFactory.createGroup(userSequence, data)
        groupRepo.save(group)
        val groupMaster =
            groupFactory.createGroupMasterConnection(group.sequence!!, userSequence, masterUserInfo, group)
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

        val group = groupRepo.findById(groupSequence).orElseThrow { throw EntityNotFoundException() }

        return GroupDto.Detail(
            groupSequence = group.sequence!!,
            name = group.name,
            description = group.description,
            imageUrl = group.imageUrl,
            createDate = group.createDate,
            updateDate = group.updateDate,
            users = usersInGroup.map { it -> UserInGroupDto(
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

    fun userInGroupCheckOrThrow(userSequence: Long, groupSequence: Long) {
        if (!groupUserRepo.existsByUserSequenceAndGroupSequence(userSequence, groupSequence)) {
            throw BadCredentialsException("Group user does not exist")
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

    fun getGroupInUsersNickName(groupSeq: Long, userSeqs: List<Long>): Map<Long, String> {
        val groupUsers =
            groupUserRepo.findByGroupSequenceAndUserSequenceIn(groupSeq, userSeqs)
        return groupUsers.associate { user -> user.userSequence to user.nickname }

    }

    fun autoTransferMasterPrivileges(userSeq: Long) {
        val masterInfo = groupUserRepo.findAllByUserSequenceAndGrade(userSeq, Grade.MASTER)
        val groupSeqs = masterInfo.map { it.groupSequence }
        groupUserRepo.deleteAllById(masterInfo.map { it.groupUserSequence })

        val groupOldestUser = groupUserRepo.findByGroupOldestUser(groupSeqs)
        if (groupOldestUser.isNotEmpty()) {
            groupUserRepo.assignMasterToOldestUser(groupOldestUser.map { it.groupUserSequence!! })
            groupUserRepo.flush()
        }
    }
}

