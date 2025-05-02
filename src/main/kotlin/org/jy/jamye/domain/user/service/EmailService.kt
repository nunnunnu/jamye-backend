package org.jy.jamye.domain.user.service

import jakarta.mail.MessagingException
import lombok.extern.slf4j.Slf4j
import org.jy.jamye.application.user.dto.EmailDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.util.StringUtils.generateRandomCode
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Slf4j
@Transactional
@Service
class EmailService(private val redisClient: RedisClient, private val emailSender: JavaMailSender
) {
    val log = LoggerFactory.getLogger(EmailService::class.java)

    @Throws(MessagingException::class)
    fun sendEmail(toEmail: String, title: String, content: String) {
        log.info("[email 전송] start")
        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setTo(toEmail)
        helper.setSubject(title)
        helper.setText(content, true)
        helper.setReplyTo("jamye@gmail.com")
        log.info("[email 전송] 이메일 정보 세팅 완료")
        try {
            emailSender.send(message)
            log.info("[email 전송] 이메일 전송 완료")
        } catch (e: RuntimeException) {
            e.printStackTrace()
            throw RuntimeException("Unable to send email in sendEmail", e) // 원인 예외를 포함시키기
        }
    }

    fun createVerificationCode(email: String): EmailDto {
        log.info("[email 인증정보 전송] 1. 인증 코드 생성 및 저장")
        val randomCode = generateRandomCode(10)

        log.info("[email 인증정보 전송] 1. 인증 코드 생성 및 저장 - 인증코드 redis 저장")
        redisClient.setValueAndExpireTimeMinutes(randomCode, email, 60)
        return EmailDto(verifyCode = randomCode, email = email)
    }

    fun verifyCode(email: String, code: String): Boolean {
        log.info("[email 인증] 인증번호 유효성 검사 -> redis 조회")
        val verifyEmail = redisClient.getAndDelete(code)
        return verifyEmail != null && email == verifyEmail
    }
}