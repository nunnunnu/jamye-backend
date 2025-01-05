package org.jy.jamye.common.listener

import org.jy.jamye.domain.service.EmailService
import org.jy.jamye.domain.service.PostService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class PostDeleteEventListener(
    private val postService: PostService,
) {
    @Async
    @EventListener
    fun deleteGroupPost(data: PostDeleteEvent) {
        postService.deletePostInGroup(data.groupSeq)
    }
}

data class PostDeleteEvent(
    val groupSeq: Long) {
}