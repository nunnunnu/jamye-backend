package org.jy.jamye.ui

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.infra.UserFactory
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.security.JwtTokenProvider
import org.jy.jamye.ui.post.LoginPostDto
import org.jy.jamye.ui.post.UserPostDto
import org.jy.jamye.ui.post.UserUpdateDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import kotlin.test.Test

@SpringBootTest
@Transactional
class UserControllerTest @Autowired constructor(
    var userController: UserController,
    var userFactory: UserFactory,
    var userRepo: UserRepository,
    var jwtTokenProvider: JwtTokenProvider
) {
    private var testId = "setupId"
    private var testEmail = "setupEmail@email.com"
    private val testPassword = "setupPassword"
    private var setUpUserSequence: Long? = null

    @BeforeEach
    fun setup() {
        val user = userFactory.create(UserDto(id = testId, email = testEmail, password = testPassword))

        userRepo.save(user)
        setUpUserSequence = user.sequence
    }

    @Test
    @DisplayName("유저 생성 api 테스트 - 성공")
    fun 회원가입_성공() {
        val data = UserPostDto(id = "testId2", email = "test2@email.com", password = "test")
        val response = userController.createUser(data)

        val userSequence: Long? = response.data
        assertThat(userSequence).isNotNull()
        assertThat(response.status).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    @DisplayName("유저 생성 api 테스트 - 실패: 중복 ID")
    fun 회원가입_실패_중복ID() {
        val data = UserPostDto(id = testId, email = "test3@email.com", password = "test")

        assertThatThrownBy {
            assertThat(userController.createUser(data))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("이미 등록된 아이디입니다.")
    }

    @Test
    @DisplayName("유저 생성 api 테스트 - 실패: 중복 ID")
    fun 회원가입_실패_중복이메일() {
        val data = UserPostDto(id = "testId4", email = testEmail, password = "test")

        assertThatThrownBy {
            assertThat(userController.createUser(data))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("이미 등록된 이메일입니다.")
    }

    @Test
    @DisplayName("로그인 성공")
    fun 로그인_성공() {
        val response = userController.login(LoginPostDto(id = testId, password = testPassword))

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val user = response.data!!
        assertThat(user.id).isEqualTo(testId)
        assertThat(user.email).isEqualTo(testEmail)
        assertThat(user.token).isNotNull
        val token = user.token
        val validateToken = jwtTokenProvider.validateToken(token = token.accessToken!!)
        assertThat(validateToken).isTrue()
        val refreshTokenExpired = jwtTokenProvider.isRefreshTokenExpired(token.refreshToken)
        assertThat(refreshTokenExpired).isFalse()
    }

    @Test
    @DisplayName("로그인 실패 - ID 오류")
    fun 로그인_실패_잘못된_ID() {
        assertThatThrownBy {
            assertThat(userController.login(LoginPostDto(id = "없는ID", password = testPassword)))
        }.isInstanceOf(BadCredentialsException::class.java)
            .hasMessageContaining("로그인 정보를 다시 확인해주세요")
    }

    @Test
    @DisplayName("로그인 실패 - ID 오류")
    fun 로그인_실패_잘못된_비밀번호() {
        assertThatThrownBy {
            assertThat(userController.login(LoginPostDto(id = testId, password = "잘못된비밀번호")))
        }.isInstanceOf(BadCredentialsException::class.java)
            .hasMessageContaining("로그인 정보를 다시 확인해주세요")
    }

    @Test
    @DisplayName("회원정보 조회_성공")
    fun 회원정보_조회_성공() {
        val response = userController.getUser(setUpUserSequence!!)
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val user = response.data!!
        assertThat(user.id).isEqualTo(testId)
        assertThat(user.email).isEqualTo(testEmail)
    }

    @Test
    @DisplayName("회원정보 조회 - 실패")
    fun 회원정보_조회_실패_번호없음() {
        assertThatThrownBy {
            assertThat(userController.getUser(0L))
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining("없는 유저 번호를 입력하셨습니다.")
    }

    @Test
    @DisplayName("회원정보 수정 - 성공")
    fun 회원정보_수정_성공() {
        val updateEmail = "update@email.com"
        val newPassword = "newPassword"
        val response = userController.updateUser(
            setUpUserSequence!!,
            UserUpdateDto(email = updateEmail, newPassword = newPassword, oldPassword = testPassword)
        )

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val user = response.data!!
        assertThat(user.id).isEqualTo(testId)
        assertThat(user.email).isEqualTo(updateEmail)
        assertThat(user.password).isNotEqualTo(newPassword)

    }

    @Test
    @DisplayName("회원정보 수정 - 성공")
    fun 회원정보_이메일만_수정() {
        val updateEmail = "update2@email.com"
        val response = userController.updateUser(
            setUpUserSequence!!,
            UserUpdateDto(email = updateEmail, oldPassword = testPassword))

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val user = response.data!!
        assertThat(user.id).isEqualTo(testId)
        assertThat(user.email).isEqualTo(updateEmail)
    }

    @Test
    @DisplayName("회원정보 수정 - 성공")
    fun 회원정보_비밀번호만_수정() {
        val newPassword = "newPassword"
        val response = userController.updateUser(
            setUpUserSequence!!,
            UserUpdateDto(oldPassword = testPassword, newPassword = newPassword))

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val user = response.data!!
        assertThat(user.id).isEqualTo(testId)
        assertThat(user.email).isEqualTo(testEmail)
    }
}