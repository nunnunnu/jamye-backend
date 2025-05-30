package org.jy.jamye.domain.post.model

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
    fun update(userSeqInGroup: Long?, nickName: String) {
        this.nickname = nickName
        this.userSeqInGroup = userSeqInGroup
    }
}