package org.jy.jamye.infra

import org.jy.jamye.domain.model.Notify
import org.springframework.data.jpa.repository.JpaRepository

interface NotifyRepository: JpaRepository<Notify, Long>  {
    fun findAllByUserSeq(userSeq: Long): List<Notify>

}