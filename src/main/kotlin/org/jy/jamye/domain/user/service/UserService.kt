package org.jy.jamye.domain.user.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.user.dto.NotifyDto
import org.jy.jamye.security.JwtTokenProvider
import org.jy.jamye.application.user.dto.UserDto
import org.jy.jamye.application.user.dto.UserLoginDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.exception.PasswordErrorException
import org.jy.jamye.common.util.StringUtils
import org.jy.jamye.domain.user.model.LoginType
import org.jy.jamye.domain.user.model.Notify
import org.jy.jamye.infra.user.UserFactory
import org.jy.jamye.infra.user.UserReader
import org.jy.jamye.infra.user.NotifyRepository
import org.jy.jamye.infra.user.UserRepository
import org.jy.jamye.security.TokenDto
import org.jy.jamye.ui.user.UserUpdateDto
import org.slf4j.Logger
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
    val log: Logger = LoggerFactory.getLogger(UserService::class.java)

    @Transactional
    fun createUser(data: UserDto): Long {
        log.info("[createUser] user entity 생성")
        val user = userFactory.create(data)
        log.info("[createUser] user entity 저장")
        userRepo.save(user)
        return user.sequence!!
    }

    @Transactional(readOnly = true)
    fun login(id: String, password: String): UserLoginDto {
        val user = userRepo.findByUserId(id).orElseThrow {
            log.info("[login] 로그인 정보 조회 실패")
            throw BadCredentialsException("로그인 정보를 다시 확인해주세요")
        }
        log.info("[login] 로그인 정보 조회 성공")
        if (!passwordEncoder.matches(password, user.password)) {
            log.info("[login] 로그인 실패 - 비밀번호 오류")
            throw BadCredentialsException("로그인 정보를 다시 확인해주세요")
        }
        val token = tokenProvider.getAccessToken(user.userId, user.password)
        redisClient.setIdByRefreshToken(userId = user.userId, refreshToken = token.refreshToken)
        return UserLoginDto(sequence = user.sequence!!, token = token, id = user.userId, email = user.email)
    }

    @Transactional(readOnly = true)
    fun getUser(id: String): UserDto {
        log.info("[유저 정보 조회]")
        val user = userReader.getUserByIdOrThrow(id)
        return UserDto(sequence = user.sequence, id = user.userId, email = user.email, createDate = user.createDate, updateDate = user.updateDate, loginType = user.loginType)
    }

    @Transactional
    @CacheEvict(cacheNames = ["userCache"], key = "#id")
    fun updateUser(id: String, data: UserUpdateDto): UserDto {
        val user = userReader.getUserByIdOrThrow(id)
        if (!passwordEncoder.matches(data.oldPassword, user.password)) {
            log.info("[유저 정보 수정] 실패 - 비밀번호 오류")
            throw BadCredentialsException("비밀번호를 다시 확인해주세요.")
        }
        val encodePassword =
            if (StringUtils.hasText(data.newPassword)) passwordEncoder.encode(data.newPassword) else null

        user.updateUserInfo(data.email, encodePassword)
        return UserDto(sequence = user.sequence, id = user.userId, email = user.email, updateDate = user.updateDate, createDate = user.createDate)

    }

    @CacheEvict(cacheNames = ["userCache"], key = "#id")
    fun deleteUser(id: String, password: String): Long {
        val user = userReader.getUserByIdOrThrow(id)
        if (!passwordEncoder.matches(password, user.password)) {
            log.info("[유저 탈퇴] 실패 - 비밀번호 오류")
            throw PasswordErrorException()
        }
        log.info("[유저 탈퇴] 유저 정보 삭제")
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

    @Transactional
    fun userUpdateRandomPassword(id: String, email: String, randomPassword: String): String {
        val user = userRepo.findByUserIdAndEmail(id, email).orElseThrow { throw EntityNotFoundException() }
        val encodePassword = passwordEncoder.encode(randomPassword)
        user.updateUserInfo(encodePassword = encodePassword)
        return randomPassword
    }

    fun passwordCheck(id: String, password: String) {
        val user = userRepo.findByUserId(id).orElseThrow {
            log.info("[비밀번호 검증] 아이디 오류")
            throw BadCredentialsException("로그인 정보를 다시 확인해주세요")
        }
        if (!passwordEncoder.matches(password, user.password)) {
            log.info("[비밀번호 검증] 비밀번호 오류 오류")
            throw PasswordErrorException("비밀번호가 잘못되었습니다.")
        }
    }

    @Transactional(readOnly = true)
    fun getAccessToken(refreshToken: String): TokenDto {
        val userId = redisClient.getIdByRefreshToken(refreshToken)
        if (tokenProvider.isRefreshTokenExpired(refreshToken)) {
            log.info("[access token 재발급] 만료된 refresh 토큰 = {}", refreshToken)
            throw IllegalArgumentException("만료된 토큰")
        }
        val user = userRepo.findByUserId(userId)
            .orElseThrow {
                log.info("[access token 재발급] refresh 토큰의 유저 정보가 존재하지않음 = {}", userId)
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
        val notify = notifyRepository.findById(notifySeq).orElseThrow {
            log.info("[알람함 읽음 처리] 실패 - 번호 없음")
            throw EntityNotFoundException()
        }
        notify.read()
        notifyRepository.save(notify)
        log.info("[알람함 읽음 처리] 읽음 정보 update")
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
        log.info("[알람함 조회] 모든 알람 조회 ${notifyList.size}")
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
        log.info("[알람함 조회] 생성일자 descending 정렬")
        return result.sortedByDescending { it.createDate }
    }

    fun deleteNotify(standardDate: LocalDateTime) {
        val count = notifyRepository.deleteAllByStandardDateBefore((standardDate))
        log.info("[notifyDeleteJob] 알람함 삭제 진행 ${count}개")
    }
    fun getNotifyNoReadCount(userSeq: Long): Long {
        log.info("[안읽은 알람 갯수 socket 채널 전송] start")
        val unreadCount = notifyRepository.countByUserSeqAndIsRead(userSeq, false)
        log.info("메시지 전송 중: ${userSeq}에게 /queue/unread-count로 $unreadCount 전송")
        messagingTemplate.convertAndSend("/alarm/receive/$userSeq", unreadCount)
        log.info("[안읽은 알람 갯수 socket 채널 전송] end")
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
        log.info("[알람함 discord 연동] discord 채널 아이디 유저정보 저장")
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
        log.info("[${type.name} 인증코드 - user 정보 조회] 3. ${type.name}ID로 회원가입 처리 - user 정보 조회")
        var user = userRepo.findByUserId(kakaoUserInfo.id).orElse(null)
        if(user == null) {
            log.info("[${type.name} 인증코드 - user 정보 조회] 3. ${type.name}ID로 회원가입 처리 - 미가입 회원 -> 강제 회원가입 처리")
            user = userFactory.createSocial(kakaoUserInfo, type)
            userRepo.save(user)
            log.info("[${type.name} 인증코드 - user 정보 조회] 3. ${type.name}ID로 회원가입 처리 - 미가입 회원 강제 회원가입 성공")
        }
        log.info("[${type.name} 인증코드 - user 정보 조회] 3. ${type.name}ID로 회원가입 처리 - 엑세스 토큰 발급")
        val token = tokenProvider.getAccessToken(user.userId, user.password)
        return UserLoginDto(sequence = user.sequence!!,
            id = user.userId,
            email = user.email,
            token = token)
    }

    @Transactional
    fun updateFcmToken(userId: String, token: String) {
        val user = userReader.getUserByIdOrThrow(userId)
        user.fcmToken = token
        userRepo.save(user)
        log.info("[fcm token update] end")
    }

    @Transactional(readOnly = true)
    fun getUserFcmInfo(userSeqs: Set<Long>): Set<String> {
        //TODO: 1:n 구조 변경 필요
        val users = userRepo.findAllById(userSeqs)
        return users.filter { it.fcmToken != null }.map { it.fcmToken!! }.toSet()

    }

    @Transactional(readOnly = true)
    fun findIdByEmail(email: String): String {
        val user = userRepo.findByEmail(email).orElseThrow { EntityNotFoundException() }
        if (user.loginType == LoginType.KAKAO || user.loginType == LoginType.GOOGLE) {
            throw EntityNotFoundException("소셜로그인을 통한 가입 회원입니다. 해당 서비스를 이용하여 로그인해주세요")
        }
        return user.userId
    }
}