package org.jy.jamye.ui.post

import java.time.LocalDateTime

data class PostCreateDto<T>(
    val title: String,
    val groupSeq: Long,
    val content: T
) {
    data class Message(
        val seq: Long,
        val content: String,
        val sendUserNickName: String? = null,
        val sendUserInGroupSeq:Long? = null,
        val sendDate: LocalDateTime? = null
    )

    data class Board(
        val content: String
    ) {
        fun replaceUri(imageUriMap: MutableMap<String, String>) {
            imageUriMap.forEach { (t, u) ->  {
                this.content.replace("<img src=\"${t}\" alt=\"image\" width=\"200\" height=\"auto\"/>", "<img src=\"${u}\" alt=\"image\" width=\"200\" height=\"auto\"/>")
            }}


        }
    }

}
