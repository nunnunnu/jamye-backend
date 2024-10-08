package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "post_info")
class Post(
    @Column(name = "title")
    var title: String,
    @Column(name = "create_ui_seq")
    val createUserSequence: Long,
    @Column(name = "update_ui_seq")
    var updateUserSequence: Long,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    var updateDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "pi_seq")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var postSequence: Long? = null
) {
}