package org.jy.jamye.infra

import org.jy.jamye.domain.model.Board
import org.springframework.data.jpa.repository.JpaRepository

interface BoardRepository: JpaRepository<Board, Long> {
}