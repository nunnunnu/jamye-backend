package org.jy.jamye.ui.user

import jakarta.validation.constraints.NotBlank

data class UserPostDto (
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,
    @field:NotBlank(message = "아이디는 필수입니다.")
    val id: String,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String
){

    init {
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) { "Invalid email format" }
        require(password.length >= 8) { "Password must be at least 8 characters long" }
    }
}

data class LoginPostDto(
    val id: String,
    val password: String
)

data class UserUpdateDto(
    val email: String? = null,
    val newPassword: String? = null,
    val oldPassword: String
) {
    init {
        email?.let { require(it.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) { "Invalid email format" } }
        newPassword?.let{ require(newPassword.length >= 8) { "Password must be at least 8 characters long" } }
    }
}

data class UserPasswordDto(
    val password: String
)

class FindPassword(
    val email: String,
    val id: String
) {

}


