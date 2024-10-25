package org.jy.jamye.ui

import org.jy.jamye.application.dto.EmailDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.service.EmailService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/email")
class EmailController(private val emailService: EmailService) {
    //인증 번호 전송
    @PostMapping("/send")
    fun sendEmail(@RequestParam email: String): ResponseDto<EmailDto> {
        //todo: 이벤트 적용필요
        emailService.sendCodeToEmail(email)
        return ResponseDto(status = HttpStatus.OK)
    }

    //이메일 인증
    @PostMapping("/verify")
    fun verifyEmail(@RequestBody requestDto: EmailDto): ResponseDto<Boolean> {
        val isVerified: Boolean = emailService.verifyCode(requestDto.email, requestDto.verifyCode)
        return ResponseDto(status = HttpStatus.OK, data = isVerified)
    }
}