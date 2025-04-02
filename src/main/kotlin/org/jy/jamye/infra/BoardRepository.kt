package org.jy.jamye.infra

import org.jy.jamye.domain.model.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Meta
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
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
    @Meta(comment = "게시글의 board 삭제")
    @Transactional
    @Modifying
    @Query("delete from Board b where b.postSeq = :postSeq")
    fun deleteByPostSeq(postSeq: Long)
}