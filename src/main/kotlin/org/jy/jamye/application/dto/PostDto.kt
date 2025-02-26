package org.jy.jamye.application.dto

import org.jy.jamye.domain.model.PostType
import java.time.LocalDateTime

data class PostDto(
    var postSequence: Long? = null,
    val title: String,
    val groupSequence: Long,
    var createdUserSequence: Long? = null,
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null,
    val type: PostType
) {
    var createdUserNickName: String? = null

    data class Detail (
        var postSequence: Long? = null,
        val title: String,
        val postType: PostType,
        val groupSequence: Long,
        val createdUserSequence: Long,
        var createDate: LocalDateTime? = null,
        var updateDate: LocalDateTime? = null,
        var createdUserNickName: String? = null,
        val isViewable: Boolean,
            )
    data class MessagePost(
        val sendUserSeq: Long? = null,
        val sendUser: String? = null,
        var message: MutableList<MessageSequence> = mutableListOf(),
        var sendDate: String? = null,
        val myMessage: Boolean? = null,
    ) {
        constructor(sendUser: String?) : this(
            sendUser = sendUser,
            message = mutableListOf(),
            myMessage = sendUser == null
        ) {
        }
    }

    data class MessageSequence(
        var seq: Long,
        var message: String? = null,
        var isReply: Boolean? = false,
        var replyMessage: String? = null,
        var replyTo: String? = null,
        val imageKey: Set<String> = setOf(),
        val imageUri: MutableSet<Pair<Long, String>> = mutableSetOf(),
        val messageSeq: Long? = null,
        val replyMessageSeq: Long? = null,
        val replyToSeq: Long? = null,
        val replyToKey: Long? = null
    ) {

        fun replyStringKey(): String {
            return this.replyToKey.toString() + this.replyToSeq.toString()
        }
    }
    data class BoardPost(
        val content: String,
    )

    data class PostContent<T> (
        var postSequence: Long,
        val title: String,
        val groupSequence: Long,
        val createdUserSequence: Long,
        var createdUserNickName: String? = null,
        var createDate: LocalDateTime,
        var updateDate: LocalDateTime,
        val postType: PostType,
        val content: T
    )

    data class MessageNickNameInfo(
        val message: MutableMap<Long, MessagePost>,
        val nickName: Map<Long, MessageNickNameDto> = mapOf()
    )

    data class MessageUpdate(
        val message: MutableMap<Long, MessagePost>,
        val deleteMessage: Set<Long> = setOf(),
        val deleteImage: Set<Long> = setOf(),
        val title: String? = null
    )

    class BoardUpdate (
        val content: String
    )
}

data class MessageNickNameDto (
    val nickName: String,
    val userSeqInGroup: Long? = null,
    var userNameInGroup: String? = null,
    var imageUri: String? = null
)
