package org.jy.jamye.domain.user.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import jakarta.servlet.http.HttpServletResponse
import org.jy.jamye.application.user.dto.UserDto
import org.jy.jamye.application.user.dto.UserLoginDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.domain.user.model.LoginType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class SocialAuthService(
    private val redisClient: RedisClient,
    private val userService: UserService
){
    val log: Logger = LoggerFactory.getLogger(SocialAuthService::class.java)
    @Throws(JsonProcessingException::class)
    fun kakaoLogin(code: String, response: HttpServletResponse): UserLoginDto {
        log.info("[카카오 인증코드 - user 정보 조회] 1. 인가 코드로 액세스 토큰 요청 - start")
        val accessToken = getAccessToken(code)
        log.info("[카카오 인증코드 - user 정보 조회] 1. 인가 코드로 액세스 토큰 요청 - end")

        log.info("[카카오 인증코드 - user 정보 조회] 2. 토큰으로 카카오 API 호출 - start")
        val kakaoUserInfo = getKakaoUserInfo(accessToken)
        log.info("[카카오 인증코드 - user 정보 조회] 2. 토큰으로 카카오 API 호출 - end")

        log.info("[카카오 인증코드 - user 정보 조회] 3. 카카오ID로 회원가입 처리 - start")
        val userLogin = userService.registerUserIfNeed(kakaoUserInfo, LoginType.KAKAO)
        log.info("[카카오 인증코드 - user 정보 조회] 3. 카카오ID로 회원가입 처리 - end")
        val token = userLogin.token

        log.info("[카카오 인증코드 - user 정보 조회] 4. 카카오ID로 회원가입 처리 - refresh 토큰 정보 저장")
        redisClient.setValue(token.refreshToken, kakaoUserInfo.id)

        return userLogin
    }

    @Value("\${kakao.redirect}")
    private var kakaoRedirectUrl: String? = null
    @Throws(JsonProcessingException::class)
    private fun getAccessToken(code: String): String {
        log.info("[카카오 인증코드 - user 정보 조회] 인가 코드로 액세스 토큰 요청 - HTTP header 생성")
        val headers: HttpHeaders = HttpHeaders()
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8")

        log.info("[카카오 인증코드 - user 정보 조회] 1. 인가 코드로 액세스 토큰 요청 - HTTP body 생성")
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("grant_type", "authorization_code")
        body.add("client_id", "25166c7ef56c4e3be1613096d91a88f6")
        body.add("redirect_uri", kakaoRedirectUrl)
        body.add("code", code)

        log.info("[카카오 인증코드 - user 정보 조회] 1. 인가 코드로 액세스 토큰 요청 - HTTP 요청 전송")
        val kakaoTokenRequest = HttpEntity(body, headers)
        val rt = RestTemplate()
        val response = rt.exchange<String>(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            kakaoTokenRequest,
            String::class.java
        )

        log.info("[카카오 인증코드 - user 정보 조회] 1. 인가 코드로 액세스 토큰 요청 - HTTP 응답 (JSON) -> 액세스 토큰 파싱")
        val responseBody = response.body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(responseBody)
        return jsonNode["access_token"].asText()
    }

    @Throws(JsonProcessingException::class)
    private fun getKakaoUserInfo(accessToken: String): UserDto {
        log.info("[카카오 인증코드 - user 정보 조회] 2. 토큰으로 카카오 API 호출 - HTTP Header 생성")
        val headers: HttpHeaders = HttpHeaders()
        headers.add("Authorization", "Bearer $accessToken")
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8")

        log.info("[카카오 인증코드 - user 정보 조회] 2. 토큰으로 카카오 API 호출 - HTTP 요청 보내기")
        val kakaoUserInfoRequest = HttpEntity<MultiValueMap<String, String>>(headers)
        val rt = RestTemplate()
        val response = rt.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            kakaoUserInfoRequest,
            String::class.java
        )

        log.info("[카카오 인증코드 - user 정보 조회] 2. 토큰으로 카카오 API 호출 - responseBody 에 있는 정보를 꺼냄")
        val responseBody = response.body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(responseBody)

        val id = jsonNode["id"].asText()
        val kakaoAccount = jsonNode["kakao_account"]
        val email = kakaoAccount?.get("email")?.asText() ?: throw IllegalArgumentException("카카오 로그인에 실패하였습니다.")

        return UserDto(email = email, id = id)
    }

    @Value("\${google.client-id}")
    private var googleClientId: String? = null

    fun googleLogin(token: String): UserLoginDto {
        val googleIdToken = GoogleIdTokenVerifier.Builder(
            NetHttpTransport(), GsonFactory.getDefaultInstance()
        )
            .setAudience(listOf(googleClientId))
            .build()
            .verify(token)
        log.info("[google 인증코드 - user 정보 조회] 1. 구글 아이디 조회")

        if(googleIdToken == null) {
            log.info("[google 인증코드 - user 정보 조회] 1. 구글 아이디 조회 - 실패")
            throw IllegalArgumentException("현재 구글로그인이 불가능합니다")
        }

        log.info("[google 인증코드 - user 정보 조회] 2. 구글 유저 정보 조회")
        val payload = googleIdToken.payload
        val id = payload["sub"] as String
        val email = payload["email"] as String

        val userLogin = userService.registerUserIfNeed(
            UserDto(
                id = id,
                email = email
            ), type = LoginType.GOOGLE
        )
        val tokenInfo = userLogin.token

        log.info("[google 인증코드 - user 정보 조회] 3. refresh token 정보 저장")
        redisClient.setValue(tokenInfo.refreshToken, id)

        return userLogin
    }
}