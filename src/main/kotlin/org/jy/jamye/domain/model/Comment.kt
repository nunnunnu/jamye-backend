package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "pi_comment")
class Comment (
    @Column(name = "cm", columnDefinition = "TEXT")
    var comment: String,
    @Column(name="gi_seq")
    val groupSeq: Long,
    @Column(name="pi_seq")
    val postSeq: Long,
    @Column(name="ui_seq")
    val userSeq: Long,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    val updateDate: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pc_seq")
    var commentSeq: Long? = null
){
    fun update(comment: String) {
        if (comment.isNotBlank()) {
            this.comment = comment
        }

    }
}