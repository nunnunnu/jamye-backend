package org.jy.jamye.infra

import org.jy.jamye.domain.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PostRepository: JpaRepository<Post, Long> {
    fun findByGroupSeqAndPostSeq(groupSequence: Long, postSequence: Long): Optional<Post>
    fun findByGroupSeq(groupSequence: Long): List<Post>
    fun deleteByGroupSeq(groupSeq: Long)
    fun deleteByUserSeqInAndGroupSeq(agreeUserSeqs: Set<Long>, groupSeq: Long)

}
