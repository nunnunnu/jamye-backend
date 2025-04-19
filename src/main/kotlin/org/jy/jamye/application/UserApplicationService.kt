package org.jy.jamye.application

import org.jy.jamye.application.dto.NotifyDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.domain.group.service.GroupService
import org.jy.jamye.domain.user.service.UserService
import org.jy.jamye.infra.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserApplicationService(
    private val userService: UserService,
    private val groupService: GroupService,
    private val userRepository: UserRepository,
    private val redisClient: RedisClient,
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

    fun deleteNotify(userId: String, notifySeq: Long) {
        val user = userService.getUser(userId)
        userService.deleteNotify(user.sequence!!, notifySeq)
    }

    fun logout(userId: String, accessToken: String, refreshToken: String) {
        redisClient.setBlackList(accessToken)
        redisClient.deleteRefreshToken(refreshToken, userId)
    }
}
