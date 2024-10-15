package org.jy.jamye.ui

import jakarta.servlet.http.HttpSession
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.domain.model.Grade
import org.jy.jamye.domain.model.Group
import org.jy.jamye.domain.model.User
import org.jy.jamye.infra.GroupUserRepository
import org.jy.jamye.infra.GroupFactory
import org.jy.jamye.infra.GroupRepository
import org.jy.jamye.infra.UserFactory
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class GroupControllerTest @Autowired constructor(val groupController: GroupController,
                                      val userRepo: UserRepository,
                                      val userFactory: UserFactory,
                                      val groupUserRepository: GroupUserRepository,
                                      val groupRepository: GroupRepository,
                                      val groupFactory: GroupFactory) {

    private var setupUser: User? = null
    val name = "testGroup"
    val description = "testDescription"
    val masterNickName = "히히"
    var setupGroup: Group? = null
    var setupInviteCode: String? = null

    @BeforeEach
    fun setup() {
        val user = userFactory.create(UserDto(id = "testId", email = "testEmail@email.com", password = "testPassword"))

        userRepo.save(user)
        setupUser = user

        val group = groupFactory.createGroup(userSequence = user.sequence!!, GroupDto(name = name, description = description))
        groupRepository.save(group)
        setupGroup = group

        val groupUser = groupFactory.createGroupMasterConnection(
            groupSequence = group.sequence!!,
            userSequence = user.sequence!!, masterUserInfo = UserInGroupDto.Simple(nickname = masterNickName), group = group
        )
        groupUserRepository.save(groupUser)

        setupInviteCode = groupController.inviteGroupCode(user, group.sequence!!).data
    }
    @Test
    fun groups() {
        val response = groupController.groups(user = setupUser!!)
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val groups = response.data!!

        assertThat(groups.size).isEqualTo(1)
        val group = groups[0]
        assertThat(group.name).isEqualTo(name)
        assertThat(group.description).isEqualTo(description)
        assertThat(group).isNotNull
    }

    @Test
    fun createGroup() {
        val name = "testGroup2"
        val description = "testDescription2"
        val masterNickName = "기기"
        val response = groupController.createGroup(
            user = setupUser!!,
            GroupPostDto(name = name, description = description, nickname = masterNickName)
        )

        assertThat(response.status).isNotEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val group = response.data!!

        assertThat(group.name).isEqualTo(name)
        assertThat(group.description).isEqualTo(description)
        assertThat(group.users).isNotNull
        val master = group.users!![0]
        assertThat(master.nickname).isEqualTo(masterNickName)
    }

    @Test
    fun getGroup() {
        val response = groupController.getGroup(setupGroup!!.sequence!!, user = setupUser!!)
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
        val group = response.data!!
        assertThat(group.name).isEqualTo(name)
        assertThat(group.description).isEqualTo(description)
        assertThat(group.users).isNotNull
        val masterInfo = group.users!!.filter { it -> it.grade == Grade.MASTER }
        assertThat(masterInfo.size).isEqualTo(1)
        assertThat(masterInfo[0].nickname).isEqualTo(masterNickName)
    }

    @Test
    fun inviteGroup() {
        val response = groupController.inviteGroupCode(setupUser!!, setupGroup!!.sequence!!)
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
    }

    @Test
    fun inviteGroup_fail() {
        assertThatThrownBy {
            assertThat(groupController.inviteGroupCode(setupUser!!, 0L))
        }.isInstanceOf(BadCredentialsException::class.java)
            .hasMessageContaining("Group user does not exist")
    }

    @Test
    fun inviteUser() {
        val save = userRepo.save(
            userFactory.create(
                UserDto(
                    id = "testId2",
                    email = "testEmail2@email.com",
                    password = "testPassword"
                )
            )
        )
        val response = groupController.inviteGroupUser(
            save,
            GroupPostDto.Invite(
                groupSequence = setupGroup!!.sequence!!,
                nickName = "ss",
                inviteCode = setupInviteCode!!
            )
        )

        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.data).isNotNull
    }

    @Test
    fun inviteUser_fail_nickname() {
        val save = userRepo.save(
            userFactory.create(
                UserDto(
                    id = "testId3",
                    email = "testEmail3@email.com",
                    password = "testPassword"
                )
            )
        )
        assertThatThrownBy {
            assertThat(groupController.inviteGroupUser(save,
                GroupPostDto.Invite(
                    groupSequence = setupGroup!!.sequence!!,
                    nickName = masterNickName,
                    inviteCode = setupInviteCode!!
                )))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Group nickname already exists")

    }

    @Test
    fun inviteUser_fail_user() {
        assertThatThrownBy {
            assertThat(groupController.inviteGroupUser(setupUser!!,
                GroupPostDto.Invite(
                    groupSequence = setupGroup!!.sequence!!,
                    nickName = "sd",
                    inviteCode = setupInviteCode!!
                )))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Group user already exists")

    }

    @Test
    fun inviteUser_fail_invite_code() {
        assertThatThrownBy {
            assertThat(groupController.inviteGroupUser(setupUser!!,
                GroupPostDto.Invite(
                    groupSequence = setupGroup!!.sequence!!,
                    nickName = "sld",
                    inviteCode = "잘못된초대코드"
                )))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("정상적인 초대코드가 아님")

    }
}