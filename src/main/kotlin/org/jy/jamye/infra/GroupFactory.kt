package org.jy.jamye.infra

import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.domain.model.Grade
import org.jy.jamye.domain.model.Group
import org.jy.jamye.domain.model.GroupUser
import org.springframework.stereotype.Service

@Service
class GroupFactory(
    private val groupUserRepository: GroupUserRepository
) {
    fun createGroup(userSequence: Long, data: GroupDto): Group {
        return Group(name = data.name, description = data.description, imageUrl = data.imageUrl)
    }

    fun createGroupMasterConnection(
        groupSequence: Long,
        userSequence: Long,
        masterUserInfo: UserInGroupDto.Simple,
        group: Group
    ): GroupUser {
        if(groupUserRepository.existsByUserSequenceAndGroupSequence(userSequence, groupSequence))
            throw IllegalArgumentException("Group user already exists")

        return GroupUser(
            userSequence = userSequence,
            groupSequence = groupSequence,
            nickname = masterUserInfo.nickname,
            imageUrl = masterUserInfo.imageUrl,
            grade = Grade.MASTER,
            group = group)
    }

    fun createGroupNormalUser(userSequence: Long, group: Group, nickName: String, profileImageUrl: String?)
    : GroupUser {
        val groupSequence = group.sequence!!
        if(groupUserRepository.existsByGroupSequenceAndNickname(groupSequence, nickName)) {
            throw IllegalArgumentException("Group nickname already exists")
        }
        if(groupUserRepository.existsByUserSequenceAndGroupSequence(userSequence, groupSequence)) {
            throw IllegalArgumentException("Group user already exists")
        }

        return GroupUser(
            userSequence = userSequence,
            groupSequence = groupSequence,
            nickname = nickName,
            imageUrl = profileImageUrl,
            grade = Grade.NORMAL,
            group = group)
    }
}