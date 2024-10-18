package org.jy.jamye.infra

import org.jy.jamye.domain.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PostRepository: JpaRepository<Post, Long> {
    fun findByGroupSequenceAndPostSequence(groupSequence: Long, postSequence: Long): Optional<Post>

}
