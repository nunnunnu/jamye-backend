package org.jy.jamye.common.listener

import org.jy.jamye.domain.service.UserService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class PostUpdateEventListener(
    private val userService: UserService
) {
    @Async
    @EventListener
    fun notifyOnPostUpdate(data: NotifyPostUpdateEvent) {
        println("???")
        userService.notifyOnPostUpdate(data.userSeqs, data.groupSeq, data.postSeq, data.groupName, data.postTitle)
    }
}

data class NotifyPostUpdateEvent(
    val userSeqs: Set<Long>,
    val groupSeq: Long,
    val groupName: String,
    val postSeq: Long,
    val postTitle: String
) {
}