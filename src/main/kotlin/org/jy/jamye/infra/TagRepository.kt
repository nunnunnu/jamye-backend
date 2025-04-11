package org.jy.jamye.infra

import org.jy.jamye.domain.model.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository: JpaRepository<Tag, Long> {
    fun findByGroupSeq(groupSeq: Long, page: Pageable): Slice<Tag>
    fun findByGroupSeqAndTagNameContains(groupSeq: Long, keyword: String, page: Pageable) : Slice<Tag>
}