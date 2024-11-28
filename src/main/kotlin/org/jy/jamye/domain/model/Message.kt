package org.jy.jamye.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pi_msg")
class Message(
    @Column(name = "pi_cont")
    val content: String?,
    @Column(name = "nickName")
    var nickName: String?,
    @Column(name = "gu_seq")
    var groupUserSequence: Long?,
    @Column(name = "send_date")
    var sendDate: String? = null,
    @Column(name = "replyTo")
    var replyTo: String? = null,
    @Column(name = "reply_message")
    var replyMessage: String? = null,
    @Column(name="num")
    val orderNumber: Long,
    @Column(name="pi_seq")
    val postSeq: Long,
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="mi_seq")
    val messageImage: Collection<MessageImage>,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mi_seq")
    var messageSeq: Long? = null
) {
}