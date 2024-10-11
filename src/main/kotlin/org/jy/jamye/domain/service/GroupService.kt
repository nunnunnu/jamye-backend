package org.jy.jamye.domain.service

import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.infra.GroupFactory
import org.jy.jamye.infra.GroupRepository
import org.springframework.stereotype.Service

@Service
class GroupService(
    private val groupRepo: GroupRepository,
    private val groupUserRepo: GroupUserRepository,
    private val groupFactory: GroupFactory
) {
    fun getGroupInUser(userSequence: Long): List<GroupDto> {
        val groupConnection = groupUserRepo.findByUserSequence(userSequence)
        var result = ArrayList<GroupDto>()
        groupConnection.forEach() {
                val group = it.group
                result.add(GroupDto(groupSequence = group.sequence, name = group.name, description = group.description, createDate = group.createDate, updateDate = group.updateDate))
        }
        return result
    }

    fun createGroup(userSequence: Long, data: GroupDto, masterUserInfo: UserInGroupDto.Simple): GroupDto.Detail {
        val group = groupFactory.createGroup(userSequence, data)
        groupRepo.save(group)
        val groupMaster =
            groupFactory.createGroupMasterConnection(group.sequence!!, userSequence, masterUserInfo, group)
        groupUserRepo.save(groupMaster)
        return GroupDto.Detail(groupSequence = group.sequence!!, name = group.name, description = group.description,
            imageUrl = group.imageUrl, createDate = group.createDate, updateDate = group.updateDate,
            users = listOf(UserInGroupDto(
                nickname = groupMaster.nickname,
                imageUrl = groupMaster.imageUrl,
                createDate = groupMaster.createDate,
                updateDate = groupMaster.updateDate,
                grade = groupMaster.grade,
                groupUserSequence = groupMaster.groupUserSequence!!,
                userSequence = groupMaster.userSequence,
                groupSequence = group.sequence!!))
        )
    }

}