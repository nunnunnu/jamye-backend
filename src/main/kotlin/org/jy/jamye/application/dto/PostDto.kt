package org.jy.jamye.application.dto

import java.time.LocalDateTime

data class PostDto(
    var postSequence: Long? = null,
    val title: String,
    val groupSequence: Long,
    val createdUserSequence: Long? = null,
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null,
) {
    var createdUserNickName: String? = null

    data class Detail (
        var postSequence: Long? = null,
        val title: String,
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
        var message: MutableList<String> = mutableListOf(),
        var sendDate: String? = null,
        val myMessage: Boolean? = null,
        var isReply: Boolean? = false,
        var replyMessage: String? = null,
    ) {
        constructor(sendUser: String?) : this(
            sendUser = sendUser,
            message = mutableListOf(),
            myMessage = if(sendUser == null) true else false
        ) {

        }


    }
}