package org.jy.jamye.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "pi_bi")
@DiscriminatorValue("BOR")
open class Board(
    @Column(name = "dtl")
    var detail: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bi_seq")
    var boardSeq: Long? = null
) {
}