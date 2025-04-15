package org.jy.jamye.application

import org.jy.jamye.application.dto.CommentDto
import org.jy.jamye.common.listener.NotifyInfo
import org.jy.jamye.domain.service.CommentService
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.PostService
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.infra.CommentRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentAppService(
    private val commentService: CommentService,
    private val postService: PostService,
    private val userService: UserService,
    private val groupService: GroupService,
    private val commentRepository: CommentRepository,
    private val publisher: ApplicationEventPublisher
) {
    fun getComment(userId: String, groupSeq: Long, postSeq: Long): List<CommentDto> {
        val user = userService.getUser(userId)
        postService.postCheck(groupSeq, postSeq, user.sequence!!)
        val usersInGroup =
            groupService.getUsersInGroup(groupSeq, user.sequence).associate { it.userSequence to it.nickname }
        val comments = commentService.getComment(groupSeq, postSeq)
        comments.forEach { it.nickName = usersInGroup[it.userSeq] }
        return comments
    }

    fun createComment(userId: String, groupSeq: Long, postSeq: Long, comment: String): Long {
        val user = userService.getUser(userId)
        postService.postCheck(groupSeq, postSeq, user.sequence!!)
        val postTitle = postService.getPostTitle(groupSeq, postSeq)
        val groupInfo = groupService.getGroupSimpleInfo(groupSeq)
        val event = NotifyInfo(
            groupSeq = groupSeq,
            postSeq = postSeq,
            userSeqs = setOf(postTitle.createdUserSequence!!),
            message = "${groupInfo.name}: ${postTitle.title} - 댓글이 등록되었습니다"
        )
        publisher.publishEvent(event)
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
