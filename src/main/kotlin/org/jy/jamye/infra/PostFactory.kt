package org.jy.jamye.infra

import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.domain.model.*
import org.springframework.stereotype.Service

@Service
class PostFactory(
    private val groupRepo: GroupRepository,
    private val userRepo: UserRepository,
    private val groupUserRepo: GroupUserRepository
) {

    fun createPost(data: PostDto, type: PostType): Post {
        if(!groupUserRepo.existsByUserSequenceAndGroupSequence(data.createdUserSequence!!, data.groupSequence)) {
            throw IllegalStateException("글 작성 권한없음")
        }
        return Post(title = data.title,
            userSeq = data.createdUserSequence!!,
            groupSeq = data.groupSequence,
            postType = type
            )
    }


    fun createPostMessageType(data: PostDto.MessagePost, postSeq: Long): List<Message> {
        val messages = data.message.map {
            Message(
                content = it.message,
                nickName = data.sendUser,
                groupUserSequence = data.sendUserInGroupSeq,
                orderNumber = it.seq,
                sendDate = data.sendDate,
                postSeq = postSeq,
                replyTo = it.replyTo,
                replyMessage = it.replyMessage,
                messageImage = it.imageUri.map { img ->
                    MessageImage(imageUri = img.second)
                }.toSet()
            )
        }
        return messages
    }

    fun createPostBoardType(detailContent: PostDto.BoardPost, postSeq: Long): Board {
        return Board(
            detail = detailContent.content,
            postSeq = postSeq)

    }

    fun createUserGroupFactory(groupSeq: Long, postSeq: Long, userSeq: Long): UserGroupPost {
        return UserGroupPost(
            groupSequence = groupSeq,
            postSequence = postSeq,
            userSequence = userSeq
        )
    }

    fun createMessageImage(messageSeq: Long, uriList: List<String>): List<MessageImage> {
        return uriList.map { MessageImage(messageSeq = messageSeq, imageUri = it) }

    }

}
