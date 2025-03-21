package org.jy.jamye.infra

import org.jy.jamye.domain.model.MessageImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface MessageImageRepository: JpaRepository<MessageImage, Long> {
    fun findByMessageSeqIn(messageSeqList: Set<Long>): List<MessageImage>
    fun deleteAllByMessageSeqIn(deleteMessage: Set<Long>)
    @Modifying
    @Query(
        """delete from MessageImage where messageSeq IN (
                select pm.messageSeq from Post po
                inner join Message pm ON po.postSeq = pm.postSeq
                where po.groupSeq = :groupSeq
            )
        """)
    fun deleteAllPostInGroup(groupSeq: Long)
    @Modifying
    @Query(
        """delete from MessageImage where messageSeq IN (
                select pm.messageSeq from Message pm
                where pm.postSeq = :postSeq
            )
        """)
    fun deleteByPostSeq(postSeq: Long)
}