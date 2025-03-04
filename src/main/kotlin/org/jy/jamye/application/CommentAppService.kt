package org.jy.jamye.application

import org.jy.jamye.application.dto.CommentDto
import org.jy.jamye.domain.service.CommentService
import org.jy.jamye.domain.service.PostService
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.infra.CommentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentAppService(
    private val commentService: CommentService,
    private val postService: PostService,
    private val userService: UserService,
    private val commentRepository: CommentRepository
) {
    fun getComment(userId: String, groupSeq: Long, postSeq: Long): List<CommentDto> {
        val user = userService.getUser(userId)
        postService.postCheck(groupSeq, postSeq, user.sequence!!)
        return commentService.getComment(groupSeq, postSeq)
    }

    fun createComment(userId: String, groupSeq: Long, postSeq: Long, comment: String): Long {
        val user = userService.getUser(userId)
        postService.postCheck(groupSeq, postSeq, user.sequence!!)
        return commentService.createComment(user.sequence, groupSeq, postSeq, comment)
    }

    @Transactional
    fun deleteComment(userId: String, groupSeq: Long, postSeq: Long, commentSeq: Long) {
        val user = userService.getUser(userId)
        postService.postCheck(groupSeq, postSeq, user.sequence!!)
        commentRepository.deleteById(commentSeq)
    }

    fun updateComment(userId: String, groupSeq: Long, postSeq: Long, commentSeq: Long, comment: String) {
        val user = userService.getUser(userId)
        postService.postCheck(groupSeq, postSeq, user.sequence!!)
        commentService.updateComment(groupSeq = groupSeq, postSeq = postSeq, userSeq = user.sequence, commentSeq = commentSeq, commentText = comment)
    }

}
