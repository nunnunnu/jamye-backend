package org.jy.jamye.infra

import org.jy.jamye.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsById(id: String): Boolean
}