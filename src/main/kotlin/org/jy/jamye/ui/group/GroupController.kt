package org.jy.jamye.ui.group

import org.jy.jamye.application.group.GroupApplicationService
import org.jy.jamye.application.group.DeleteVote
import org.jy.jamye.application.group.GroupDto
import org.jy.jamye.application.post.dto.PostDto
import org.jy.jamye.application.user.dto.UserInGroupDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.group.service.GroupService
import org.jy.jamye.domain.post.service.VisionService
import org.jy.jamye.ui.post.GroupPostDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/group")
class GroupController(private val groupAppService: GroupApplicationService,
    private val groupService: GroupService,
    private val visionService: VisionService
) {
    @GetMapping("/list")
    fun groups(@AuthenticationPrincipal user: UserDetails) :  ResponseDto<List<GroupDto.UserInfo>> {
        val groups = groupAppService.getGroupsInUser(user.username)
        return ResponseDto(data = groups)
    }

    @PostMapping
    fun createGroup(
        @AuthenticationPrincipal user: UserDetails,
        @RequestPart data: GroupPostDto,
        @RequestPart profileImageUrl: MultipartFile? = null,
        @RequestPart imageUrl: MultipartFile? = null
    ): ResponseDto<GroupDto.Detail> {
        val profileFileUri = profileImageUrl?.let { visionService.saveFile(it) }
        val groupImageUri = imageUrl?.let { visionService.saveFile(it) }
        val result = groupAppService.createGroup(
            user.username,
            GroupDto(name = data.name, description = data.description, imageUrl = groupImageUri),
            UserInGroupDto.Simple(nickname = data.nickname, imageUrl = profileFileUri)
        )
        return ResponseDto(data = result, status = HttpStatus.CREATED)
    }

    @GetMapping("/{groupSeq}")
    fun getGroup(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<GroupDto.Detail> {
        val result = groupAppService.getGroup(user.username, groupSeq)
        return ResponseDto(data = result)
    }

    @GetMapping("/name/{groupSeq}")
    fun getGroupName(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<GroupDto> {
        val result = groupAppService.getGroupSimple(user.username, groupSeq)
        return ResponseDto(data = result)
    }

    @GetMapping("/invite/{groupSeq}")
    fun inviteGroupCode(@AuthenticationPrincipal user: UserDetails, @PathVariable groupSeq: Long): ResponseDto<String> {
        val groupInviteCode: String = groupAppService.inviteGroupCode(user.username, groupSeq)
        return ResponseDto(data = groupInviteCode)
    }

    @PostMapping("/invite")
    fun inviteGroupUser(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody data: GroupPostDto.Invite,
    ): ResponseDto<Long> {

        val userInGroupSequence = groupAppService.inviteGroupUser(user.username, data)
        return ResponseDto(data = userInGroupSequence)
    }

    @GetMapping("/group-info/{inviteCode}")
    fun inviteGroupInfo(@AuthenticationPrincipal user: UserDetails, @PathVariable inviteCode: String): ResponseDto<GroupDto> {
        val group = groupAppService.getInviteGroup(user.username, inviteCode)
        return ResponseDto(data = group)
    }


    @DeleteMapping("/{groupSeq}")
    fun deleteGroup(@AuthenticationPrincipal user: UserDetails, @PathVariable("groupSeq") groupSeq: Long): ResponseDto<Boolean> {
        val result = groupAppService.deleteGroupWithResult(user.username, groupSeq)
        return ResponseDto(data = result)
    }

    @GetMapping("/{groupSeq}/nick-name")
    fun duplicateNickName(@AuthenticationPrincipal user: UserDetails, @PathVariable("groupSeq") groupSeq: Long, @RequestParam nickName: String)
            : ResponseDto<Nothing> {
        groupService.nickNameDuplicateCheck(groupSeq, nickName = nickName)
        return ResponseDto()
    }

    @PostMapping("/vote/{type}/{groupSeq}")
    fun groupDeleteVote(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable("type") type: DeleteVote.VoteType,
        @PathVariable("groupSeq") groupSeq: Long,
    )
            : ResponseDto<Nothing> {
        groupAppService.deleteGroupVote(user.username, type, groupSeq)
        return ResponseDto()
    }

    @GetMapping("/users/{groupSeq}")
    fun getAllUsersInGroup(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<List<UserInGroupDto>> {
        val usersInGroup = groupAppService.getUsersInGroup(groupSeq, user.username)
        return ResponseDto(data = usersInGroup)
    }

    @GetMapping("/user/{groupSeq}")
    fun getUsersInGroup(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<UserInGroupDto> {
        val usersInGroup = groupAppService.getUserInGroup(groupSeq, user.username)
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
        groupAppService.updateUserInGroupInfo(groupSeq, userInGroupSeq, nickName, profile, user.username)
        return ResponseDto()
    }

    @PostMapping("/leave/{groupSeq}")
    fun leaveGroup(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails)
            : ResponseDto<Nothing> {
        groupAppService.leaveGroup(groupSeq, user.username)
        return ResponseDto()
    }

    @GetMapping("/vote-info/{groupSeq}")
    fun isGroupDeletionVoteInProgress(@PathVariable("groupSeq") groupSeq: Long,
        @AuthenticationPrincipal user: UserDetails): ResponseDto<DeleteVote.Detail> {
        val voteInfo = groupAppService.isGroupDeletionVoteInProgress(groupSeq, user.username)
        return ResponseDto(data = voteInfo)
    }

    @PostMapping("/{groupSeq}")
    fun updateGroupInfo(@AuthenticationPrincipal user: UserDetails,
        @PathVariable("groupSeq") groupSeq: Long,
        @RequestPart data: GroupPostDto.Update,
        @RequestPart file: MultipartFile?) : ResponseDto<GroupDto>{
        val saveFile = file?.let { visionService.saveFile(it) }
        val group = groupAppService.updateGroupInfo(user.username, groupSeq, data, saveFile)
        return ResponseDto(data = group)
    }

    @GetMapping("/{groupSeq}/delete-vote/check")
    fun isDeletionVoteInProgress(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<DeleteVote.Detail> {
        val deletionVoteInProgress = groupAppService.isDeletionVoteInProgress(groupSeq, user.username)
        return ResponseDto(data = deletionVoteInProgress)
    }

    @GetMapping("/{groupSeq}/all-post/count")
    fun getAllPostCountInGroup(
        @PathVariable("groupSeq") groupSeq: Long,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseDto<PostDto.Count> {
        val allPostCountInGroup = groupAppService.getAllPostCountInGroup(groupSeq, user.username)
        return ResponseDto(data = allPostCountInGroup)
    }

    @GetMapping("/all/delete-vote-info")
    fun getAllDeleteVoteIngGroup(@AuthenticationPrincipal user: UserDetails): ResponseDto<Map<Long, DeleteVote.Detail>> {
        val deleteVote = groupAppService.getDeleteVoteInMyGroup(user.username)
        return ResponseDto(data = deleteVote)
    }

    @PostMapping("/{groupSeq}/panhandling")
    fun jamyePanhandlingAlarm(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails) {
        groupAppService.jamyePanhandlingAlarm(groupSeq, user.username);
    }
}