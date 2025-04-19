package org.jy.jamye.common.listener

import org.jy.jamye.domain.user.service.EmailService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component


data class EmailEvent(
    val email: String,
    val userId: String? = null) {

}

@Component
class EmailEventListener(private val emailService: EmailService) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    fun codeSendPush(data: EmailEvent) {
        log.info("[email 인증정보 전송] event listener 수신")
        emailService.sendCodeToEmail(data.email)
    }
}