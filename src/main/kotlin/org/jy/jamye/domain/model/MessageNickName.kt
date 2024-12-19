package org.jy.jamye.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "msg_name")
class MessageNickName(
    @Column(name = "nickname")
    var nickname: String,
    @Column(name = "gi_ui_seq")
    var userSeqInGroup: Long? = null,
    @Column(name = "pi_seq")
    val postSeq: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_name_seq")
    var messageNickNameSeq: Long? = null
) {
}