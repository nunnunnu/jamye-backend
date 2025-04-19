package org.jy.jamye.infra.user

import org.jy.jamye.common.exception.NonExistentUser
import org.jy.jamye.domain.user.model.User
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserReader(private val userRepo: UserRepository) {
    @Cacheable(cacheNames = ["userCache"], key = "#userId")
    fun getUserByIdOrThrow(userId: String): User {
        return userRepo.findByUserId(userId).orElseThrow { NonExistentUser() }
    }

}