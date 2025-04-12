package org.jy.jamye.infra

import org.jy.jamye.domain.model.PostTagConnection
import org.springframework.data.jpa.repository.*
import org.springframework.transaction.annotation.Transactional

interface PostTagRepository: JpaRepository<PostTagConnection, Long> {
    @Meta(comment = "게시글들의 태그 모두 조회(fetch join)")
    @EntityGraph(attributePaths = ["tag"])
    fun findByPostSeqIn(postSeqs: Set<Long>): List<PostTagConnection>
    @Meta(comment = "게시글 연결 태그 삭제")
    @Transactional
    @Modifying
    @Query("delete from PostTagConnection where postSeq=:postSeq")
    fun deleteByPostSeq(postSeq: Long)
    @Meta(comment = "게시글 내 태그 모두 조회(fetch join)")
    @EntityGraph(attributePaths = ["tag"])
    fun findByPostSeq(postSequence: Long): List<PostTagConnection>
    @Meta(comment = "게시글 내 태그 연결 끊기")
    @Modifying
    @Transactional
    fun deleteByPostSeqAndPostTagSeqIn(postSeq: Long, tagDisconnected: Set<Long>)
}