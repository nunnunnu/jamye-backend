package org.jy.jamye.domain.post.model

import jakarta.persistence.*

@Entity
@Table(name = "pi_msg")
class Message(
    @Column(name = "pi_cont")
    var content: String?,
    @Column(name = "msg_name_seq")
    var messageNickNameSeq: Long?,
    @Column(name = "send_date")
    var sendDate: String? = null,
    @Column(name = "replyTo")
    var replyTo: Long? = null,
    @Column(name = "reply_message")
    var replyMessage: String? = null,
    @Column(name = "reply_seq")
    var replyToMessageSeq: Long? = null,
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
        messageNickNameSeq: Long?,
        replyNickNameSeq: Long? = null,
        replyMessage: String? = null,
        replyToMessageSeq: Long?,
        orderNumber: Long?,
        sendUserSeq: Long?
    ) {
        if (content != null) {
            this.content = content
        }
        if (messageNickNameSeq != null) {
            this.messageNickNameSeq = messageNickNameSeq
        }
        if  (orderNumber != null) {
            this.orderNumber = orderNumber
        }

        this.replyTo = replyNickNameSeq
        this.replyMessage = replyMessage
        this.replyToMessageSeq = replyToMessageSeq
        this.messageNickNameSeq = sendUserSeq
    }
}