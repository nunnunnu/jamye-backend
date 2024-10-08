package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "gi_ui_con")
class GroupUser (
    @Column(name = "ui_seq", nullable = false)
    val userSequence: Long,
    @Column(name = "gi_seq", nullable = false)
    val groupSequence: Long,
    @Column(name = "grade")
    val grade: Grade,
    @Column(name = "nickname")
    var nickname: String,
    @Column(name = "img_url")
    var imageUrl: String,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    var updateDate: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val groupUserSequence: Long? = null
    ){
}