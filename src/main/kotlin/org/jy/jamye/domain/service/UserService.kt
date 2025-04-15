package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.NotifyDto
import org.jy.jamye.security.JwtTokenProvider
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserLoginDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.exception.PasswordErrorException
import org.jy.jamye.common.util.StringUtils
import org.jy.jamye.domain.model.LoginType
import org.jy.jamye.domain.model.Notify
import org.jy.jamye.infra.NotifyRepository
import org.jy.jamye.infra.UserFactory
import org.jy.jamye.infra.UserReader
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.security.TokenDto
import org.jy.jamye.ui.post.UserUpdateDto
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userFactory: UserFactory,
    private val userRepo: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider,
    private val redisClient: RedisClient,
    private val notifyRepository: NotifyRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val userReader: UserReader
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
        val user = userReader.getUserByIdOrThrow(id)
        return UserDto(sequence = user.sequence, id = user.userId, email = user.email, createDate = user.createDate, updateDate = user.updateDate, loginType = user.loginType)
    }

    @Transactional
    @CacheEvict(cacheNames = ["userCache"], key = "#id")
    fun updateUser(id: String, data: UserUpdateDto): UserDto {
        val user = userReader.getUserByIdOrThrow(id)
        if (!passwordEncoder.matches(data.oldPassword, user.password)) {
            throw BadCredentialsException("비밀번호를 다시 확인해주세요.")
        }
        val encodePassword = if (StringUtils.hasText(data.newPassword)) passwordEncoder.encode(data.newPassword) else null

        user.updateUserInfo(data.email, encodePassword)
        return UserDto(sequence = user.sequence, id = user.userId, email = user.email, updateDate = user.updateDate, createDate = user.createDate)

    }

    @CacheEvict(cacheNames = ["userCache"], key = "#id")
    fun deleteUser(id: String, password: String): Long {
        val user = userReader.getUserByIdOrThrow(id)
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

    fun notifySend(userSeqs: Set<Long>, groupSeq: Long?, postSeq: Long?, message: String) {
        userSeqs.forEach { userSeq ->
            val notify = Notify(message = message,
                groupSeq = groupSeq, postSeq = postSeq, userSeq = userSeq)
            notifyRepository.save(notify)
            getNotifyNoReadCount(userSeq)
        }

    }

    @Transactional
    fun viewNotify(notifySeq: Long): NotifyDto {
        val notify = notifyRepository.findById(notifySeq).orElseThrow { EntityNotFoundException() }
        notify.read()
        notifyRepository.save(notify)
        return NotifyDto(
            groupSeq = notify.groupSeq,
            postSeq = notify.postSeq,
            notifySeq = notify.notiSeq,
            message = notify.message,
            isRead = notify.isRead,
            userSeq = notify.userSeq
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
                isRead = it.isRead,
                createDate = it.createDate,
                userSeq = it.userSeq
            )
        }
        return result.sortedByDescending { it.createDate }
    }

    fun deleteNotify(standardDate: LocalDateTime) {
        val count = notifyRepository.deleteAllByStandardDateBefore((standardDate))
        log.info("[notifyDeleteJob] 알람함 삭제 진행 ${count}개")
    }
    fun getNotifyNoReadCount(userSeq: Long): Long {
        val unreadCount = notifyRepository.countByUserSeqAndIsRead(userSeq, false)
        println("메시지 전송 중: ${userSeq}에게 /queue/unread-count로 $unreadCount 전송")
        messagingTemplate.convertAndSend("/alarm/receive/$userSeq", unreadCount)

        return unreadCount
    }

    fun allNotifyRead(sequence: Long) {
        notifyRepository.notifyInUserAllRead(sequence)
        getNotifyNoReadCount(sequence)
    }

    @Transactional
    fun deleteNotify(userSeq: Long) {
        val count = notifyRepository.deleteAllByUserReadNotify(userSeq)
        log.info("[deleteNotifyRead] $userSeq 읽은 알림 삭제 :${count}개")
    }

    @Transactional
    fun deleteNotify(userSeq: Long, notifySeq: Long) {
        notifyRepository.deleteByNotiSeqAndUserSeq(notifySeq, userSeq)
        getNotifyNoReadCount(userSeq)
    }

    @CacheEvict(cacheNames = ["userCache"], key = "#userId")
    fun discordConnect(userId: String, channelId: String) {
        val user = userReader.getUserByIdOrThrow(userId)
        user.discordConnect(channelId)
        userRepo.save(user)
    }

    @Transactional(readOnly = true)
    fun findDiscordConnectUser(userSeqs: Set<Long>): Set<String> {
        return userRepo
            .findBySequenceInAndDiscordChannelIdNotNull(userSeqs).map { it.discordChannelId!! }.toSet()
    }

    fun registerUserIfNeed(kakaoUserInfo: UserDto, type: LoginType): UserLoginDto {
        var user = userRepo.findByUserId(kakaoUserInfo.id).orElse(null)
        if(user == null) {
            user = userFactory.createSocial(kakaoUserInfo, type)
            userRepo.save(user)
        }
        val token = tokenProvider.getAccessToken(user.userId, user.password)
        return UserLoginDto(sequence = user.sequence!!,
            id = user.userId,
            email = user.email,
            token = token)
    }
}