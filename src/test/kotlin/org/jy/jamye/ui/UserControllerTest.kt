package org.jy.jamye.ui

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.domain.model.Role
import org.jy.jamye.domain.model.User
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.ui.post.UserPostDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import kotlin.test.Test

@SpringBootTest
@Transactional
class UserControllerTest @Autowired constructor(var userController: UserController, var userRepo: UserRepository) {
    private var testId = "setupId"
    private var testEmail = "setupEmail@email.com"

    @BeforeEach
    fun setup() {
        val user = User(id = testId, email = testEmail, password = "test", nickname = "test", role = Role.USER)
        userRepo.save(user)
    }

    @Test
    @DisplayName("유저 생성 api 테스트 - 성공")
    fun createUser() {
        val data = UserPostDto(id = "testId2", email = "test2@email.com", password = "test", nickname = "test")
        val response = userController.createUser(data)

        val userSequence: Long? = response.data
        assertThat(userSequence).isNotNull()
        assertThat(response.status).isEqualTo(HttpStatus.CREATED)
    }

}