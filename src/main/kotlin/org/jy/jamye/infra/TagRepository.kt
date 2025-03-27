package org.jy.jamye.infra

import org.jy.jamye.domain.model.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository: JpaRepository<Tag, Long> {
    fun findByGroupSeq(groupSeq: Long): List<Tag>
    fun findByGroupSeqAndTagNameContains(groupSeq: Long, keyword: String) : List<Tag>
}