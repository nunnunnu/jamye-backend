package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "user_info")
class User(
    @Column(name = "id", nullable = false)
    val id: String,
    @Column(name = "email", nullable = false)
    val email: String,
    @Column(name = "nick", nullable = false)
    val nickname: String,
    @Column(name = "pw")
    private var password: String,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    var updateDate: LocalDateTime = LocalDateTime.now(),
    @Enumerated(value = EnumType.STRING)
    @Column(name="role") var role: Role,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq", nullable = false)
    val sequence: Long? = null,
) {
}