package org.jy.jamye.infra

import org.jy.jamye.domain.model.MessageNickName
import org.springframework.data.jpa.repository.JpaRepository

interface MessageNickNameRepository: JpaRepository<MessageNickName, Long>

{
    fun findAllByPostSeq(postSeq: Long): List<MessageNickName>
}