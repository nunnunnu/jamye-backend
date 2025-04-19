package org.jy.jamye.application.post.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class CommentDto(
    var comment: String,
    val groupSeq: Long,
    val postSeq: Long,
    val userSeq: Long,
    var nickName: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updateDate: LocalDateTime,
    var commentSeq: Long? = null
) {

}
