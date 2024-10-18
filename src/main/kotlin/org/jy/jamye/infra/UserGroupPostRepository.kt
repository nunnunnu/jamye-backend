package org.jy.jamye.infra

import org.jy.jamye.domain.model.UserGroupPost
import org.springframework.data.jpa.repository.JpaRepository

interface UserGroupPostRepository: JpaRepository<UserGroupPost, Long> {
    fun existsByUserSequenceAndGroupSequenceAndPostSequence(userSequence: Long, groupSequence: Long, postSequence: Long): Boolean

}
