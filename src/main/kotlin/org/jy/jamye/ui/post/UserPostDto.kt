package org.jy.jamye.ui.post

data class UserPostDto (
    val email: String,
    val id: String,
    val password: String
)

data class LoginPostDto(
    val id: String,
    val password: String
)

data class UserUpdateDto(
    val email: String? = null,
    val newPassword: String? = null,
    val oldPassword: String
)


