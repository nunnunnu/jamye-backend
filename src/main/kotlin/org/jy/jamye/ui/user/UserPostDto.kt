package org.jy.jamye.ui.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.jy.jamye.common.util.Password

data class UserPostDto(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "이메일 형식이 아닙니다.")
    val email: String,
    @field:NotBlank(message = "아이디는 필수입니다.")
    val id: String,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Password
    val password: String
){

    init {
        require(password.length >= 8) { "Password must be at least 8 characters long" }
    }
}

data class LoginPostDto(
    @field:NotBlank(message = "아이디는 필수입니다.")
    val id: String,
    @field:Password
    val password: String
)

data class UserUpdateDto(
    @field:Email(message = "이메일 형식이 아닙니다.")
    val email: String? = null,
    @field:Password
    val newPassword: String? = null,
    @field:Password
    val oldPassword: String
) {
    init {
        newPassword?.let { require(newPassword.length >= 8) { "Password must be at least 8 characters long" } }
    }
}

data class UserPasswordDto(
    @field:Password
    val password: String
)

class FindPassword(
    @field:Email(message = "이메일 형식이 아닙니다.")
    val email: String,
    @field:NotBlank
    val id: String
) {

}


