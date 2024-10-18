package org.jy.jamye.application.dto

import org.springframework.data.annotation.CreatedBy
import java.time.LocalDateTime

data class PostDto(
    var postSequence: Long? = null,
    val title: String,
    val groupSequence: Long,
    val createdUserSequence: Long,
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null
) {
    var createdUserNickName: String? = null
}