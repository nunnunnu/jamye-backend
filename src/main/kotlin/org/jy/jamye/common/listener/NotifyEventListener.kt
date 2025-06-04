package org.jy.jamye.common.listener

import org.jy.jamye.domain.user.service.DiscordService
import org.jy.jamye.domain.post.service.PostService
import org.jy.jamye.domain.user.service.FcmService
import org.jy.jamye.domain.user.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.lang.Thread.sleep

@Component
class NotifyEventListener(
    private val userService: UserService,
    private val discordService: DiscordService,
    private val postService: PostService,
    private val fcmService: FcmService
) {
    val log: Logger = LoggerFactory.getLogger(NotifyEventListener::class.java)

    @Async
    @EventListener
    fun notifySend(data: NotifyInfo) {
        log.info("[NotifyEventListener] 알람 전송 시작 ${data.userSeqs.size}개")
        val finalMessage = if(data.message == null) data.title else data.title + " - " + data.message
        log.info("[NotifyEventListener] 알람 전송 시작 - 기본 알람")
        userService.notifySend(data.userSeqs, data.groupSeq, data.postSeq, finalMessage)
        val findDiscordConnectUser = userService.findDiscordConnectUser(data.userSeqs)
        if(findDiscordConnectUser.isNotEmpty()) {
            log.info("[NotifyEventListener] 알람 전송 시작 - 디스코드 알람 ${findDiscordConnectUser.size}개")
            val postType = if (data.groupSeq != null && data.postSeq != null) postService.getPostTitle(
                groupSeq = data.groupSeq,
                postSeq = data.postSeq
            ) else null
            findDiscordConnectUser.forEach { channelId ->
                discordService.sendDiscordDm(
                    channelId,
                    message = finalMessage,
                    data.groupSeq,
                    data.postSeq,
                    postType?.type
                )
            }
        }

        val fcmTokens = userService.getUserFcmInfo(data.userSeqs)
        log.info("[NotifyEventListener] 알람 전송 시작 - 모바일 알람 ${fcmTokens.size}개")
        fcmTokens.forEach { fcmService.sendMessageByToken(title = data.title, body = data.message, token = it) }

    }
}

data class NotifyInfo(
    val userSeqs: Set<Long>,
    val groupSeq: Long? = null,
    val postSeq: Long? = null,
    val title: String,
    var message: String? = null
) {
}