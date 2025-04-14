package org.jy.jamye.ui

import jakarta.servlet.http.HttpServletResponse
import org.jy.jamye.domain.service.KakaoAuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class AuthController(private val kakaoAuthService: KakaoAuthService) {
    @GetMapping("/kakao/callback")
    fun handleKakaoAccessToken(@RequestParam code: String, response: HttpServletResponse) {
        kakaoAuthService.kakaoLogin(code = code, response = response)
    }
}