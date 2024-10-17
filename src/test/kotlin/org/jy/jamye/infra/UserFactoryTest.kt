package org.jy.jamye.infra

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.common.exception.AlreadyRegisteredIdException
import org.jy.jamye.common.exception.DuplicateEmailException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test

@SpringBootTest
@Transactional
class UserFactoryTest @Autowired constructor(var userFactory: UserFactory, val userRepo: UserRepository, var passwordEncoder: PasswordEncoder) {
    private var testId = "setupId"
    private var testEmail = "setupEmail@email.com"
    @BeforeEach
    fun setUp() {
        val create = userFactory.create(
            UserDto(id = testId, email = testEmail, password = "test")
        )

        userRepo.save(create)
    }

    @Test
    @DisplayName("유저 save 성공 테스트")
    fun createUserSuccess() {
        val id = "testid"
        val email = "test1@email.com"
        val password = "test"
        val create = userFactory.create(
            UserDto(id = id, email = email, password = password)
        )

        assertThat(create.userId).isEqualTo(id)
        assertThat(create.email).isEqualTo(email)
        assertThat(passwordEncoder.matches(password, create.password)).isTrue()

    }

    @Test
    @DisplayName("유저 save 실패 - 중복 ID")
    fun createUserDuplicateId() {
        assertThatThrownBy { userFactory
            .create(UserDto(id = testId, email = "test2@email.com", password = "test"))
        }.isInstanceOf(AlreadyRegisteredIdException::class.java)
            .hasMessageContaining("이미 가입된 아이디입니다.")
    }

    @Test
    @DisplayName("유저 save 실패 - 중복 이메일")
    fun createUserDuplicateEmail() {
        assertThatThrownBy { userFactory.create(
                UserDto(id = "test3", email = testEmail, password = "test"))
        }.isInstanceOf(DuplicateEmailException::class.java)
            .hasMessageContaining("이미 가입된 이메일입니다.")
    }

}