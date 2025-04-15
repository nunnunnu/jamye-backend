package org.jy.jamye.security

data class TokenDto(
    val grantType: String? = "Bearer",
   val refreshToken: String,
   val accessToken: String? = null
) {
    data class GoogleToken(
        val token: String
    )
}