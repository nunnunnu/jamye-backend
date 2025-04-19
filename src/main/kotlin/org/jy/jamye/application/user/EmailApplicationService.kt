package org.jy.jamye.application.user

import org.jy.jamye.common.util.StringUtils.generateRandomCode
import org.jy.jamye.domain.user.service.EmailService
import org.jy.jamye.domain.user.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmailApplicationService(
    private val userService: UserService,
    private val emailService: EmailService
) {
    val log: Logger = LoggerFactory.getLogger(EmailApplicationService::class.java)
    @Transactional
    fun findUserAndSendEmail(id: String, email: String) {
        log.info("[비밀번호 찾기] 1. 임시 비밀번호 생성")
        val randomPassword = generateRandomCode(8)
        log.info("[비밀번호 찾기] 2. 임시 비밀번호 이메일 발송")
        emailService.sendRandomPasswordToEmail(randomPassword, email)
        userService.userUpdateRandomPassword(id, email, randomPassword)
    }
}
