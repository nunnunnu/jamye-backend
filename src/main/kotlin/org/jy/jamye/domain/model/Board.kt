package org.jy.jamye.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "pi_bi")
@DiscriminatorValue("BOR")
class Board(
    @Column(name = "dtl")
    var detail: String,
    @Column(name = "pi_seq")
    var postSeq: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bi_seq")
    var boardSeq: Long? = null
) {
}