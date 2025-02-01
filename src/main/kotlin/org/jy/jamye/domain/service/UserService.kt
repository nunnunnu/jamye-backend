package org.jy.jamye.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.NotifyDto
import org.jy.jamye.security.JwtTokenProvider
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserLoginDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.exception.PasswordErrorException
import org.jy.jamye.common.util.StringUtils
import org.jy.jamye.domain.model.Notify
import org.jy.jamye.domain.model.User
import org.jy.jamye.infra.NotifyRepository
import org.jy.jamye.infra.UserFactory
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.security.TokenDto
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
    private val tokenProvider: JwtTokenProvider,
    private val redisClient: RedisClient,
    private val notifyRepository: NotifyRepository
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
        val token = tokenProvider.getAccessToken(user.userId, user.password)
        redisClient.setIdByRefreshToken(userId = user.userId, refreshToken = token.refreshToken)
        return UserLoginDto(sequence = user.sequence!!, token = token, id = user.userId, email = user.email)
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

    fun deleteUser(id: String, password: String): Long {
        val user = getUserByIdOrThrow(id)
        if (!passwordEncoder.matches(password, user.password)) {
            throw PasswordErrorException()
        }
        userRepo.deleteById(user.sequence!!)
        return user.sequence!!
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

    fun userUpdateRandomPassword(id: String, email: String, randomPassword: String): String {
        val user = userRepo.findByUserIdAndEmail(id, email).orElseThrow { throw EntityNotFoundException() }
        val encodePassword = passwordEncoder.encode(randomPassword)
        user.updateUserInfo(encodePassword = encodePassword)
        return randomPassword
    }

    fun passwordCheck(id: String, password: String) {
        val user = userRepo.findByUserId(id).orElseThrow { throw BadCredentialsException("로그인 정보를 다시 확인해주세요") }
        if (!passwordEncoder.matches(password, user.password)) {
            throw PasswordErrorException("비밀번호가 잘못되었습니다.")
        }
    }

    @Transactional(readOnly = true)
    fun getAccessToken(refreshToken: String): TokenDto {
        val userId = redisClient.getIdByRefreshToken(refreshToken)
        if (tokenProvider.isRefreshTokenExpired(refreshToken)) {
            // TODO: 만료토큰 관리로직 추가 필요
            log.info("[getAccessToken] 만료된 refresh 토큰 = {}", refreshToken)
            throw IllegalArgumentException("만료된 토큰")
        }
        val user = userRepo.findByUserId(userId)
            .orElseThrow {
                log.info("[getAccessToken] refresh 토큰의 유저 정보가 존재하지않음 = {}", userId)
                throw IllegalArgumentException("refresh 토큰의 유저 정보가 존재하지않습니다.")
            }

        val generateToken = tokenProvider.getAccessToken(user.userId, user.password)
        redisClient.setIdByRefreshToken(generateToken.refreshToken, user.userId)
        return TokenDto(refreshToken = refreshToken, accessToken = generateToken.accessToken)
    }

    fun notifyOnPostUpdate(userSeqs: Set<Long>, groupSeq: Long, postSeq: Long, groupName: String, postName: String) {
        userSeqs.forEach { userSeq ->
            val notify = Notify(message = "보유하신 " + groupName+"의 잼얘 " + postName + "이 업데이트되었습니다.",
                groupSeq = groupSeq, postSeq = postSeq, userSeq = userSeq)
            notifyRepository.save(notify)
        }
    }

    @Transactional(readOnly = true)
    fun viewNotify(notifySeq: Long): NotifyDto {
        val notify = notifyRepository.findById(notifySeq).orElseThrow { EntityNotFoundException() }
        notify.read()
        notifyRepository.save(notify)
        return NotifyDto(
            groupSeq = notify.groupSeq,
            postSeq = notify.postSeq,
            notifySeq = notify.notiSeq,
            message = notify.message,
            isRead = notify.isRead
        )
    }

    @Transactional(readOnly = true)
    fun getNotifyList(userSeq: Long): List<NotifyDto> {
        val notifyList = notifyRepository.findAllByUserSeq(userSeq)
        val result = notifyList.map {
            NotifyDto(
                groupSeq = it.groupSeq,
                postSeq = it.postSeq,
                notifySeq = it.notiSeq,
                message = it.message,
                isRead = it.isRead
            )
        }
        result.sortedBy { !it.isRead }
        return result
    }

}