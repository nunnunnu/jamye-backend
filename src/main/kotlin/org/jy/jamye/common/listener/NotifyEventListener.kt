package org.jy.jamye.common.listener

import org.jy.jamye.domain.service.UserService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NotifyEventListener(
    private val userService: UserService
) {
    @Async
    @EventListener
    fun notifySend(data: NotifyInfo) {
        userService.notifySend(data.userSeqs, data.groupSeq, data.postSeq, data.message)
    }
}

data class NotifyInfo(
    val userSeqs: Set<Long>,
    val groupSeq: Long? = null,
    val postSeq: Long? = null,
    val message: String
) {
}