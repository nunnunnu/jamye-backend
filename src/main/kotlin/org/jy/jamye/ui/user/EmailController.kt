package org.jy.jamye.ui.user

import org.jy.jamye.application.user.EmailApplicationService
import org.jy.jamye.application.user.dto.EmailDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.common.listener.EmailEvent
import org.jy.jamye.domain.user.service.EmailService
import org.slf4j.Logger
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
    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/send")
    fun sendEmail(@RequestParam email: String): ResponseDto<EmailDto> {
        log.info("[email 인증정보 전송] start")
        val event = EmailEvent(email = email)
        publisher.publishEvent(event)
        log.info("[email 인증정보 전송] end")
        return ResponseDto(status = HttpStatus.OK)
    }

    @PostMapping("/verify")
    fun verifyEmail(@RequestBody requestDto: EmailDto): ResponseDto<Boolean> {
        log.info("[email 인증] start")
        val isVerified: Boolean = emailService.verifyCode(requestDto.email, requestDto.verifyCode)
        log.info("[email 인증] end")
        return ResponseDto(status = HttpStatus.OK, data = isVerified)
    }

    @GetMapping("/password")
    fun findPassword(@RequestBody data: FindPassword):ResponseDto<Nothing> {
        log.info("[비밀번호 찾기] start")
        emailAppService.findUserAndSendEmail(data.id, data.email)
        log.info("[비밀번호 찾기] end")
        return ResponseDto(status = HttpStatus.OK)
    }
}