package org.jy.jamye.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pi_bi")
@DiscriminatorValue("BOR")
class Board(
    @Column(name = "dtl")
    var detail: String,
    title: String,
    groupSeq: Long,
    userSeq: Long,
    createDate: LocalDateTime = LocalDateTime.now(),
    updateDate: LocalDateTime = LocalDateTime.now(),
    piType: PostType
): Post(title, groupSeq, userSeq, createDate, updateDate, piType) {
}