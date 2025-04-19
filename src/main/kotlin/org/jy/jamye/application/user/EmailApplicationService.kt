package org.jy.jamye.application.user

import org.jy.jamye.common.util.StringUtils.generateRandomCode
import org.jy.jamye.domain.user.service.EmailService
import org.jy.jamye.domain.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmailApplicationService(
    private val userService: UserService,
    private val emailService: EmailService
) {
    @Transactional
    fun findUserAndSendEmail(id: String, email: String) {
        val randomPassword = generateRandomCode(8)
        emailService.sendRandomPasswordToEmail(randomPassword, email)
        userService.userUpdateRandomPassword(id, email, randomPassword)
    }
}
