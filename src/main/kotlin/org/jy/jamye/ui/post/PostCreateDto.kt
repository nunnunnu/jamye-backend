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
        var content: String
    ) {
        fun replaceUri(imageUriMap: MutableMap<String, String>) {
            imageUriMap.forEach { (t, u) ->
                println(this.content.contains(t))
                run {
                    this.content = this.content.replace(
                        t, u
                    )
                }
            }


        }
    }

}
