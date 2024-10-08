package org.jy.jamye.Security

data class TokenDto(
    val grantType: String? = "Bearer",
   val refreshToken: String,
   val accessToken: String? = null
)