package org.jy.jamye.infra

import org.jy.jamye.domain.model.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface MessageRepository: JpaRepository<Message, Long> {
    fun findAllByPostSeqOrderByOrderNumber(postSequence: Long): List<Message>
    @Modifying
    @Query("""
        delete from Message m where m.postSeq in (
            select p.postSeq from Post p where p.groupSeq = :groupSeq
        )
    """)
    fun deleteAllByGroupSeq(groupSeq: Long)
    fun deleteByPostSeq(postSeq: Long)
}