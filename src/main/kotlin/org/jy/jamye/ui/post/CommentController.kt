package org.jy.jamye.ui.post

import jakarta.validation.Valid
import org.jy.jamye.application.post.CommentAppService
import org.jy.jamye.application.post.dto.CommentDto
import org.jy.jamye.common.io.ResponseDto
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/comment")
@Validated
class CommentController(
    private val commentAppService: CommentAppService
) {
    @GetMapping("/{groupSeq}/{postSeq}")
    fun getComment(@AuthenticationPrincipal user: UserDetails,
        @PathVariable("groupSeq") groupSeq: Long,
        @PathVariable("postSeq") postSeq: Long
    ): ResponseDto<List<CommentDto>> {
        val comments = commentAppService.getComment(user.username, groupSeq, postSeq)
        return ResponseDto(data = comments)
    }

    @PostMapping("/{groupSeq}/{postSeq}")
    fun createComment(@AuthenticationPrincipal user: UserDetails,
        @PathVariable("groupSeq") groupSeq: Long,
        @PathVariable("postSeq") postSeq: Long,
        @Valid @RequestBody comment: CommentPostDto
    ): ResponseDto<Long> {
        val createCommentSeq = commentAppService.createComment(user.username, groupSeq, postSeq, comment.comment, comment.replySeq)
        return ResponseDto(data = createCommentSeq)
    }

    @DeleteMapping("/{groupSeq}/{postSeq}/{commentSeq}")
    fun deleteComment(@AuthenticationPrincipal user: UserDetails,
        @PathVariable("groupSeq") groupSeq: Long,
        @PathVariable("postSeq") postSeq: Long,
        @PathVariable("commentSeq") commentSeq: Long
    ) {
        commentAppService.deleteComment(user.username, groupSeq, postSeq, commentSeq)
    }

    @PostMapping("/{groupSeq}/{postSeq}/{commentSeq}")
    fun updateComment(@AuthenticationPrincipal user: UserDetails,
        @PathVariable("groupSeq") groupSeq: Long,
        @PathVariable("postSeq") postSeq: Long,
        @PathVariable("commentSeq") commentSeq: Long,
        @RequestBody comment: CommentPostDto
    ): ResponseDto<Nothing> {
        commentAppService.updateComment(user.username, groupSeq, postSeq, commentSeq, comment.comment)
        return ResponseDto()

    }
}