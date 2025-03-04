package org.jy.jamye.application.dto

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

data class CommentDto(
    var comment: String,
    val groupSeq: Long,
    val postSeq: Long,
    val userSeq: Long,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
    var commentSeq: Long? = null
) {

}
