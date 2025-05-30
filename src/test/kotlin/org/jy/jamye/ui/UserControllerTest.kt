package org.jy.jamye.ui

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.jy.jamye.domain.user.model.User
import org.jy.jamye.infra.*
import org.jy.jamye.infra.group.GroupFactory
import org.jy.jamye.infra.group.GroupRepository
import org.jy.jamye.infra.user.GroupUserRepository
import org.jy.jamye.infra.user.UserFactory
import org.jy.jamye.infra.user.UserRepository
import org.jy.jamye.security.JwtTokenProvider
import org.jy.jamye.ui.user.UserController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Transactional
class UserControllerTest @Autowired constructor(
    var userController: UserController,
    var userFactory: UserFactory,
    var userRepo: UserRepository,
    var jwtTokenProvider: JwtTokenProvider,
    val groupUserRepository: GroupUserRepository,
    val groupRepository: GroupRepository,
    val groupFactory: GroupFactory,
    @PersistenceContext
    private val entityManager: EntityManager // EntityManager 주입
) {
    private var testId = "setupId"
    private var testEmail = "setupEmail@email.com"
    private val testPassword = "setupPassword"
    private var setupUser: User? = null

//    @BeforeEach
//    fun setup() {
//        val user = userFactory.create(UserDto(id = testId, email = testEmail, password = testPassword))
//
//        userRepo.save(user)
//        setupUser = user
//    }
//
//    @Test
//    @DisplayName("유저 생성 api 테스트 - 성공")
//    fun 회원가입_성공() {
//        val data = UserPostDto(id = "testId2", email = "test2@email.com", password = "testtest")
//        val response = userController.createUser(data)
//
//        val userSequence: Long? = response.data
//        assertThat(userSequence).isNotNull()
//        assertThat(response.status).isEqualTo(HttpStatus.CREATED)
//    }
//
//    @Test
//    @DisplayName("유저 생성 api 테스트 - 실패: 중복 ID")
//    fun 회원가입_실패_중복ID() {
//        val data = UserPostDto(id = testId, email = "test3@email.com", password = "testtest")
//
//        assertThatThrownBy { userController.createUser(data)
//        }.isInstanceOf(AlreadyRegisteredIdException::class.java)
//            .hasMessageContaining("이미 가입된 아이디입니다.")
//    }
//
//    @Test
//    @DisplayName("유저 생성 api 테스트 - 실패: 중복 이메일")
//    fun 회원가입_실패_중복이메일() {
//        val data = UserPostDto(id = "testId4", email = testEmail, password = "testtest")
//
//        assertThatThrownBy { userController.createUser(data)
//        }.isInstanceOf(DuplicateEmailException::class.java)
//            .hasMessageContaining("이미 가입된 이메일입니다.")
//    }
//
//    @Test
//    @DisplayName("유저 생성 api 테스트 - 실패: 이메일 형식 에러")
//    fun 회원가입_실패_이메일형식_에러() {
//        assertThatThrownBy { UserPostDto(id = "testId4", email = "sj", password = "testtest")
//        }.isInstanceOf(IllegalArgumentException::class.java)
//            .hasMessageContaining("Invalid email format")
//    }
//
//    @Test
//    @DisplayName("유저 생성 api 테스트 - 실패: 이메일 형식 에러")
//    fun 회원가입_실패_비밀번호자리수_에러() {
//        assertThatThrownBy { UserPostDto(id = "testId4", email = "testEmail@mail.com", password = "test")
//        }.isInstanceOf(IllegalArgumentException::class.java)
//            .hasMessageContaining("Password must be at least 8 characters long")
//    }
//
//    @Test
//    @DisplayName("유저 생성 api 테스트 - 실패: 아이디 입력X")
//    fun 회원가입_실패_아이디_입력X() {
//        val user = UserPostDto(id = "", email = "testEmail@mail.com", password = "testtest")
//        assertThatThrownBy { userController.createUser(user)
//        }.isInstanceOf(ConstraintViolationException::class.java)
//            .hasMessageContaining("아이디는 필수입니다.")
//    }
//
//    @Test
//    @DisplayName("로그인 성공")
//    fun 로그인_성공() {
//        val response = userController.login(LoginPostDto(id = testId, password = testPassword))
//
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        val user = response.data!!
//        assertThat(user.id).isEqualTo(testId)
//        assertThat(user.email).isEqualTo(testEmail)
//        assertThat(user.token).isNotNull
//        val token = user.token
//        val validateToken = jwtTokenProvider.validateToken(token = token.accessToken!!)
//        assertThat(validateToken).isTrue()
//        val refreshTokenExpired = jwtTokenProvider.isRefreshTokenExpired(token.refreshToken)
//        assertThat(refreshTokenExpired).isFalse()
//    }
//
//    @Test
//    @DisplayName("로그인 실패 - ID 오류")
//    fun 로그인_실패_잘못된_ID() {
//        assertThatThrownBy { userController.login(LoginPostDto(id = "없는ID", password = testPassword))
//        }.isInstanceOf(BadCredentialsException::class.java)
//            .hasMessageContaining("로그인 정보를 다시 확인해주세요")
//    }
//
//    @Test
//    @DisplayName("로그인 실패 - ID 오류")
//    fun 로그인_실패_잘못된_비밀번호() {
//        assertThatThrownBy { userController.login(LoginPostDto(id = testId, password = "잘못된비밀번호"))
//        }.isInstanceOf(BadCredentialsException::class.java)
//            .hasMessageContaining("로그인 정보를 다시 확인해주세요")
//    }
//
//    @Test
//    @DisplayName("회원정보 조회_성공")
//    fun 회원정보_조회_성공() {
//        val response = userController.getUser(setupUser!!)
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        val user = response.data!!
//        assertThat(user.id).isEqualTo(testId)
//        assertThat(user.email).isEqualTo(testEmail)
//    }
//
//    @Test
//    @DisplayName("회원정보 조회 - 실패")
//    fun 회원정보_조회_실패_번호없음() {
//        val errorUser = User(userId = "error", email = testEmail, password = testPassword, role = Role.ROLE_USER)
//        assertThatThrownBy { userController.getUser(errorUser)
//        }.isInstanceOf(EntityNotFoundException::class.java)
//            .hasMessageContaining("없는 유저 번호를 입력하셨습니다.")
//    }
//
//    @Test
//    @DisplayName("회원정보 수정 성공 - 전체 정보 수정")
//    fun 회원정보_수정_성공() {
//        val updateEmail = "update@email.com"
//        val newPassword = "newPassword"
//        val response = userController.updateUser(
//            setupUser!!,
//            UserUpdateDto(email = updateEmail, newPassword = newPassword, oldPassword = testPassword)
//        )
//
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        val user = response.data!!
//        assertThat(user.id).isEqualTo(testId)
//        assertThat(user.email).isEqualTo(updateEmail)
//        assertThat(user.createDate).isNotEqualTo(user.updateDate)
//
//    }
//
//    @Test
//    @DisplayName("회원정보 수정 성공 - 이메일만 수정")
//    fun 회원정보_이메일만_수정() {
//        val updateEmail = "update2@email.com"
//        val response = userController.updateUser(
//            setupUser!!,
//            UserUpdateDto(email = updateEmail, oldPassword = testPassword))
//
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        val user = response.data!!
//        assertThat(user.id).isEqualTo(testId)
//        assertThat(user.email).isEqualTo(updateEmail)
//        assertThat(user.createDate).isNotEqualTo(user.updateDate)
//    }
//
//    @Test
//    @DisplayName("회원정보 수정 실패 - 이메일 형식에러")
//    fun 회원정보_수정_실패_이메일형식에러() {
//        val updateEmail = "update2email.com"
//
//        assertThatThrownBy { UserUpdateDto(email = updateEmail, oldPassword = testPassword)
//        }.isInstanceOf(IllegalArgumentException::class.java)
//            .hasMessageContaining("Invalid email format")
//    }
//
//    @Test
//    @DisplayName("회원정보 수정 성공 - 비밀번호만_수정")
//    fun 회원정보_비밀번호만_수정() {
//        val newPassword = "newPassword"
//        val response = userController.updateUser(
//            setupUser!!,
//            UserUpdateDto(oldPassword = testPassword, newPassword = newPassword))
//
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        val user = response.data!!
//        assertThat(user.id).isEqualTo(testId)
//        assertThat(user.email).isEqualTo(testEmail)
//        assertThat(user.createDate).isNotEqualTo(user.updateDate)
//    }
//
//    @Test
//    @DisplayName("회원정보 수정 실패 - 비밀번호자리수에러")
//    fun 회원정보_수정_실패_비밀번호_자리수에러() {
//        val newPassword = "aaaa"
//        assertThatThrownBy { UserUpdateDto(oldPassword = testPassword, newPassword = newPassword)
//        }.isInstanceOf(IllegalArgumentException::class.java)
//            .hasMessageContaining("Password must be at least 8 characters long")
//    }
//
//    @Test
//    @DisplayName("회원 정보 삭제 - 성공")
//    fun 회원정보삭제_성공() {
//        val response = userController.deleteUser(setupUser!!, UserPasswordDto(testPassword))
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//
//        assertThatThrownBy { userController.getUser(setupUser!!)
//        }.isInstanceOf(EntityNotFoundException::class.java)
//            .hasMessageContaining("없는 유저 번호를 입력하셨습니다.")
//
//    }
//
//    @Test
//    @DisplayName("회원 정보 삭제 - 실패")
//    fun 회원정보삭제_실패() {
//        assertThatThrownBy { userController.deleteUser(setupUser!!, UserPasswordDto("잘못된비밀번호"))
//        }.isInstanceOf(PasswordErrorException::class.java)
//            .hasMessageContaining("비밀번호가 일치하지 않습니다.")
//
//        val response = userController.getUser(setupUser!!)
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//    }
//
//    @Test
//    @DisplayName("회원 정보 삭제 - 성공")
//    fun 회원정보삭제_성공_등급자동양도() {
//        var user = userRepo.save(userFactory.create(UserDto(id = "grade", password = "testtest", email = "email@email.com")))
//        val group = groupFactory.createGroup(userSequence = user.sequence!!, GroupDto(name = "name", description = "description"))
//        groupRepository.save(group)
//
//        val masterUser = groupFactory.createGroupMasterConnection(
//            groupSequence = group.sequence!!,
//            userSequence = user.sequence!!, masterUserInfo = UserInGroupDto.Simple(nickname = "masterNickName"), group = group
//        )
//        val normalUser = groupFactory.createGroupNormalUser(
//            group = group,
//            userSequence = setupUser!!.sequence!!, nickName = "normal", profileImageUrl = null
//        )
//        groupUserRepository.save(masterUser)
//        groupUserRepository.save(normalUser)
//
//        userController.deleteUser(user, UserPasswordDto(password = "testtest"))
//
//        assertThatThrownBy { userController.getUser(user)
//        }.isInstanceOf(EntityNotFoundException::class.java)
//            .hasMessageContaining("없는 유저 번호를 입력하셨습니다.")
//        entityManager.clear()
//        val findUser = groupUserRepository.findByUserSequence(setupUser!!.sequence!!)
//        assertThat(findUser.grade).isEqualTo(Grade.MASTER)
//
//    }
}