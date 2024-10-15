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
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/group")
class GroupController(private val groupService: GroupApplicationService, private val session: HttpSession) {
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
        session.setAttribute(groupInviteCode, groupSeq)
        return ResponseDto(data = groupInviteCode, status = HttpStatus.OK)
    }

    @PostMapping("/invite")
    fun inviteGroupUser(@AuthenticationPrincipal user: UserDetails,
                        @RequestBody data: GroupPostDto.Invite
    ): ResponseDto<Long> {
        val groupSequence: Long? = session.getAttribute(data.inviteCode) as Long?
        if(groupSequence != data.groupSequence) {
            throw IllegalArgumentException("정상적인 초대코드가 아님")
        }
        val userInGroupSequence = groupService.inviteGroupUser(user.username, data)
        return ResponseDto(data = userInGroupSequence, status = HttpStatus.OK)
    }

}