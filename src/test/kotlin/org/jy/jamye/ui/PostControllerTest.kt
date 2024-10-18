package org.jy.jamye.ui

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.exception.PostAccessDeniedException
import org.jy.jamye.domain.model.Group
import org.jy.jamye.domain.model.Post
import org.jy.jamye.domain.model.User
import org.jy.jamye.domain.model.UserGroupPost
import org.jy.jamye.infra.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class PostControllerTest @Autowired constructor(
    private val postController: PostController,
    private val postRepository: PostRepository,
    private val postFactory: PostFactory,
    private val userFactory: UserFactory,
    private val userRepository: UserRepository,
    private val groupFactory: GroupFactory,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val userGroupPostRepository: UserGroupPostRepository
) {
    private val title = "Test title"
    private var testId = "setupId"
    private var testEmail = "setupEmail@email.com"
    private val testPassword = "setupPassword"
    private val masterNickname = "masterNickname"
    private var setupUser: User? = null
    private var groupUser: User? = null
    private var setupGroup: Group? = null
    private var setupPost: Post? = null
    @BeforeEach
    fun init() {
        setupUser = userRepository.save(userFactory.create((UserDto(id = testId, email = testEmail, password = testPassword))))
        groupUser = userRepository.save(userFactory.create((UserDto(id = "testId2", email = "setupEmail2@email.com", password = testPassword))))
        val group = groupFactory.createGroup(userSequence = setupUser!!.sequence!!, GroupDto(name = "test", description = "description"))
        setupGroup = groupRepository.save(group)

        val masterUser = groupFactory.createGroupMasterConnection(
            groupSequence = group.sequence!!,
            userSequence = setupUser!!.sequence!!, masterUserInfo = UserInGroupDto.Simple(nickname = masterNickname), group = group
        )
        groupUserRepository.save(masterUser)

        val normalUser = groupFactory.createGroupNormalUser(
            group = group,
            userSequence = groupUser!!.sequence!!, nickName = "dd", profileImageUrl = null
        )
        groupUserRepository.save(normalUser)
        setupPost = postRepository.save(
            postFactory.createPost(
                PostDto(
                    title = title,
                    createdUserSequence = setupUser!!.sequence!!,
                    groupSequence = setupGroup!!.sequence!!
                )
            )
        )
        userGroupPostRepository.save(UserGroupPost(groupSequence = setupGroup!!.sequence!!, userSequence = setupUser!!.sequence!!, postSequence = setupPost!!.postSequence!!))
    }

    @Test
    fun getPost_success() {
        val response = postController.getPost(
            groupSequence = setupGroup!!.sequence!!,
            postSequence = setupPost!!.postSequence!!,
            user = setupUser!!
        )

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        response.data!!.let {
            assertThat(it.title).isEqualTo(title)
            assertThat(it.createdUserNickName).isEqualTo(masterNickname)
            assertThat(it.createdUserSequence).isEqualTo(setupUser!!.sequence)
        }
    }

    @Test
    fun getPost_fail_access() {
        assertThatThrownBy{
            postController.getPost(
                groupSequence = setupGroup!!.sequence!!,
                postSequence = setupPost!!.postSequence!!,
                user = groupUser!!
            )
        }.isInstanceOf(PostAccessDeniedException::class.java)
            .hasMessageContaining("보유하지않은 게시글입니다.")
    }

    @Test
    fun getPost_fail_user() {
        val save = userRepository.save(
            userFactory.create(
                (UserDto(
                    id = "test",
                    email = "test@email.com",
                    password = "testest"
                ))
            )
        )
        assertThatThrownBy { postController.getPost(user = save, groupSequence = setupGroup!!.sequence!!, postSequence = setupPost!!.postSequence!!) }
            .isInstanceOf(BadCredentialsException::class.java)
            .hasMessageContaining("Group user does not exist")
    }

}