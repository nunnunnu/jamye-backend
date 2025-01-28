package org.jy.jamye.application.dto

data class NotifyDto(
    val message: String,
    val groupSeq: Long,
    val postSeq: Long
) {
}