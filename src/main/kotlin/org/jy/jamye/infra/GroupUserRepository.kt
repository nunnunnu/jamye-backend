package org.jy.jamye.infra

import org.jy.jamye.domain.model.GroupUser
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface GroupUserRepository: JpaRepository<GroupUser, Long> {
    @EntityGraph(attributePaths = ["group"])
    fun findByUserSequence(sequence: Long): List<GroupUser>
    fun findByGroupSequence(groupSequence: Long): List<GroupUser>
    fun existsByUserSequenceAndGroupSequence(userSequence: Long, groupSequence: Long): Boolean
    fun existsByGroupSequenceAndNickname(groupSequence: Long, nickName: String): Boolean
}