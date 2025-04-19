package org.jy.jamye.domain.group.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "gi_vote")
class GroupVote(
    @Column(name = "end_date_time")
    val endDateTime: LocalDateTime = LocalDateTime.now().plusDays(7),
    @Column(name = "gi_seq")
    val groupSeq: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gv_seq")
    var groupVoteSeq: Long? = null

) {

}
