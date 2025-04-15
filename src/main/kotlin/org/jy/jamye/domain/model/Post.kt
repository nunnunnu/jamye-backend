package org.jy.jamye.domain.model

import jakarta.persistence.*
import lombok.NoArgsConstructor
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import java.time.LocalDateTime

@Entity
@Table(name = "post_info")
@NoArgsConstructor
class Post(
    @Column(name = "title")
    var title: String,
    @Column(name = "group_seq")
    var groupSeq: Long,
    @Column(name = "ui_seq")
    @CreatedBy
    val userSeq: Long,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    val updateDate: LocalDateTime = LocalDateTime.now(),
    @Column(name="pi_type")
    @Enumerated(value = EnumType.STRING)
    val postType: PostType,
    @Column(name = "pi_seq")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var postSeq: Long? = null
) {
    fun titleUpdate(title: String) {
        if (this.title != title) {
            this.title = title
        }

    }
}

enum class PostType(val koName: String, val url: String) {
    MSG("Message", "message"), BOR("Board", "board");
}