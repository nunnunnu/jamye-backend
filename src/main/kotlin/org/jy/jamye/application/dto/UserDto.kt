package org.jy.jamye.application.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime

data class UserDto (
    val sequence: Long? = null,
    val id: String,
    val email: String,
    val nickname: String,
    @JsonIgnore
    val password: String,
    val createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null
)
