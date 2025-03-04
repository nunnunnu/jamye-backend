package org.jy.jamye.infra

import org.jy.jamye.domain.model.Notify
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface NotifyRepository: JpaRepository<Notify, Long>  {
    fun findAllByUserSeq(userSeq: Long): List<Notify>
    @Modifying
    @Query("""
        delete from Notify n
        where n.createDate <= :standardDate
    """)
    @Transactional
    fun deleteAllByStandardDateBefore(standardDate: LocalDateTime): Int
    fun countByUserSeqAndIsRead(userSeq: Long, isRead: Boolean): Long
    @Modifying
    @Query("""
        update Notify n
        set n.isRead = true
        where n.userSeq = :userSeq
            and n.isRead = false
    """)
    @Transactional
    fun notifyInUserAllRead(userSeq: Long)
    @Transactional
    @Modifying
    @Query("""
        delete from Notify n
        where n.userSeq = :userSeq
        and n.isRead = true
    """)
    fun deleteAllByUserReadNotify(userSeq: Long): Int
    fun deleteByNotiSeqAndUserSeq(notifySeq: Long, userSeq: Long)

}