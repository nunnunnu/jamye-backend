package org.jy.jamye.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.exception.PostAccessDeniedException
import org.jy.jamye.domain.model.*
import org.jy.jamye.infra.*
import org.jy.jamye.ui.post.PostCreateDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
//@Transactional
class PostControllerTest @Autowired constructor(
    private val postController: PostController,
    private val postRepository: PostRepository,
    private val postFactory: PostFactory,
    private val userFactory: UserFactory,
    private val userRepository: UserRepository,
    private val groupFactory: GroupFactory,
    private val groupRepository: GroupRepository,
    private val groupUserRepository: GroupUserRepository,
    private val userGroupPostRepository: UserGroupPostRepository,
    private val boardRepository: BoardRepository
) {
    private val title = "Test title"
    private var testId = "setupId2"
    private var testEmail = "setupEmail@email.com"
    private val testPassword = "setupPassword"
    private val masterNickname = "masterNickname"
    private var setupUser: User? = null
    private var groupUser: User? = null
    private var setupGroup: Group? = null
    private var setupPost: Post? = null
    @BeforeEach
    @Transactional
    fun init() {
        setupUser = userRepository.save(userFactory.create((UserDto(id = testId, email = testEmail, password = testPassword))))
        groupUser = userRepository.save(userFactory.create((UserDto(id = "testIdtest", email = "setupEmail2@email.com", password = testPassword))))
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
        setupPost = postRepository.save(postFactory.createPost(
            data = PostDto(
                title = title,
                createdUserSequence = setupUser!!.sequence!!,
                groupSequence = setupGroup!!.sequence!!
            ), type = PostType.BOR
        ))

        boardRepository.save(
            postFactory.createPostBoardType(postSeq =  setupPost!!.postSeq!!,
            detailContent = PostDto.BoardPost(content = "test"))
        )

        userGroupPostRepository.save(UserGroupPost(groupSequence = setupGroup!!.sequence!!, userSequence = setupUser!!.sequence!!, postSequence = setupPost!!.postSeq!!))
    }

    @Test
    fun getPostBoard_success() {
        val response = postController.getPost(
            groupSequence = setupGroup!!.sequence!!,
            postSequence = setupPost!!.postSeq!!,
            user = setupUser!!
        )

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val data = response.data!!
        assertThat(data.title).isEqualTo(title)
        assertThat(data.createdUserNickName).isEqualTo(masterNickname)
        assertThat(data.createdUserSequence).isEqualTo(setupUser!!.sequence)
        val content: PostDto.BoardPost = data.content as PostDto.BoardPost
        assertThat(content.content).isEqualTo("test")
    }

    @Test
    fun getPost_fail_access() {
        assertThatThrownBy{
            postController.getPost(
                groupSequence = setupGroup!!.sequence!!,
                postSequence = setupPost!!.postSeq!!,
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
                    id = "test11",
                    email = "test@email.com",
                    password = "testest"
                ))
            )
        )
        assertThatThrownBy { postController.getPost(user = save, groupSequence = setupGroup!!.sequence!!, postSequence = setupPost!!.postSeq!!) }
            .isInstanceOf(BadCredentialsException::class.java)
            .hasMessageContaining("Group user does not exist")
    }

    @Test
    fun createPostBoard_success() {
        val title = "생성테스트"
        val content = "test"
        val response = postController.createPostBoardType(
            user = setupUser!!, data = PostCreateDto<PostCreateDto.Board>(
                title = title,
                content = PostCreateDto.Board(content = content),
                groupSeq = setupGroup!!.sequence!!
            ), imageMap = mutableMapOf()
        )
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull

        val findPost: Post = postRepository.findById(response.data!!).get()
        val detailContent = boardRepository.findById(response.data!!)
        assertThat(findPost.title).isEqualTo(title)
        assertThat(detailContent.get().detail).isEqualTo(content)
    }

    @Test
    fun createPostMessageType() {
        val sendUserNickName = "first"
        val sendUserNickName2 = "two"

        var params: PostCreateDto<MutableMap<Long, PostDto.MessagePost>> =
            PostCreateDto(
                title = title,
                groupSeq = setupGroup!!.sequence!!,
                content = mutableMapOf(
                    2L to PostDto.MessagePost(
                        sendUser = sendUserNickName2,
                        message = mutableListOf(
                            PostDto.MessageSequence(seq = 2L, message = "twoMessageFromSecondUser"),
                            PostDto.MessageSequence(seq = 1L, message = "firstMessageFromSecondUser"),
                            PostDto.MessageSequence(seq = 3L, message = "threeMessageFromSecondUser")
                        ),
                        sendDate = "오전 11:21",
                    ),
                    1L to PostDto.MessagePost(
                            sendUser = sendUserNickName,
                            message = mutableListOf(
                                PostDto.MessageSequence(seq = 3L, message = "threeMessageFromFirstUser"),
                                PostDto.MessageSequence(seq = 2L, message = "twoMessageFromFirstUser"),
                                PostDto.MessageSequence(seq = 1L, message = "firstMessageFromFirstUser"),
                            ),
                            sendDate = "오전 11:20",
                        ),

                    3L to PostDto.MessagePost(
                        message = mutableListOf(
                            PostDto.MessageSequence(seq = 1L, message = "firstMessageFromMyMessage"),
                            PostDto.MessageSequence(seq = 3L, message = "threeMessageFromMyMessage"),
                            PostDto.MessageSequence(seq = 2L, message = "twoMessageFromMyMessage")
                        ),
                        sendDate = "오전 11:22"
                )
            )
        )
        val response = postController.createPostMessageType(user = setupUser!!, data = params, imageMap = mutableMapOf())
        println(response)
    }

}