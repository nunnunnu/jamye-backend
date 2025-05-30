package org.jy.jamye.ui.post

import org.jy.jamye.application.post.dto.TagDto
import java.time.LocalDateTime

data class PostCreateDto<T>(
    val title: String,
    val groupSeq: Long,
    val content: T,
    val tags: List<TagDto.Simple> = listOf()
) {
    data class Message(
        val seq: Long,
        val content: String,
        val sendUserNickName: String? = null,
        val sendUserInGroupSeq:Long? = null,
        val sendDate: LocalDateTime? = null
    )

    data class Board(
        val title: String? = null,
        var content: String,
        var tags: List<TagDto.Detail> = listOf(),
        var tagDisconnected: Set<Long> = setOf()
    ) {
        fun replaceUri(imageUriMap: MutableMap<String, Pair<Long, String>>, imageUrl: String?) {
            imageUriMap.forEach { (t, u) ->
                run {
                    this.content = this.content.replace(
                        t, imageUrl + "/" + u.second
                    )
                }
            }


        }
    }
    data class MessageNickNameDto (
        val nickName: String,
        val userSeqInGroup: Long? = null,
        var imageUri: String? = null
    )

    data class MessageNickNameUpdate(
        val deleteMessageNickNameSeqs: Set<Long> = setOf(),
        val updateInfo: Map<Long, MessageNickNameDto>,
        val createInfo: Set<MessageNickNameDto> = setOf()
    )
}


