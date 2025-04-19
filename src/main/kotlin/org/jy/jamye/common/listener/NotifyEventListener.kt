package org.jy.jamye.common.listener

import org.jy.jamye.domain.user.service.DiscordService
import org.jy.jamye.domain.post.service.PostService
import org.jy.jamye.domain.user.service.UserService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NotifyEventListener(
    private val userService: UserService,
    private val discordService: DiscordService,
    private val postService: PostService
) {
    @Async
    @EventListener
    fun notifySend(data: NotifyInfo) {
        userService.notifySend(data.userSeqs, data.groupSeq, data.postSeq, data.message)
        val findDiscordConnectUser = userService.findDiscordConnectUser(data.userSeqs)
        if(findDiscordConnectUser.isNotEmpty()) {
            val postType = if (data.groupSeq != null && data.postSeq != null) postService.getPostTitle(groupSeq = data.groupSeq, postSeq = data.postSeq) else null
            findDiscordConnectUser.forEach { channelId -> discordService.sendDiscordDm(channelId, message = data.message, data.groupSeq, data.postSeq, postType?.type) }
        }
    }
}

data class NotifyInfo(
    val userSeqs: Set<Long>,
    val groupSeq: Long? = null,
    val postSeq: Long? = null,
    var message: String
) {
}