package org.jy.jamye.application.user.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jy.jamye.domain.user.model.LoginType
import org.jy.jamye.security.TokenDto
import java.time.LocalDateTime

data class UserDto (
    val sequence: Long? = null,
    val id: String,
    val email: String,
    @JsonIgnore
    var password: String? = null,
    val createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null,
    val loginType: LoginType? = LoginType.NOMAL,
)

data class UserLoginDto(
    val sequence: Long,
    val id: String,
    val email: String,
    val token: TokenDto,
)
