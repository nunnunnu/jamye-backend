package org.jy.jamye.infra.post

import org.jy.jamye.domain.post.model.Post
import org.jy.jamye.domain.post.model.PostType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Meta
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

    @Meta(comment = "jamye-list")
    @Query("""
        SELECT p FROM Post p
        WHERE
            p.groupSeq = :groupSeq
            AND p.postSeq IN :postSeqs
            AND (:keyword is null OR p.title LIKE concat('%', :keyword, '%'))
            AND (:#{#postType == null || #postType.isEmpty()} = true OR p.postType IN :postType)
            AND (
                :#{#tags == null || #tags.isEmpty()} = true OR 
                EXISTS (
                    SELECT 1 FROM PostTagConnection t 
                    WHERE t.postSeq = p.postSeq AND t.tagSeq IN :tags
                )
            )
    """)
    fun findByGroupSeqAndPostSeqInAndFilter(
        groupSeq: Long, postSeqs: Set<Long>,
        keyword: String?,
        tags: Set<Long>,
        postType: Set<PostType>,
        page: Pageable): Page<Post>

    fun countByGroupSeq(groupSequence: Long): Long

}
