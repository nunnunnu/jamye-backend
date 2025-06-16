package org.jy.jamye.domain.post.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.post.dto.CommentDto
import org.jy.jamye.domain.post.model.Comment
import org.jy.jamye.infra.post.CommentRepository
import org.jy.jamye.infra.post.CommentFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val commentFactory: CommentFactory
) {
    @Transactional(readOnly = true)
    fun getComment(groupSeq: Long, postSeq: Long): List<CommentDto> {
        val comments = commentRepository.findAllByGroupSeqAndPostSeq(groupSeq, postSeq)
        return comments.map { CommentDto(
            comment = it.comment,
                    groupSeq = it.groupSeq,
                    postSeq = it.postSeq,
                    userSeq = it.userSeq,
                    createDate = it.createDate,
                    updateDate = it.updateDate,
                    commentSeq = it.commentSeq
        ) }.toList()
    }

    @Transactional
    fun createComment(userSeq: Long, groupSeq: Long, postSeq: Long, commentText: String): Long {
        val comment = commentFactory.createComment(userSeq, groupSeq, postSeq, commentText)
        commentRepository.save(comment)
        return comment.commentSeq!!
    }

    @Transactional
    fun updateComment(groupSeq: Long, postSeq: Long, userSeq: Long, commentText: String, commentSeq: Long) {
        val comment = getCommentOrThrow(commentSeq, userSeq)
        comment.update(commentText)
        commentRepository.save(comment)
    }

    private fun getCommentOrThrow(commentSeq: Long, userSeq: Long): Comment {
        return commentRepository.findByUserSeqAndCommentSeq(userSeq, commentSeq)?: throw EntityNotFoundException("해당 댓글에 접근할 수 없습니다.")
    }

    @Transactional
    fun deleteCommentByPost(postSeq: Long) {
        commentRepository.deleteByPostSeq(postSeq)
    }

}
