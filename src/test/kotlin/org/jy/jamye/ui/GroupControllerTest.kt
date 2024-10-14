package org.jy.jamye.ui

import org.assertj.core.api.Assertions.assertThat
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
        val masterInfo = group.users!!.filter { it -> it.grade.equals(Grade.MASTER) }
        assertThat(masterInfo.size).isEqualTo(1)
        assertThat(masterInfo[0].nickname).isEqualTo(masterNickName)
    }

}