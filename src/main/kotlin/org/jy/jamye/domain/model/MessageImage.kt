package org.jy.jamye.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "pi_msg_img")
class MessageImage(
    @Column(name = "mi_seq")
    val messageSeq: Long? = null,
    @Column(name = "img_uri")
    val imageUri: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_seq")
    val messageImageSeq: Long? = null
) {
}