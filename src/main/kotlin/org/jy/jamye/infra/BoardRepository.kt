package org.jy.jamye.infra

import org.jy.jamye.domain.model.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface BoardRepository: JpaRepository<Board, Long> {
    fun findByPostSeq(postSequence: Long): Optional<Board>
    @Modifying
    @Query("""    
        delete from Board b where b.postSeq in (
            select p.postSeq from Post p where p.groupSeq = :groupSeq
        ) 
    """)
    fun deleteAllByGroupSeq(groupSeq: Long)
    fun deleteByPostSeq(postSeq: Long)
}