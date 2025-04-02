package org.jy.jamye.infra

import org.jy.jamye.domain.model.UserGroupPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Meta
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface UserGroupPostRepository: JpaRepository<UserGroupPost, Long> {
    fun existsByUserSequenceAndGroupSequenceAndPostSequence(userSequence: Long, groupSequence: Long, postSequence: Long): Boolean
    @Query("SELECT e.postSequence FROM UserGroupPost e WHERE e.groupSequence = :groupSeq AND e.userSequence = :userSeq")
    fun findPostSeqByGroupSequenceAndUserSequence(groupSeq: Long, userSeq: Long): Set<Long>
    fun deleteByGroupSequence(groupSeq: Long)
    fun deleteByGroupSequenceAndUserSequenceIn(groupSeq: Long, agreeUserSeqs: Set<Long>)
    fun findAllByPostSequenceAndGroupSequence(postSeq: Long, groupSeq: Long): Set<UserGroupPost>
    @Meta(comment = "삭제한 게시글의 유저 보유내역 삭제")
    @Transactional
    @Modifying
    @Query("delete from UserGroupPost e where e.postSequence = :postSeq")
    fun deleteByPostSequence(postSeq: Long)
}
