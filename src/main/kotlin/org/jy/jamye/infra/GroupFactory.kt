package org.jy.jamye.infra

import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.domain.model.Grade
import org.jy.jamye.domain.model.Group
import org.jy.jamye.domain.model.GroupUser
import org.springframework.stereotype.Service

@Service
class GroupFactory {
    fun createGroup(userSequence: Long, data: GroupDto): Group {
        return Group(name = data.name, description = data.description, imageUrl = data.imageUrl)
    }

    fun createGroupMasterConnection(
        groupSequence: Long,
        userSequence: Long,
        masterUserInfo: UserInGroupDto.Simple,
        group: Group
    ): GroupUser {
        return GroupUser(
            userSequence = userSequence,
            groupSequence = groupSequence,
            nickname = masterUserInfo.nickname,
            imageUrl = masterUserInfo.imageUrl,
            grade = Grade.MASTER,
            group = group)
    }
}