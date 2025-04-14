package org.jy.jamye.application.dto

import org.jy.jamye.security.TokenDto

class AuthDto(
    val seq: Long,
    val id: String,
    val token: TokenDto? = null,
) {
}