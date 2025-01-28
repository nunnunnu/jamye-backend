package org.jy.jamye.domain.model

import jakarta.persistence.*
import org.h2.util.StringUtils
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
    val updateDate: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gi_seq", nullable = false)
    var sequence: Long? = null
) {
    fun updateInfo(name: String?, imageUrl: String?, description: String?) {
        if(!StringUtils.isWhitespaceOrEmpty(name)) {
            this.name = name!!
        }
        if(!imageUrl.isNullOrBlank()) {
            this.imageUrl = imageUrl
        }
        if(!description.isNullOrBlank()) {
            this.description = description
        }
    }
}