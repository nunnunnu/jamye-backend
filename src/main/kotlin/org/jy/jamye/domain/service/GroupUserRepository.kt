package org.jy.jamye.domain.service

import org.jy.jamye.domain.model.GroupUser
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface GroupUserRepository: JpaRepository<GroupUser, Long> {
    @EntityGraph(attributePaths = ["group"])
    fun findByUserSequence(sequence: Long): List<GroupUser>
}