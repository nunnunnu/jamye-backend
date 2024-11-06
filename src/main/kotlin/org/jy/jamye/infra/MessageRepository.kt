package org.jy.jamye.infra

import org.jy.jamye.domain.model.Message
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository: JpaRepository<Message, Long> {
}