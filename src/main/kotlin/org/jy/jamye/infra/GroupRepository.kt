package org.jy.jamye.infra

import org.jy.jamye.domain.model.Group
import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository: JpaRepository<Group, Long> {
}