package org.jy.jamye.application

import org.jy.jamye.application.dto.NotifyDto
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.security.TokenDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserApplicationService(
    private val userService: UserService,
    private val groupService: GroupService,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun deleteUser(username: String, password: String) {
        val userSeq = userService.deleteUser(username, password)
        groupService.autoTransferMasterPrivileges(userSeq, setOf())
    }

    fun duplicateIdCheck(id: String): Boolean {
        return !userRepository.existsByUserId(id)
    }

    fun duplicateEmailCheck(email: String): Boolean {
        return !userRepository.existsByEmail(email)
    }

    fun getNotifyList(userId: String): List<NotifyDto> {
        val user = userService.getUser(userId)
        val notifyList = userService.getNotifyList(user.sequence!!)
        return notifyList
    }

    fun getNotifyNoReadCount(userId: String): Long{
        val user = userService.getUser(userId)
        return userService.getNotifyNoReadCount(user.sequence!!)
    }

    fun allNotifyRead(userId: String) {
        val user = userService.getUser(userId)
        userService.allNotifyRead(user.sequence!!)
    }

    fun deleteReadNotify(userId: String) {
        val user = userService.getUser(userId)
        userService.deleteNotify(user.sequence!!)


    }
}
