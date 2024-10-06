package org.jy.jamye.domain.service

import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.infra.UserFactory
import org.jy.jamye.infra.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userFactory: UserFactory, private val userRepo: UserRepository) {
    fun createUser(data: UserDto) {
        val user = userFactory.create(data)
        userRepo.save(user)
    }
}