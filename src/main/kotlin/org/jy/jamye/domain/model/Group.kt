package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "group_info")
class Group (
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "img_url")
    var imageUrl: String? = null,
    @Column(name = "description")
    var description: String? = null,
    @Column(name = "create_date")
    @CreationTimestamp
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "update_date")
    @UpdateTimestamp
    var updateDate: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gi_seq", nullable = false)
    var sequence: Long? = null
)