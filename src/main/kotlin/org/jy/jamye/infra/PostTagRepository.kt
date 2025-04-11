package org.jy.jamye.infra

import org.jy.jamye.domain.model.PostTagConnection
import org.springframework.data.jpa.repository.*
import org.springframework.transaction.annotation.Transactional

interface PostTagRepository: JpaRepository<PostTagConnection, Long> {
    @EntityGraph(attributePaths = ["tag"])
    fun findByPostSeqIn(toSet: Set<Long>): List<PostTagConnection>
    @Meta(comment = "게시글 연결 태그 삭제")
    @Transactional
    @Modifying
    @Query("delete from PostTagConnection where postSeq=:postSeq")
    fun deleteByPostSeq(postSeq: Long)
}