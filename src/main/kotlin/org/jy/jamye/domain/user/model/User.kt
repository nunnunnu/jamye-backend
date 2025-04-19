package org.jy.jamye.domain.user.model

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
    var email: String,
    @Column(name = "pw")
    private var password: String,
    @Column(name = "dc_ch")
    var discordChannelId: String? = null,
    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    var loginType: LoginType? = LoginType.NOMAL,
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

    fun updateUserInfo(email: String? = null, encodePassword: String? = null) {
        email?.let {
            this.email = it
        }
        encodePassword?.let {
            this.password = it
        }
    }

    fun discordConnect(channelId: String) {
        this.discordChannelId = channelId
    }
}


enum class Role {
    ROLE_USER
}
enum class LoginType(val basicPassword: String?) {
    NOMAL(null), KAKAO("[KAKAO]PASSWORD"), GOOGLE("[GOOGLE]PASSWORD")
}