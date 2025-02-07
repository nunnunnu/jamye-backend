package org.jy.jamye.application.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class NotifyDto(
    val message: String,
    val groupSeq: Long? = null,
    val postSeq: Long? = null,
    val isRead: Boolean,
    var notifySeq: Long?  = null,
    val userSeq: Long,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    val createDate: LocalDateTime? = null
) {
}