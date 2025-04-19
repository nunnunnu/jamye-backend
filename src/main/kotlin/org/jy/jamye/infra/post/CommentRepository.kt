package org.jy.jamye.infra.post

import org.jy.jamye.domain.post.model.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Meta
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

interface CommentRepository: JpaRepository<Comment, Long> {
    fun findAllByGroupSeqAndPostSeq(groupSeq: Long, postSeq: Long): List<Comment>
    fun findByUserSeqAndCommentSeq(userSeq: Long, commentSeq: Long): Optional<Comment>
    @Meta(comment = "게시글 내 댓글 모두 삭제")
    @Transactional
    @Modifying
    @Query("delete from Comment c where c.postSeq = :postSeq")
    fun deleteByPostSeq(postSeq: Long)

}
