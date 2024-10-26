package org.jy.jamye.domain.service

import jakarta.mail.MessagingException
import lombok.extern.slf4j.Slf4j
import org.jy.jamye.application.dto.EmailDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.listener.EmailEvent
import org.jy.jamye.common.util.StringUtils.generateRandomCode
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
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
        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setTo(toEmail)
        helper.setSubject(title)
        helper.setText(content, true)
        helper.setReplyTo("jamye@gmail.com")
        try {
            emailSender.send(message)
            log.info("이메일 전송 완료")
        } catch (e: RuntimeException) {
            e.printStackTrace() // 또는 로거를 사용하여 상세한 예외 정보 로깅
            throw RuntimeException("Unable to send email in sendEmail", e) // 원인 예외를 포함시키기
        }
    }

    fun sendCodeToEmail(email: String) {
        val createdCode: EmailDto = createVerificationCode(email = email)
        val title = "[잼얘 가챠] 가입 이메일 인증코드입니다."

        val content = (("""<html>
                <body>
                <h1>잼얘 가챠 인증 코드: """ + createdCode.verifyCode + "</h1>" + """
                <p>해당 코드를 회원가입 인증코드 입력칸에 입력하세요.</p>
                <footer style='color: grey; font-size: small;'>
                <p>※본 메일은 자동응답 메일이므로 본 메일에 회신하지 마시기 바랍니다.</p>
                <p>※인증 코드는 1시간 뒤 만료됩니다.</p>
                </footer>
                </body>
                </html>"""))
        try {
            sendEmail(email, title, content)
        } catch (e: java.lang.RuntimeException) {
            e.printStackTrace() // 또는 로거를 사용하여 상세한 예외 정보 로깅
            throw java.lang.RuntimeException("Unable to send email in sendCodeToEmail", e) // 원인 예외를 포함시키기
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw java.lang.RuntimeException("Unable to send email in sendCodeToEmail", e)
        }
    }

    // 인증 코드 생성 및 저장
    fun createVerificationCode(email: String): EmailDto {
        val randomCode = generateRandomCode(10)

        redisClient.setValueAndExpireTimeMinutes(randomCode, email, 60)
        return EmailDto(verifyCode = randomCode, email = email)
    }

    // 인증 코드 유효성 검사
    fun verifyCode(email: String, code: String): Boolean {
        val verifyEmail = redisClient.getAndDelete(code)
        return verifyEmail != null && email==verifyEmail
    }

    fun sendRandomPasswordToEmail(randomPassword: String, email: String) {
        val title = "[잼얘 가챠] 신규 비밀번호입니다."

        val content = (("""<html>
                <body>
                <h1>잼얘 가챠 신규 비밀번호: """ + randomPassword + "</h1>" + """
                <p>신규 비밀번호를 사용하여 로그인 한 다음 꼭 비밀번호를 변경해주세요.</p>
                <footer style='color: red; font-size: small;'>
                <p>※본 메일은 자동응답 메일이므로 본 메일에 회신하지 마시기 바랍니다.</p>
                </footer>
                </body>
                </html>"""))
        try {
            sendEmail(email, title, content)
        } catch (e: java.lang.RuntimeException) {
            e.printStackTrace() // 또는 로거를 사용하여 상세한 예외 정보 로깅
            throw java.lang.RuntimeException("Unable to send email in sendCodeToEmail", e) // 원인 예외를 포함시키기
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw java.lang.RuntimeException("Unable to send email in sendCodeToEmail", e)
        }
    }
}