package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.jy.jamye.application.dto.PostDto

@Entity
@Table(name = "notify")
class Notify(
    @Column(name = "text")
    val message: String,
    @Column(name = "ui_seq")
    val userSeq: Long,
    @Column(name = "gi_seq")
    val groupSeq: Long,
    @Column(name = "pi_seq")
    val postSeq: Long,
    @Column(name = "is_read")
    var isRead: Boolean? = false,
    @Column(name = "noti_seq")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var notiSeq: Long? = null
) {
}