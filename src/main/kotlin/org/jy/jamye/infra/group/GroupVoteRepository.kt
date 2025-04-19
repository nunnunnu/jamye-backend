package org.jy.jamye.infra.group

import org.jy.jamye.domain.group.model.GroupVote
import org.springframework.data.jpa.repository.JpaRepository

interface GroupVoteRepository: JpaRepository<GroupVote, Long> {

}
