package org.jy.jamye.domain.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.domain.model.User
import org.jy.jamye.infra.UserFactory
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.security.JwtTokenProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.util.*

@Service
class KakaoAuthService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authBuilder: AuthenticationManagerBuilder,
    private val redisClient: RedisClient,
    private val userFactory: UserFactory,
){

    @Throws(JsonProcessingException::class)
    fun kakaoLogin(code: String, response: HttpServletResponse): Long {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        val accessToken = getAccessToken(code)

        // 2. 토큰으로 카카오 API 호출
        val kakaoUserInfo = getKakaoUserInfo(accessToken)

        // 3. 카카오ID로 회원가입 처리
        val kakaoUser = registerKakaoUserIfNeed(kakaoUserInfo)

        // 4. 강제 로그인 처리
        var authentication: Authentication = forceLogin(kakaoUser)

        val authenticationToken =
            UsernamePasswordAuthenticationToken(kakaoUser.userId, kakaoUser.password)

        authentication =
            authBuilder.getObject().authenticate(authenticationToken)

        val refreshToken: String = jwtTokenProvider.generateToken(authentication).refreshToken

        redisClient.setValue(refreshToken, kakaoUser.email)

        return kakaoUserInfo.sequence!!
    }

    @Throws(JsonProcessingException::class)
    private fun getAccessToken(code: String): String {
        // HTTP Header 생성
        val headers: HttpHeaders = HttpHeaders()
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8")

        // HTTP Body 생성
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("grant_type", "authorization_code")
        body.add("client_id", "25166c7ef56c4e3be1613096d91a88f6")
        body.add("redirect_uri", "http://localhost:8081//oauth/kakao") //todo: 변경 필요
        body.add("code", code)

        // HTTP 요청 보내기
        val kakaoTokenRequest = HttpEntity(body, headers)
        val rt = RestTemplate()
        val response = rt.exchange<String>(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            kakaoTokenRequest,
            String::class.java
        )

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        val responseBody = response.body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(responseBody)
        return jsonNode["access_token"].asText()
    }

    @Throws(JsonProcessingException::class)
    private fun getKakaoUserInfo(accessToken: String): UserDto {
        // HTTP Header 생성
        val headers: HttpHeaders = HttpHeaders()
        headers.add("Authorization", "Bearer $accessToken")
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8")

        // HTTP 요청 보내기
        val kakaoUserInfoRequest = HttpEntity<MultiValueMap<String, String>>(headers)
        val rt = RestTemplate()
        val response = rt.exchange<String>(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            kakaoUserInfoRequest,
            String::class.java
        )

        // responseBody에 있는 정보를 꺼냄
        val responseBody = response.body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(responseBody)

        val seq = jsonNode["id"].asLong()
        val id = jsonNode["id"].asText()

        return UserDto(sequence = seq, email = id, id = id)
    }

    private fun registerKakaoUserIfNeed(kakaoUserInfo: UserDto): User {
        var user = userRepository.findByUserId(kakaoUserInfo.id).orElse(null)
        if(user == null) {
            kakaoUserInfo.password = UUID.randomUUID().toString()
            user = userFactory.create(kakaoUserInfo)
            userRepository.save(user)
        }
        return user
    }

    private fun forceLogin(kakaoUser: User): Authentication {
        val userDetails: UserDetails = kakaoUser
        val authentication: Authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        SecurityContextHolder.getContext().authentication = authentication
        return authentication
    }
}