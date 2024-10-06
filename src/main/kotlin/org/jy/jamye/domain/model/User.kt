package org.jy.jamye.domain.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "user_info")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq", nullable = false)
    val sequence: Long? = null,
    @Column(name = "id", nullable = false)
    val id: String,
    @Column(name = "email", nullable = false)
    val email: String,
    @Column(name = "nick", nullable = false)
    val nickname: String,
    @Column(name = "pw")
    var password: String,
    @Column(name = "create_date")
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    var updateDate: LocalDateTime = LocalDateTime.now(),
    @Enumerated(value = EnumType.STRING)
    @Column(name="role") var role: Role,
) {
}