package org.jy.jamye.infra

import org.jy.jamye.application.dto.GroupDto
import org.jy.jamye.domain.model.Grade
import org.jy.jamye.domain.model.GroupUser
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional

interface GroupUserRepository: JpaRepository<GroupUser, Long> {
    @EntityGraph(attributePaths = ["group"])
    fun findAllByUserSequence(sequence: Long): List<GroupUser>
    fun findAllByGroupSequence(groupSequence: Long): List<GroupUser>
    fun existsByUserSequenceAndGroupSequence(userSequence: Long, groupSequence: Long): Boolean
    fun existsByGroupSequenceAndNickname(groupSequence: Long, nickName: String): Boolean
    fun existsByUserSequenceAndGroupSequenceAndGrade(userSequence: Long, groupSequence: Long, grade: Grade): Boolean
    fun findByGroupSequenceAndUserSequence(groupSequence: Long, userSequence: Long): Optional<GroupUser>
    fun findByGroupSequenceAndUserSequenceIn(groupSeq: Long, userSeqs: List<Long>): List<GroupUser>
    fun findAllByUserSequenceAndGrade(userSeq: Long, grade: Grade): List<GroupUser>

    @Transactional
    @Modifying
    @Query("delete from GroupUser group where group.groupSequence = :groupSequence")
    fun deleteByGroup(groupSequence: Long)

    @Transactional
    @Modifying
    @Query("""
       UPDATE GroupUser gu
            SET gu.grade = 'MASTER'
            WHERE gu.groupUserSequence IN (:groupUserSeqs) 
    """)
    fun assignMasterToOldestUser(groupUserSeqs: List<Long>)
    fun findByUserSequence(sequence: Long): GroupUser

    @Query("""
            SELECT gu 
            FROM GroupUser gu 
            WHERE gu.createDate = (
                SELECT MIN(g.createDate) 
                FROM GroupUser g 
                WHERE g.groupSequence = gu.groupSequence
            ) 
            AND gu.groupSequence IN :groupSeqs
    """)
    fun findByGroupOldestUser(groupSeqs: List<Long>): List<GroupUser>
    fun countByGroupSequenceAndCreateDateGreaterThan(groupSequence: Long, createDate: LocalDateTime): Long
    @Modifying
    @Transactional
    fun deleteAllByGroupSequence(groupSeq: Long)
    @Modifying
    @Transactional
    fun deleteAllByGroupSequenceAndUserSequenceIn(groupSeq: Long, deleteAgree: Set<Long>)
    @Query("""
       SELECT g.userSequence
       FROM GroupUser g
       WHERE g.groupSequence = :groupSeq
        AND g.userSequence IN (:userSeqs)
        AND g.grade = 'MASTER'
    """)
    fun findGroupMasterSeq(groupSeq: Long, userSeqs: Set<Long>): Long
    @Query("""
        SELECT g.groupSequence as groupSeq, count(g.userSequence) as totalUser
        FROM GroupUser g
        GROUP BY g.groupSequence
    """)
    fun countGroupInUser(map: List<Long>): List<GroupDto.GroupTotalUser>
    fun countByUserSequenceInAndGroupSequence(userSeqs: Set<Long>, groupSequence: Long): Int
    fun countByGroupSequence(groupSeq: Long): Long
    @Modifying
    fun deleteAllByGroupSequenceAndUserSequence(groupSeq: Long, userSeq: Long)
}