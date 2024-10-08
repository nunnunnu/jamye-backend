package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "user_info")
class User(
    @Column(name = "id", nullable = false)
    val userId: String,
    @Column(name = "email", nullable = false)
    val email: String,
    @Column(name = "pw")
    private var password: String,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    val updateDate: LocalDateTime = LocalDateTime.now(),
    @Enumerated(value = EnumType.STRING)
    @Column(name="role") var role: Role,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ui_seq", nullable = false)
    val sequence: Long? = null,
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        AuthorityUtils.createAuthorityList(this.role.toString())

    override fun getPassword(): String {
        return this.password
    }

    override fun getUsername(): String {
        return this.userId
    }
}