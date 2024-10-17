package org.jy.jamye.application.dto

import org.springframework.data.annotation.CreatedBy
import java.time.LocalDateTime

data class PostDto(
    val postSequence: Long,
    val title: String,
    val groupSequence: Long,
    val createdUserSequence: Long,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime
) {
    var createdUserNickName: String? = null
}