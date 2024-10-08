package org.jy.jamye.infra

import org.jy.jamye.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsByUserId(id: String): Boolean
    fun findByUserId(id: String): Optional<User>
}