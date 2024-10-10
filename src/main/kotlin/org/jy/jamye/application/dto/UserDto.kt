package org.jy.jamye.application.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jy.jamye.security.TokenDto
import java.time.LocalDateTime

data class UserDto (
    val sequence: Long? = null,
    val id: String,
    val email: String,
    @JsonIgnore
    val password: String? = null,
    val createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null
)

data class UserLoginDto(
    val sequence: Long,
    val id: String,
    val email: String,
    val token: TokenDto,
)
