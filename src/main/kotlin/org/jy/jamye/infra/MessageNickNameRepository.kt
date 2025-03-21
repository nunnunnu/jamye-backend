package org.jy.jamye.infra

import org.jy.jamye.domain.model.MessageNickName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface MessageNickNameRepository: JpaRepository<MessageNickName, Long>

{
    fun findAllByPostSeq(postSeq: Long): List<MessageNickName>
    @Modifying
    @Query("""delete from MessageNickName m where m.postSeq in (
            select p.postSeq from Post p where p.groupSeq = :groupSeq
        )
    """)
    fun deleteAllPostInGroup(groupSeq: Long)
    fun countByPostSeq(postSeq: Long): Long
}