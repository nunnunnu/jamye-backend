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

    fun createPostMessageType(postData: PostDto, data: PostDto.MessagePost): List<Message> {
        if(!groupUserRepo.existsByUserSequenceAndGroupSequence(postData.createdUserSequence!!, postData.groupSequence)) {
            throw IllegalStateException("글 작성 권한없음")
        }
        val messages = data.message.map {
            Message(
                content = it.message,
                nickName = data.sendUser,
                groupUserSequence = data.sendUserInGroupSeq,
                orderNumber = it.seq,
                sendDate = data.sendDate,
                title = postData.title,
                userSeq = postData.createdUserSequence!!,
                groupSeq = postData.groupSequence,
                piType = PostType.MSG
            )
        }
        return messages
    }

    fun createPostBoardType(detailContent: PostDto.BoardPost, postData: PostDto): Board {
        if(!groupUserRepo.existsByUserSequenceAndGroupSequence(postData.createdUserSequence!!, postData.groupSequence)) {
            throw IllegalStateException("글 작성 권한없음")
        }
        return Board(
            detail = detailContent.content,
            title = postData.title,
            userSeq = postData.createdUserSequence!!,
            groupSeq = postData.groupSequence,
            piType = PostType.BOR)

    }

}
