package org.jy.jamye.common.listener

import org.jy.jamye.domain.user.service.EmailService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component


data class EmailEvent(
    val email: String,
    val userId: String? = null) {

}

@Component
class EmailEventListener(private val emailService: EmailService) {

    @Async
    @EventListener
    fun codeSendPush(data: EmailEvent) {
        emailService.sendCodeToEmail(data.email)
    }
}