package org.jy.jamye.application

import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupApplicationService(private val userService: UserService, private val groupService: GroupService) {
    fun getGroupsInUser(id: String): List<GroupDto> {
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
        return groupService.inviteCodePublish(user.sequence!!, groupSeq)
    }

    fun inviteGroupUser(userId: String, data: GroupPostDto.Invite): Long {
        val user = userService.getUser(userId)
        return groupService.inviteUser(user.sequence!!, data)
    }
}