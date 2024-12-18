package org.jy.jamye.application.dto

import org.jy.jamye.domain.model.GroupUser
import org.jy.jamye.domain.model.PostType
import java.time.LocalDateTime

data class PostDto(
    var postSequence: Long? = null,
    val title: String,
    val groupSequence: Long,
    var createdUserSequence: Long? = null,
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null,
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
        val sendUser: String? = null,
        val sendUserInGroupSeq: Long? = null,
        var message: MutableList<MessageSequence> = mutableListOf(),
        var sendDate: String? = null,
        val myMessage: Boolean? = null
    ) {
        constructor(sendUser: String?) : this(
            sendUser = sendUser,
            message = mutableListOf(),
            myMessage = sendUser == null
        ) {
        }
    }

    data class MessageSequence(
        val seq: Long,
        var message: String? = null,
        var isReply: Boolean? = false,
        var replyMessage: String? = null,
        var replyTo: String? = null,
        val imageKey: Set<String> = setOf(),
        val imageUri: Set<String> = setOf(),
        val messageSeq: Long? = null
    ) {

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

    data class MessageNickName(
        val message: MutableMap<Long, MessagePost>,
        val nickName: Map<String, String> = mapOf()
    )

    data class MessageUpdate(
        val message: MutableMap<Long, MessagePost>,
        val nickName: Map<String, String> = mapOf(),
        val deleteMessage: Set<Long> = setOf()
    )
}