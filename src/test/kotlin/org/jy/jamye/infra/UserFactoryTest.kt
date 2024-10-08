package org.jy.jamye.infra

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.jy.jamye.application.dto.UserDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
@Transactional
class UserFactoryTest @Autowired constructor(var userFactory: UserFactory, val userRepo: UserRepository) {
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
        val create = userFactory.create(
            UserDto(id = id, email = email, password = "test")
        )

        val save = userRepo.save(create)
        assertThat(save.userId).isEqualTo(id)
        assertThat(save.email).isEqualTo(email)

    }

    @Test
    @DisplayName("유저 save 실패 - 중복 ID")
    fun createUserDuplicateId() {
        assertThatThrownBy {
            assertThat(userFactory.create(
                UserDto(id = testId, email = "test2@email.com", password = "test")));
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("이미 등록된 아이디입니다.")
    }

    @Test
    @DisplayName("유저 save 실패 - 중복 이메일")
    fun createUserDuplicateEmail() {
        assertThatThrownBy {
            userFactory.create(
                UserDto(id = "test3", email = testEmail, password = "test")
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("이미 등록된 이메일입니다.")
    }

}