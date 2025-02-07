package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.jy.jamye.application.dto.PostDto
import java.time.LocalDateTime

@Entity
@Table(name = "notify")
class Notify(
    @Column(name = "text")
    val message: String,
    @Column(name = "ui_seq")
    val userSeq: Long,
    @Column(name = "gi_seq")
    val groupSeq: Long? = null,
    @Column(name = "pi_seq")
    val postSeq: Long? = null,
    @Column(name = "is_read")
    var isRead: Boolean = false,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    val updateDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "noti_seq")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var notiSeq: Long? = null
) {
    fun read() {
        this.isRead = true
    }
}