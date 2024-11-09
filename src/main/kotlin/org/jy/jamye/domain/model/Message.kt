package org.jy.jamye.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pi_msg")
@DiscriminatorValue("MSG")
class Message(
    @Column(name = "pi_cont")
    val content: String,
    @Column(name = "nickName")
    var nickName: String?,
    @Column(name = "gu_seq")
    var groupUserSequence: Long?,
    @Column(name = "send_date")
    var sendDate: String? = null,
    @Column(name="num")
    val orderNumber: Long,
    title: String,
    groupSeq: Long,
    userSeq: Long,
    createDate: LocalDateTime = LocalDateTime.now(),
    updateDate: LocalDateTime = LocalDateTime.now(),
    piType: PostType
): Post(title, groupSeq, userSeq, createDate, updateDate, piType) {
}