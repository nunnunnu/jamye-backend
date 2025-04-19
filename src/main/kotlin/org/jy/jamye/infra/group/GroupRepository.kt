package org.jy.jamye.infra.group

import org.jy.jamye.domain.group.model.Group
import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository: JpaRepository<Group, Long> {
}