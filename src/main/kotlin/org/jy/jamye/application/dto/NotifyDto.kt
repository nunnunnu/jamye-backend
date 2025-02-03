package org.jy.jamye.application.dto

import java.time.LocalDateTime

data class NotifyDto(
    val message: String,
    val groupSeq: Long,
    val postSeq: Long,
    val isRead: Boolean,
    var notifySeq: Long?  = null,
    val createDate: LocalDateTime? = null
) {
}