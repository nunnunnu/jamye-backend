package org.jy.jamye.infra.post

import org.jy.jamye.infra.user.UserGroupPostRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PostReader(private val userGroupPostRepository: UserGroupPostRepository) {
    @Cacheable(cacheNames = ["postExistCache"], key = "#groupSeq+#postSeq+#userSeq")
    fun postCheck(userSeq: Long, groupSeq: Long, postSeq: Long): Boolean {
        return userGroupPostRepository.existsByUserSequenceAndGroupSequenceAndPostSequence(userSeq, groupSeq, postSeq)
    }
}