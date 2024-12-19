package org.jy.jamye.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pi_msg")
class Message(
    @Column(name = "pi_cont")
    var content: String?,
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
    var orderNumber: Long,
    @Column(name="pi_seq")
    val postSeq: Long,
    @OneToMany(fetch = FetchType.LAZY, cascade = [(CascadeType.ALL)])
    @JoinColumn(name="mi_seq")
    val messageImage: Collection<MessageImage>,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mi_seq")
    var messageSeq: Long? = null
) {
    fun update(
        content: String?,
        nickName: String?,
        groupUserSequence: Long?,
        replyTo: String?,
        replyMessage: String?,
        orderNumber: Long?
        ) {
        if (content != null) {
            this.content = content
        }
        if (nickName != null) {
            this.nickName = nickName
        }
        if (groupUserSequence != null) {
            this.groupUserSequence = groupUserSequence
        }
        if (replyTo != null) {
            this.replyTo = replyTo
        }
        if (replyMessage != null) {
            this.replyMessage = replyMessage
        }
        if  (orderNumber != null) {
            this.orderNumber = orderNumber
        }
    }
}