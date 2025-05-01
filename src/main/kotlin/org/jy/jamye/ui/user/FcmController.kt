package org.jy.jamye.ui.user

import com.google.firebase.messaging.FirebaseMessagingException
import org.jy.jamye.application.user.UserApplicationService
import org.jy.jamye.domain.user.service.FcmService
import org.jy.jamye.domain.user.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/fcm")
class FcmController(
    private val fcmService: FcmService,
    private val userService: UserService
) {
    val log: Logger = LoggerFactory.getLogger(FcmController::class.java)

    // test api
    @PostMapping("/message/send")
    fun sendMessageToken(@RequestParam token: String): ResponseEntity<*> {
        fcmService.sendMessageByToken("test", "testBody2", token)
        return ResponseEntity.ok().build<Any>()
    }

    @PostMapping("/alarm")
    fun setUserToken(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam token: String
    ) {
        log.info("[fcm token update] start")
        userService.updateFcmToken(user.username, token)
    }
}