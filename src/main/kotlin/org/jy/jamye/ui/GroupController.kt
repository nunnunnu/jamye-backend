package org.jy.jamye.ui

import org.jy.jamye.application.GroupApplicationService
import org.jy.jamye.application.dto.DeleteVote
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/group")
class GroupController(private val groupService: GroupApplicationService) {
    @GetMapping("/list")
    fun groups(@AuthenticationPrincipal user: UserDetails) :  ResponseDto<List<GroupDto.UserInfo>> {
        val groups = groupService.getGroupsInUser(user.username)
        return ResponseDto(data = groups, status = HttpStatus.OK)
    }

    @PostMapping
    fun createGroup(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody data: GroupPostDto
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
        return ResponseDto(data = result, status = HttpStatus.OK)
    }

    @PostMapping("/{groupSeq}/invite")
    fun inviteGroupCode(@AuthenticationPrincipal user: UserDetails, @PathVariable groupSeq: Long): ResponseDto<String> {
        val groupInviteCode: String = groupService.inviteGroupCode(user.username, groupSeq)
        return ResponseDto(data = groupInviteCode, status = HttpStatus.OK)
    }

    @PostMapping("/invite")
    fun inviteGroupUser(@AuthenticationPrincipal user: UserDetails,
                        @RequestBody data: GroupPostDto.Invite
    ): ResponseDto<Long> {

        val userInGroupSequence = groupService.inviteGroupUser(user.username, data)
        return ResponseDto(data = userInGroupSequence, status = HttpStatus.OK)
    }

    @DeleteMapping("/{groupSeq}")
    fun deleteGroup(@AuthenticationPrincipal user: UserDetails, @PathVariable("groupSeq") groupSeq: Long): ResponseDto<Nothing> {
        groupService.deleteGroup(user.username, groupSeq)

        return ResponseDto(status = HttpStatus.OK)
    }

    @PostMapping("/vote/{type}/{groupSeq}")
    fun groupDeleteVote(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable("type") type: String,
        @PathVariable("groupSeq") groupSeq: Long)
    : ResponseDto<Nothing> {
        groupService.deleteGroupVote(user.username, type, groupSeq)
        return ResponseDto(status = HttpStatus.OK)
    }

}