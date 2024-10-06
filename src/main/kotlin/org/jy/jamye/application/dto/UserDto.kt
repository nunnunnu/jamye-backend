package org.jy.jamye.application.dto

import java.time.LocalDateTime

data class UserDto (
    val sequence: Long? = null,
    val id: String,
    val email: String,
    val nickname: String,
    var password: String,
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null
)
