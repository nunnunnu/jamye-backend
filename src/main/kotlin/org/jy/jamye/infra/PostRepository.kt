package org.jy.jamye.infra

import org.jy.jamye.domain.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface PostRepository: JpaRepository<Post, Long> {
    fun findByGroupSeqAndPostSeq(groupSequence: Long, postSequence: Long): Optional<Post>
    fun findByGroupSeq(groupSequence: Long): List<Post>
    fun deleteByGroupSeq(groupSeq: Long)
    fun deleteByUserSeqInAndGroupSeq(agreeUserSeqs: Set<Long>, groupSeq: Long)
    @Query("""
        SELECT p.postSeq
        FROM Post p
        LEFT JOIN UserGroupPost u ON 
            u.groupSequence = :groupSeq 
            AND u.userSequence = :userSeq 
            AND u.postSequence = p.postSeq
        WHERE u.userPostSequence IS NULL 
            and p.groupSeq = :groupSeq
    """)
    fun countAllByAbleDrawPool(groupSeq: Long, userSeq: Long): MutableList<Long>
    fun existsByGroupSeqAndPostSeqAndUserSeq(groupSeq: Long, postSeq: Long, userSeq: Long): Boolean

}
