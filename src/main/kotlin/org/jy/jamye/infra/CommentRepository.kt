package org.jy.jamye.infra

import org.jy.jamye.domain.model.Comment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CommentRepository: JpaRepository<Comment, Long> {
    fun findAllByGroupSeqAndPostSeq(groupSeq: Long, postSeq: Long): List<Comment>
    fun findByUserSeqAndCommentSeq(userSeq: Long, commentSeq: Long): Optional<Comment>
    fun deleteByPostSeq(postSeq: Long)

}
