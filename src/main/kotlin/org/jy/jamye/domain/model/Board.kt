package org.jy.jamye.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pi_bi")
class Board(
    @Column(name = "dtl", columnDefinition = "TEXT")
    var detail: String,
    @Column(name="pi_seq")
    val postSeq: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bi_seq")
    var boardSeq: Long? = null
) {
    fun contentUpdate(content: String) {
        this.detail = content
    }
}