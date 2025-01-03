package org.jy.jamye.infra

import org.jy.jamye.domain.model.Board
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface BoardRepository: JpaRepository<Board, Long> {
    fun findByPostSeq(postSequence: Long): Optional<Board>
}