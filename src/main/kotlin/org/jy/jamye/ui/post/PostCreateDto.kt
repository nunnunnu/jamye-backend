package org.jy.jamye.ui.post

import java.time.LocalDateTime

class PostCreateDto<T>(
    val title: String,
    val groupSeq: Long,
    val content: T
) {
    data class Message(
        val content: String,
        val sendUserNickName: String? = null,
        val sendUserInGroupSeq:Long?,
        val sendDate: LocalDateTime?
    )

    data class Board(
        val content: String
    )

}
