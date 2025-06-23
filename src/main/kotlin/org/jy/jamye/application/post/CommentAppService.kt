package org.jy.jamye.application.post

import org.jy.jamye.application.post.dto.CommentDto
import org.jy.jamye.common.listener.NotifyInfo
import org.jy.jamye.domain.post.service.CommentService
import org.jy.jamye.domain.group.service.GroupService
import org.jy.jamye.domain.post.service.PostService
import org.jy.jamye.domain.user.service.UserService
import org.jy.jamye.infra.post.CommentRepository
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
        val replySeqs = comments.filter { it.replySeq != null }.map { it.replySeq!! }.toSet()
        val replyUserNicknamesMap =
            groupService.getGroupInUsersNickName(groupSeq, replySeqs)
        comments.forEach {
            it.nickName = usersInGroup[it.userSeq]
            it.replyUserNickname = replyUserNicknamesMap[it.replySeq]
        }
        return comments
    }

    fun createComment(userId: String, groupSeq: Long, postSeq: Long, comment: String, replySeq: Long?): Long {
        val user = userService.getUser(userId)
        postService.postCheck(groupSeq, postSeq, user.sequence!!)
        replySeq?.let {
            groupService.checkUserInGroupByGroupUserSeq(groupSeq, it)
        }
        val postTitle = postService.getPostTitle(groupSeq, postSeq)
        val groupInfo = groupService.getGroupSimpleInfo(groupSeq)
        val event = NotifyInfo(
            groupSeq = groupSeq,
            postSeq = postSeq,
            userSeqs = if (replySeq != null) setOf(postTitle.createdUserSequence!!, replySeq) else setOf(
                postTitle.createdUserSequence!!
            ),
            title = "${groupInfo.name}: ${postTitle.title}",
            message = "댓글이 등록되었습니다"
        )
        publisher.publishEvent(event)
        return commentService.createComment(user.sequence, groupSeq, postSeq, comment, replySeq)
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
