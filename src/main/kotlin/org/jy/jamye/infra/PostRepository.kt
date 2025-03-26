package org.jy.jamye.infra

import org.jy.jamye.domain.model.Post
import org.jy.jamye.domain.model.PostType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    @Query("""
        select p
        from Post p
        where
            p.groupSeq = :groupSeq
            and p.postSeq in :postSeqs
            and (:keyword is null or p.title like concat('%', :keyword, '%'))
            and (:#{#postType == null || #postType.isEmpty()} = true OR p.postType IN :postType)
            and (COALESCE(:tags, null) is null or 1=1)
    """)
    fun findByGroupSeqAndPostSeqInAndFilter(
        groupSeq: Long, postSeqs: Set<Long>,
        keyword: String?,
        tags: Set<String>,
        postType: Set<PostType>,
        page: Pageable): Page<Post>

    fun countByGroupSeq(groupSequence: Long): Long

}
