package org.jy.jamye.infra

import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.domain.model.Message
import org.jy.jamye.domain.model.Post
import org.springframework.stereotype.Service
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@Service
class PostFactory(
    private val groupRepo: GroupRepository,
    private val userRepo: UserRepository,
    private val groupUserRepo: GroupUserRepository
) {

    fun createPost(data: PostDto): Post {
        if(!groupUserRepo.existsByUserSequenceAndGroupSequence(data.createdUserSequence!!, data.groupSequence)) {
            throw IllegalStateException("글 작성 권한없음")
        }
        return Post(title = data.title,
            userSeq = data.createdUserSequence!!,
            groupSeq = data.groupSequence)
    }

    fun createPostMessageType(data: PostDto.MessagePost): List<Message> {
        val messages = data.message.map {
            Message(
                content = it,
                nickName = data.sendUser,
                groupUserSequence = data.sendUserInGroupSeq
            )
        }
        return messages
    }

}
