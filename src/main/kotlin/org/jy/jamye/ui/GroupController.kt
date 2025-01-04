package org.jy.jamye.ui

import org.jy.jamye.application.GroupApplicationService
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.zip.ZipFile

@RestController
@RequestMapping("/api/group")
class GroupController(private val groupService: GroupApplicationService) {
    @GetMapping("/list")
    fun groups(@AuthenticationPrincipal user: UserDetails) :  ResponseDto<List<GroupDto.UserInfo>> {
        val groups = groupService.getGroupsInUser(user.username)
        return ResponseDto(data = groups)
    }

    @PostMapping
    fun createGroup(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody data: GroupPostDto,
    ): ResponseDto<GroupDto.Detail> {
        val result = groupService.createGroup(
            user.username,
            GroupDto(name = data.name, description = data.description, imageUrl = data.imageUrl),
            UserInGroupDto.Simple(nickname = data.nickname, imageUrl = data.profileImageUrl)
        )
        return ResponseDto(data = result, status = HttpStatus.CREATED)
    }

    @GetMapping("/{groupSeq}")
    fun getGroup(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<GroupDto.Detail> {
        val result = groupService.getGroup(user.username, groupSeq)
        return ResponseDto(data = result)
    }

    @GetMapping("/invite/{groupSeq}")
    fun inviteGroupCode(@AuthenticationPrincipal user: UserDetails, @PathVariable groupSeq: Long): ResponseDto<String> {
        val groupInviteCode: String = groupService.inviteGroupCode(user.username, groupSeq)
        return ResponseDto(data = groupInviteCode)
    }

    @PostMapping("/invite")
    fun inviteGroupUser(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody data: GroupPostDto.Invite,
    ): ResponseDto<Long> {

        val userInGroupSequence = groupService.inviteGroupUser(user.username, data)
        return ResponseDto(data = userInGroupSequence)
    }

    @GetMapping("/group-info/{inviteCode}")
    fun inviteGroupInfo(@AuthenticationPrincipal user: UserDetails, @PathVariable inviteCode: String): ResponseDto<GroupDto> {
        val group = groupService.getInviteGroup(user.username, inviteCode)
        return ResponseDto(data = group)
    }


    @DeleteMapping("/{groupSeq}")
    fun deleteGroup(@AuthenticationPrincipal user: UserDetails, @PathVariable("groupSeq") groupSeq: Long): ResponseDto<Nothing> {
        groupService.deleteGroup(user.username, groupSeq)

        return ResponseDto()
    }

    @PostMapping("/vote/{type}/{groupSeq}")
    fun groupDeleteVote(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable("type") type: String,
        @PathVariable("groupSeq") groupSeq: Long,
    )
    : ResponseDto<Nothing> {
        groupService.deleteGroupVote(user.username, type, groupSeq)
        return ResponseDto()
    }

    @GetMapping("/users/{groupSeq}")
    fun getAllUsersInGroup(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<List<UserInGroupDto>> {
        val usersInGroup = groupService.getUsersInGroup(groupSeq, user.username)
        return ResponseDto(data = usersInGroup)
    }

    @GetMapping("/user/{groupSeq}")
    fun getUsersInGroup(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<UserInGroupDto> {
        val usersInGroup = groupService.getUserInGroup(groupSeq, user.username)
        return ResponseDto(data = usersInGroup)
    }

    @PostMapping("/{groupSeq}/{userInGroupSeq}")
    fun updateUserInGroupInfo(
        @PathVariable("groupSeq") groupSeq: Long,
        @PathVariable("userInGroupSeq") userInGroupSeq: Long,
        @RequestParam("nickName") nickName: String?,
        @RequestParam("profile") profile: MultipartFile?,
        @AuthenticationPrincipal user: UserDetails,
    )
    : ResponseDto<Nothing> {
        groupService.updateUserInGroupInfo(groupSeq, userInGroupSeq, nickName, profile, user.username)
        return ResponseDto()
    }
}