package org.jy.jamye.infra.user

import org.jy.jamye.application.user.dto.UserDto
import org.jy.jamye.common.exception.AlreadyRegisteredIdException
import org.jy.jamye.common.exception.DuplicateEmailException
import org.jy.jamye.domain.user.model.LoginType
import org.jy.jamye.domain.user.model.Role
import org.jy.jamye.domain.user.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserFactory(
    private val userRepo: UserRepository,
    private val passwordEncoder: PasswordEncoder
)
{
    val log: Logger = LoggerFactory.getLogger(UserFactory::class.java)

    fun create(user: UserDto): User {
        if(userRepo.existsByUserId(user.id)) {
            log.info("[createUser] 회원가입 실패, 중복 ID = {}", user.id)
            throw AlreadyRegisteredIdException()
        }
        if(userRepo.existsByEmail(user.email)) {
            log.info("[createUser] 회원가입 실패, 중복 email = {}", user.email)
            throw DuplicateEmailException()
        }
        val encode = passwordEncoder.encode(user.password)
        log.info("[createUser] 회원가입 실패, 중복 email = {}", user.email)
        return User(userId = user.id, email = user.email, password = encode, role = Role.ROLE_USER)
    }

    fun createSocial(user: UserDto, type: LoginType): User {
        log.info("[카카오 인증코드 - user 정보 조회] 3. 카카오ID로 회원가입 처리 - ${type.name}유형 가입")
        val encode = passwordEncoder.encode(type.basicPassword)
        return User(userId = user.id, email = user.email, password = encode, role = Role.ROLE_USER, loginType = type)
    }
}