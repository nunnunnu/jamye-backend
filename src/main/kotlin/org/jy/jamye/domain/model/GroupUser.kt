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
    @Enumerated(value = EnumType.STRING)
    @Column(name = "grade")
    val grade: Grade,
    @Column(name = "nickname")
    var nickname: String,
    @Column(name = "img_url")
    var imageUrl: String? = null,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    val updateDate: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gi_ui_seq")
    val groupUserSequence: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gi_seq", updatable = false, insertable = false)
    val group: Group
    ){
    fun updateInfo(nickName: String?, saveFile: String?) {
        if (nickName != null) {
            this.nickname = nickName
        }
        if (saveFile != null) {
            this.imageUrl = saveFile
        }
    }
}

enum class Grade {
    MASTER, NORMAL
}