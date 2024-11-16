package org.jy.jamye.infra

import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.domain.model.Board
import org.jy.jamye.domain.model.Message
import org.jy.jamye.domain.model.Post
import org.jy.jamye.domain.model.PostType
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
            piType = type
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
                replyMessage = it.replyMessage
            )
        }
        return messages
    }

    fun createPostBoardType(detailContent: PostDto.BoardPost, postSeq: Long): Board {
        return Board(
            detail = detailContent.content,
            postSeq = postSeq)

    }

}
