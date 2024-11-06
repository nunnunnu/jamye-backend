package org.jy.jamye.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pi_msg")
@DiscriminatorValue("MSG")
open class Message(
    @Column(name = "pi_cont")
    var content: String,
    @Column(name = "nickName")
    var nickName: String?,
    @Column(name = "gu_seq")
    var groupUserSequence: Long?,
    @Column(name = "send_date")
    var sendDate: LocalDateTime? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mi_seq")
    var messageSeq: Long? = null
) {
}