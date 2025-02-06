package org.jy.jamye.application.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class NotifyDto(
    val message: String,
    val groupSeq: Long,
    val postSeq: Long,
    val isRead: Boolean,
    var notifySeq: Long?  = null,
    val userSeq: Long,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    val createDate: LocalDateTime? = null
) {
}