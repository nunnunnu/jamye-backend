package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "ui_pi_con")
class UserGroupPost (
    @Column(name = "ui_seq")
    val userSequence: Long,
    @Column(name = "gi_seq")
    val groupSequence: Long,
    @Column(name = "pi_seq")
    val postSequence: Long,
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    var updateDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "ugp_seq", nullable = false)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userPostSequence: Long? = null
){
}