package org.jy.jamye.infra

import lombok.RequiredArgsConstructor
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.domain.model.Role
import org.jy.jamye.domain.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserFactory(private val userRepo: UserRepository)
{
    val log: Logger = LoggerFactory.getLogger(UserFactory::class.java)
    fun create(user: UserDto): User {
        if(userRepo.existsById(user.id)) {
            log.info("[createUser] 회원가입 실패, 중복 ID = {}", user.id)
            throw IllegalArgumentException("이미 등록된 아이디입니다.")
        }
        if(userRepo.existsByEmail(user.email)) {
            log.info("[createUser] 회원가입 실패, 중복 email = {}", user.email)
            throw IllegalArgumentException("이미 등록된 이메일입니다.")
        }
        return User(id = user.id, email = user.email, password = user.password, nickname = user.nickname, role = Role.USER)
    }
}