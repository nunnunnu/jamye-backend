package org.jy.jamye.infra

import org.jy.jamye.domain.model.PostTagConnection
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface PostTagRepository: JpaRepository<PostTagConnection, Long> {
    @EntityGraph(attributePaths = ["tag"])
    fun findByPostSeqIn(toSet: Set<Long>): List<PostTagConnection>
}