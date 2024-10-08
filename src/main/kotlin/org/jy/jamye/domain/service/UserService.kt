package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.Security.JwtTokenProvider
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserLoginDto
import org.jy.jamye.infra.UserFactory
import org.jy.jamye.infra.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userFactory: UserFactory,
    private val userRepo: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider
) {
    val log = LoggerFactory.getLogger(UserService::class.java)
    @Transactional
    fun createUser(data: UserDto): Long {
        val user = userFactory.create(data)
        userRepo.save(user)
        return user.sequence!!
    }

    @Transactional(readOnly = true)
    fun login(id: String, password: String): UserLoginDto {
        val user = userRepo.findByUserId(id).orElseThrow { throw BadCredentialsException("로그인 정보를 다시 확인해주세요") }
        if (!passwordEncoder.matches(password, user.password)) {
            log.info("[login] 로그인 실패 = {}", id)
            throw BadCredentialsException("로그인 정보를 다시 확인해주세요")
        }
        val generateToken = tokenProvider.getAccessToken(user.userId, password)
        return UserLoginDto(sequence = user.sequence!!, token = generateToken, id = user.userId, email = user.email)
    }

}