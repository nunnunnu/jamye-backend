package org.jy.jamye.infra.user

import org.jy.jamye.common.exception.NonExistentUser
import org.jy.jamye.domain.user.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserReader(private val userRepo: UserRepository) {
    val log: Logger = LoggerFactory.getLogger(UserReader::class.java)

    @Cacheable(cacheNames = ["userCache"], key = "#userId")
    fun getUserByIdOrThrow(userId: String): User {
        log.info("[유저 정보 조회] DB 조회")
        return userRepo.findByUserId(userId)?: throw NonExistentUser()
    }

}