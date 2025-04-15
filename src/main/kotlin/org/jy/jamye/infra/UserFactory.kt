package org.jy.jamye.infra

import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.common.exception.AlreadyRegisteredIdException
import org.jy.jamye.common.exception.DuplicateEmailException
import org.jy.jamye.domain.model.LoginType
import org.jy.jamye.domain.model.Role
import org.jy.jamye.domain.model.User
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
            log.debug("[createUser] 회원가입 실패, 중복 ID = {}", user.id)
            throw AlreadyRegisteredIdException()
        }
        if(userRepo.existsByEmail(user.email)) {
            log.debug("[createUser] 회원가입 실패, 중복 email = {}", user.email)
            throw DuplicateEmailException()
        }
        val encode = passwordEncoder.encode(user.password)
        return User(userId = user.id, email = user.email, password = encode, role = Role.ROLE_USER)
    }

    fun createSocial(user: UserDto, type: LoginType): User {
        val encode = passwordEncoder.encode(type.basicPassword)
        return User(userId = user.id, email = user.email, password = encode, role = Role.ROLE_USER, loginType = type)
    }
}