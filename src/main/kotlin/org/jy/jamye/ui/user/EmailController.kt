package org.jy.jamye.ui.user

import org.jy.jamye.application.user.dto.EmailDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.common.listener.EmailEvent
import org.jy.jamye.common.util.StringUtils.generateRandomCode
import org.jy.jamye.domain.user.service.EmailService
import org.jy.jamye.domain.user.service.UserService
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
class EmailController(
    private val emailService: EmailService,
    private val publisher: ApplicationEventPublisher,
    private val userService: UserService
) {
    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/send")
    fun sendEmail(@RequestParam email: String): ResponseDto<EmailDto> {
        log.info("[email 인증정보 전송] start")
        val createdCode: EmailDto = emailService.createVerificationCode(email = email)
        val title = "[잼얘 가챠] 가입 이메일 인증코드입니다."

        log.info("[email 인증정보 전송] 2. 이메일 전송 메세지 생성 - 회원가입 인증 코드")
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
        val event = EmailEvent(email = email, title = title, message = content)
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

    @PostMapping("/password")
    fun findPassword(@RequestBody data: FindPassword):ResponseDto<Nothing> {
        log.info("[비밀번호 찾기] start")
        log.info("[비밀번호 찾기] 1. 임시 비밀번호 생성")
        val randomPassword = generateRandomCode(8)
        userService.userUpdateRandomPassword(data.id, data.email, randomPassword)
        log.info("[비밀번호 찾기] end")
        val title = "[잼얘 가챠] 임시 비밀번호 전송"

        log.info("[비밀번호 찾기] 1. 이메일 전송 메세지 생성 - 랜덤 비밀번호 전송")
        val content = (("""<html>
                <body>
                <h1>임시 비밀번호: """ + randomPassword + "</h1>" + """
                <p style='color: red'>임시 비밀번호로 로그인 후 꼭 비밀번호를 변경해주세요.</p>
                <footer style='color: grey; font-size: small;'>
                <p>※본 메일은 자동응답 메일이므로 본 메일에 회신하지 마시기 바랍니다.</p>
                </footer>
                </body>
                </html>"""))
        val event = EmailEvent(email = data.email, data.id, title = title, message = content)
        log.info("[비밀번호 찾기] 비밀번호 이메일 전송 start")
        publisher.publishEvent(event)
        log.info("[비밀번호 찾기] 비밀번호 이메일 전송 end")
        return ResponseDto(status = HttpStatus.OK)
    }

    @PostMapping("/id")
    fun sendFullId(@RequestParam id: String): ResponseDto<Nothing> {
        log.info("[전체 아이디 이메일 전송] start")
        val user = userService.getUser(id)
        val title = "[잼얘 가챠] 회원 전체 아이디 조회"

        val content = (("""<html>
                <body>
                <h1>전체 아이디: """ + user.id + "</h1>" + """
                <footer style='color: grey; font-size: small;'>
                <p>※본 메일은 자동응답 메일이므로 본 메일에 회신하지 마시기 바랍니다.</p>
                </footer>
                </body>
                </html>"""))
        log.info("[전체 아이디 이메일 전송] 메일 형식 생성 완료")
        val event = EmailEvent(email = user.email, user.id, title = title, message = content)
        log.info("[전체 아이디 이메일 전송] 비밀번호 이메일 전송 start")
        publisher.publishEvent(event)
        log.info("[전체 아이디 이메일 전송] 비밀번호 이메일 전송 end")
        return ResponseDto()
    }
}