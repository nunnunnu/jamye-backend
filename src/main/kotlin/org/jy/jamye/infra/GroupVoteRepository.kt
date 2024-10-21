package org.jy.jamye.infra

import org.jy.jamye.domain.model.GroupVote
import org.springframework.data.jpa.repository.JpaRepository

interface GroupVoteRepository: JpaRepository<GroupVote, Long> {

}
