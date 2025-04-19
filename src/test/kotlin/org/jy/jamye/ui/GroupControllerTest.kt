package org.jy.jamye.ui

import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.domain.group.model.Group
import org.jy.jamye.domain.user.model.User
import org.jy.jamye.infra.group.GroupFactory
import org.jy.jamye.infra.user.UserFactory
import org.jy.jamye.infra.group.GroupRepository
import org.jy.jamye.infra.user.GroupUserRepository
import org.jy.jamye.infra.user.UserRepository
import org.jy.jamye.ui.group.GroupController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class GroupControllerTest @Autowired constructor(val groupController: GroupController,
                                                 val userRepo: UserRepository,
                                                 val userFactory: UserFactory,
                                                 val groupUserRepository: GroupUserRepository,
                                                 val groupRepository: GroupRepository,
                                                 val groupFactory: GroupFactory,
                                                 val redisClient: RedisClient) {

    private var setupUser: User? = null
    val name = "testGroup"
    val description = "testDescription"
    val masterNickName = "히히"
    var setupGroup: Group? = null
    var setupInviteCode: String? = null
    var inviteCodes = mutableSetOf<String>()

//    @BeforeEach
//    fun setup() {
//        val user = userFactory.create(UserDto(id = "testId", email = "testEmail@email.com", password = "testPassword"))
//
//        setupUser = userRepo.save(user)
//
//        val group = groupFactory.createGroup(userSequence = user.sequence!!, GroupDto(name = name, description = description))
//        setupGroup = groupRepository.save(group)
//
//        val groupUser = groupFactory.createGroupMasterConnection(
//            groupSequence = group.sequence!!,
//            userSequence = user.sequence!!, masterUserInfo = UserInGroupDto.Simple(nickname = masterNickName), group = group
//        )
//        groupUserRepository.save(groupUser)
//
//        setupInviteCode = groupController.inviteGroupCode(user, group.sequence!!).data
//        inviteCodes.add(setupInviteCode!!)
//    }
//
//    @AfterEach
//    fun inviteCodeDelete() {
//        redisClient.deleteKeys(inviteCodes)
//
//
//        val mapper = ObjectMapper()
//        val deleteVoteMap: MutableMap<Long, DeleteVote> = if (redisClient.getValue("deleteVotes").isNullOrBlank()) HashMap()
//        else mapper.readValue(redisClient.getValue("deleteVotes"), object : TypeReference<MutableMap<Long, DeleteVote>>() {})
//        deleteVoteMap.remove(setupGroup!!.sequence)
////        val jsonString = mapper.writeValueAsString(deleteVoteMap)
////        redisClient.setValue("deleteVotes", jsonString)
//
//    }
//
//    @Test
//    fun groups() {
//        val response = groupController.groups(user = setupUser!!)
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        val groups = response.data!!
//
//        assertThat(groups.size).isEqualTo(1)
//        val group = groups[0]
//        assertThat(group).isNotNull
//        assertThat(group.name).isEqualTo(name)
//        assertThat(group.description).isEqualTo(description)
//        assertThat(group.userNickName).isEqualTo(masterNickName)
//    }
//
//    @Test
//    fun createGroup() {
//        val name = "testGroup2"
//        val description = "testDescription2"
//        val masterNickName = "기기"
//        val response = groupController.createGroup(
//            user = setupUser!!,
//            GroupPostDto(name = name, description = description, nickname = masterNickName)
//        )
//
//        assertThat(response.status).isNotEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        val group = response.data!!
//
//        assertThat(group.name).isEqualTo(name)
//        assertThat(group.description).isEqualTo(description)
//        assertThat(group.users).isNotNull
//        val master = group.users!![0]
//        assertThat(master.nickname).isEqualTo(masterNickName)
//    }
//
//    @Test
//    fun getGroup() {
//        val response = groupController.getGroup(setupGroup!!.sequence!!, user = setupUser!!)
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        val group = response.data!!
//        assertThat(group.name).isEqualTo(name)
//        assertThat(group.description).isEqualTo(description)
//        assertThat(group.users).isNotNull
//        val masterInfo = group.users!!.filter { it -> it.grade == Grade.MASTER }
//        assertThat(masterInfo.size).isEqualTo(1)
//        assertThat(masterInfo[0].nickname).isEqualTo(masterNickName)
//    }
//
//    @Test
//    fun getGroup_fail() {
//        val save = userRepo.save(
//            userFactory.create(
//                UserDto(
//                    id = "saveId",
//                    email = "saveEmail@email.com",
//                    password = "testPassword"
//                )
//            )
//        )
//
//        assertThatThrownBy { groupController.getGroup(setupGroup!!.sequence!!, user = save) }
//            .isInstanceOf(MemberNotInGroupException::class.java)
//            .hasMessageContaining("그룹에 존재하지 않는 회원입니다.")
//    }
//
//    @Test
//    fun inviteGroup() {
//        val response = groupController.inviteGroupCode(setupUser!!, setupGroup!!.sequence!!)
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//        inviteCodes.add(response.data!!)
//
//    }
//
//    @Test
//    fun inviteGroup_fail() {
//        assertThatThrownBy { groupController.inviteGroupCode(setupUser!!, 0L)
//        }.isInstanceOf(BadCredentialsException::class.java)
//            .hasMessageContaining("Group user does not exist")
//    }
//
//    @Test
//    fun inviteUser() {
//        val save = userRepo.save(
//            userFactory.create(
//                UserDto(
//                    id = "testId2",
//                    email = "testEmail2@email.com",
//                    password = "testPassword"
//                )
//            )
//        )
//        val response = groupController.inviteGroupUser(
//            save,
//            GroupPostDto.Invite(
//                groupSequence = setupGroup!!.sequence!!,
//                nickName = "ss",
//                inviteCode = setupInviteCode!!
//            )
//        )
//
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//        assertThat(response.data).isNotNull
//    }
//
//    @Test
//    fun inviteUser_fail_nickname() {
//        val save = userRepo.save(
//            userFactory.create(
//                UserDto(
//                    id = "testId3",
//                    email = "testEmail3@email.com",
//                    password = "testPassword"
//                )
//            )
//        )
//        assertThatThrownBy { groupController.inviteGroupUser(save,
//                GroupPostDto.Invite(
//                    groupSequence = setupGroup!!.sequence!!,
//                    nickName = masterNickName,
//                    inviteCode = setupInviteCode!!
//                ))
//        }.isInstanceOf(DuplicateGroupNicknameException::class.java)
//            .hasMessageContaining("이미 그룹에 등록된 닉네임입니다.")
//
//    }
//
//    @Test
//    fun inviteUser_fail_user() {
//        assertThatThrownBy { groupController.inviteGroupUser(setupUser!!,
//                GroupPostDto.Invite(
//                    groupSequence = setupGroup!!.sequence!!,
//                    nickName = "sd",
//                    inviteCode = setupInviteCode!!
//                ))
//        }.isInstanceOf(AlreadyJoinedGroupException::class.java)
//            .hasMessageContaining("이미 그룹에 가입된 상태입니다.")
//
//    }
//
//    @Test
//    fun inviteUser_fail_invite_code() {
//        assertThatThrownBy { groupController.inviteGroupUser(setupUser!!,
//                GroupPostDto.Invite(
//                    groupSequence = setupGroup!!.sequence!!,
//                    nickName = "sld",
//                    inviteCode = "잘못된초대코드"
//                ))
//        }.isInstanceOf(InvalidInviteCodeException::class.java)
//            .hasMessageContaining("존재하지 않는 초대코드입니다.")
//
//    }
//
//    @Test
//    fun deleteGroup_success() {
//        val groupSequence = setupGroup!!.sequence!!
//        val response = groupController.deleteGroup(setupUser!!, groupSequence)
//
//        assertThat(response.status).isEqualTo(HttpStatus.OK)
//    }
//
//    @Test
//    fun deleteGroup_fail_grade() {
//        val groupSequence = setupGroup!!.sequence!!
//        val save = userRepo.save(
//            userFactory.create(
//                UserDto(
//                    id = "testId3",
//                    email = "testEmail3@email.com",
//                    password = "testPassword"
//                )
//            )
//        )
//        groupController.inviteGroupUser(
//            save,
//            GroupPostDto.Invite(
//                groupSequence = groupSequence,
//                nickName = "kk",
//                inviteCode = setupInviteCode!!
//            )
//        )
//        assertThatThrownBy { groupController.deleteGroup(save, groupSequence)
//        }.isInstanceOf(GroupDeletionPermissionException::class.java)
//            .hasMessageContaining("그룹 개설자만 그룹을 삭제 가능합니다.")
//    }
//

}