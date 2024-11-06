package org.jy.jamye.ui.post

import java.time.LocalDateTime

class PostCreateMessageDto<T>(
    val title: String,
    val createUserSeq: Long,
    val groupSeq: Long,
    val content: List<T>
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
