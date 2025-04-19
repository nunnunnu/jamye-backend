package org.jy.jamye.infra.group

import org.jy.jamye.application.group.GroupDto
import org.jy.jamye.application.user.dto.UserInGroupDto
import org.jy.jamye.common.exception.AlreadyJoinedGroupException
import org.jy.jamye.common.exception.DuplicateGroupNicknameException
import org.jy.jamye.domain.user.model.Grade
import org.jy.jamye.domain.group.model.Group
import org.jy.jamye.domain.user.model.GroupUser
import org.jy.jamye.infra.user.GroupUserRepository
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
            throw AlreadyJoinedGroupException()

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
            throw DuplicateGroupNicknameException()
        }
        if(groupUserRepository.existsByUserSequenceAndGroupSequence(userSequence, groupSequence)) {
            throw AlreadyJoinedGroupException()
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