package org.jy.jamye.infra.post

import org.jy.jamye.domain.post.model.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Meta
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface MessageRepository: JpaRepository<Message, Long> {
    fun findAllByPostSeqOrderByOrderNumber(postSequence: Long): List<Message>
    @Modifying
    @Query("""
        delete from Message m where m.postSeq in (
            select p.postSeq from Post p where p.groupSeq = :groupSeq
        )
    """)
    fun deleteAllByGroupSeq(groupSeq: Long)
    @Meta(comment = "게시글 내 메세지 모두 삭제")
    @Transactional
    @Modifying
    @Query("delete from Message m where m.postSeq in (:postSeq)")
    fun deleteByPostSeq(postSeq: Long)
}