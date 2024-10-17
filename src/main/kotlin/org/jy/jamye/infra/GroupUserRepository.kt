package org.jy.jamye.infra

import org.jy.jamye.domain.model.Grade
import org.jy.jamye.domain.model.GroupUser
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

interface GroupUserRepository: JpaRepository<GroupUser, Long> {
    @EntityGraph(attributePaths = ["group"])
    fun findAllByUserSequence(sequence: Long): List<GroupUser>
    fun findAllByGroupSequence(groupSequence: Long): List<GroupUser>
    fun existsByUserSequenceAndGroupSequence(userSequence: Long, groupSequence: Long): Boolean
    fun existsByGroupSequenceAndNickname(groupSequence: Long, nickName: String): Boolean
    fun existsByUserSequenceAndGroupSequenceAndGrade(userSequence: Long, groupSequence: Long, grade: Grade): Boolean

    @Transactional
    @Modifying
    @Query("delete from GroupUser group where group.groupSequence = :groupSequence")
    fun deleteByGroup(groupSequence: Long)

    fun findByGroupSequenceAndUserSequence(groupSequence: Long, userSequence: Long): Optional<GroupUser>
}