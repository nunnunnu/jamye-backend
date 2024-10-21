package org.jy.jamye.infra

import org.jy.jamye.domain.model.UserGroupPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserGroupPostRepository: JpaRepository<UserGroupPost, Long> {
    fun existsByUserSequenceAndGroupSequenceAndPostSequence(userSequence: Long, groupSequence: Long, postSequence: Long): Boolean
    @Query("SELECT e.postSequence FROM UserGroupPost e WHERE e.groupSequence = :groupSeq AND e.userSequence = :userSeq")
    fun findPostSeqByGroupSequenceAndUserSequence(groupSeq: Long, userSeq: Long): Set<Long>
    fun deleteByGroupSequence(groupSeq: Long)
    fun deleteByGroupSequenceAndUserSequenceIn(groupSeq: Long, agreeUserSeqs: Set<Long>)

}
