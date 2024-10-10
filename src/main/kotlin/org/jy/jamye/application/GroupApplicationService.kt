package org.jy.jamye.application

import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupApplicationService(private val userService: UserService, private val groupService: GroupService) {
    @Transactional(readOnly = true)
    fun getGroupInUser(id: String): List<GroupDto> {
        val user = userService.getUser(id)
        return groupService.getGroupInUser(user.sequence!!)
    }

    @Transactional
    fun createGroup(id: String, data: GroupDto, masterUserInfo: UserInGroupDto.Simple): GroupDto.Detail {
        val user = userService.getUser(id)
        return groupService.createGroup(user.sequence!!, data, masterUserInfo);
    }
}