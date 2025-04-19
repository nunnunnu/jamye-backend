package org.jy.jamye.infra.group

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.common.exception.MemberNotInGroupException
import org.jy.jamye.domain.group.model.Group
import org.jy.jamye.infra.user.GroupUserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class GroupReader(
    private val groupRepo: GroupRepository,
    private val groupUserRepo: GroupUserRepository
    ) {
    val log: Logger = LoggerFactory.getLogger(GroupReader::class.java)
    @Cacheable(cacheNames = ["groupCache"], key = "#groupSequence")
    fun findByIdOrThrow(groupSequence: Long): Group {
        log.info("---DB에서 조회함---")
        return groupRepo.findById(groupSequence).orElseThrow { throw EntityNotFoundException() }
    }

    @Cacheable(cacheNames = ["groupExistCache"], key = "#groupSeq+#userSeq")
    fun userInGroupCheckOrThrow(userSeq: Long, groupSeq: Long) {
        if (!groupUserRepo.existsByUserSequenceAndGroupSequence(userSeq, groupSeq)) {
            throw MemberNotInGroupException()
        }
    }
}