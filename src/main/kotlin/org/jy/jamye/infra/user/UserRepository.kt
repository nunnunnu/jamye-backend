package org.jy.jamye.infra.user

import org.jy.jamye.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Meta
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    @Meta(comment = "이메일 중복 체크")
    fun existsByEmail(email: String): Boolean
    @Meta(comment = "ID 중복 체크")
    fun existsByUserId(id: String): Boolean
    @Meta(comment = "ID로 유저 정보 조회")
    fun findByUserId(id: String): Optional<User>
    fun findByUserIdAndEmail(userId: String, email: String): Optional<User>
    fun findBySequenceInAndDiscordChannelIdNotNull(userSeqs: Set<Long>): List<User>
    @Meta(comment = "아이디 찾기")
    fun findByEmail(email: String): Optional<User>
}