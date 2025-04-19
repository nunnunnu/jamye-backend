package org.jy.jamye.ui.user

import org.jy.jamye.application.user.EmailApplicationService
import org.jy.jamye.application.user.dto.EmailDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.common.listener.EmailEvent
import org.jy.jamye.domain.user.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/email")
class EmailController(private val emailService: EmailService, private val emailAppService: EmailApplicationService,
                      private val publisher: ApplicationEventPublisher
) {
    val log = LoggerFactory.getLogger(this.javaClass)
    //인증 번호 전송
    @PostMapping("/send")
    fun sendEmail(@RequestParam email: String): ResponseDto<EmailDto> {
        val event = EmailEvent(email = email)
        publisher.publishEvent(event)
        return ResponseDto(status = HttpStatus.OK)
    }

    //이메일 인증
    @PostMapping("/verify")
    fun verifyEmail(@RequestBody requestDto: EmailDto): ResponseDto<Boolean> {
        val isVerified: Boolean = emailService.verifyCode(requestDto.email, requestDto.verifyCode)
        return ResponseDto(status = HttpStatus.OK, data = isVerified)
    }

    @GetMapping("/password")
    fun findPassword(@RequestBody data: FindPassword):ResponseDto<Nothing> {
        emailAppService.findUserAndSendEmail(data.id, data.email)
        return ResponseDto(status = HttpStatus.OK)
    }
}