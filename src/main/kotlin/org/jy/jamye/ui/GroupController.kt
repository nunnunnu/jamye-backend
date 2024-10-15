package org.jy.jamye.ui

import jakarta.servlet.http.HttpSession
import org.jy.jamye.application.GroupApplicationService
import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.application.dto.UserInGroupDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/group")
class GroupController(private val groupService: GroupApplicationService,private val session: HttpSession) {
    @GetMapping("/list")
    fun groups(@AuthenticationPrincipal user: UserDetails) :  ResponseDto<List<GroupDto>> {
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
        // redis 구현 전 임시 세션 저장
        session.setAttribute("inviteCode", groupInviteCode)
        return ResponseDto(data = groupInviteCode, status = HttpStatus.OK)
    }
}