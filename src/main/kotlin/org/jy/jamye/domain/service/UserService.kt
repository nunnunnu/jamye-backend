package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.security.JwtTokenProvider
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserLoginDto
import org.jy.jamye.common.exception.PasswordErrorException
import org.jy.jamye.common.util.StringUtils
import org.jy.jamye.domain.model.User
import org.jy.jamye.infra.UserFactory
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.ui.post.UserUpdateDto
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
            log.debug("[login] 로그인 실패 = {}", id)
            throw BadCredentialsException("로그인 정보를 다시 확인해주세요")
        }
        val generateToken = tokenProvider.getAccessToken(user.userId, password)
        return UserLoginDto(sequence = user.sequence!!, token = generateToken, id = user.userId, email = user.email)
    }

    @Transactional(readOnly = true)
    fun getUser(id: String): UserDto {
        val user = getUserByIdOrThrow(id)
        return UserDto(sequence = user.sequence, id = user.userId, email = user.email, createDate = user.createDate, updateDate = user.updateDate)
    }

    @Transactional
    fun updateUser(id: String, data: UserUpdateDto): UserDto {
        val user = getUserByIdOrThrow(id)
        if (!passwordEncoder.matches(data.oldPassword, user.password)) {
            throw BadCredentialsException("비밀번호를 다시 확인해주세요.")
        }
        val encodePassword = if (StringUtils.hasText(data.newPassword)) passwordEncoder.encode(data.newPassword) else null

        user.updateUserInfo(data.email, encodePassword)
        return UserDto(sequence = user.sequence, id = user.userId, email = user.email, updateDate = user.updateDate, createDate = user.createDate)

    }

    private fun getUserByIdOrThrow(id: String): User {
        return userRepo.findByUserId(id).orElseThrow { EntityNotFoundException("없는 유저 번호를 입력하셨습니다.") }
    }

    @Transactional
    fun deleteUser(id: String, password: String) {
        val user = getUserByIdOrThrow(id)
        if (!passwordEncoder.matches(password, user.password)) {
            throw PasswordErrorException()
        }
        //todo: 소속그룹 master 등급일때 그룹 삭제여부, 글 삭제여부 결정 필요
        userRepo.deleteById(user.sequence!!)
    }

    fun getUsers(userSeqs: List<Long>): Map<Long, UserDto> {
        val users = userRepo.findAllById(userSeqs)
        return users.associate {it -> it.sequence!! to UserDto(
                sequence = it.sequence,
                id = it.userId,
                email = it.email,
                updateDate = it.updateDate,
                createDate = it.createDate
            )
        }

    }

}