package org.jy.jamye.infra

import org.jy.jamye.domain.model.MessageImage
import org.springframework.data.jpa.repository.JpaRepository

interface MessageImageRepository: JpaRepository<MessageImage, Long> {
}