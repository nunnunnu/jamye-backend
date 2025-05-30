package org.jy.jamye.infra.post

import org.jy.jamye.application.post.dto.PostDto
import org.jy.jamye.application.post.dto.TagDto
import org.jy.jamye.domain.user.model.UserGroupPost
import org.jy.jamye.domain.post.model.*
import org.jy.jamye.infra.group.GroupRepository
import org.jy.jamye.infra.user.GroupUserRepository
import org.jy.jamye.infra.user.UserRepository
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


    fun createPostMessageType(data: PostDto.MessagePost, message: PostDto.MessageSequence, postSeq: Long, nickNameMap: Map<String, Long>): Message {
        return Message(
            content = message.message,
            messageNickNameSeq = if (nickNameMap[data.sendUser] == null) data.sendUserSeq else nickNameMap[data.sendUser],
            orderNumber = message.seq,
            sendDate = data.sendDate,
            postSeq = postSeq,
            replyTo = message.replyTo?.let { nickNameMap[it as String] },
            replyMessage = message.replyMessage,
            messageImage = message.imageUri.map { img ->
                MessageImage(imageUri = img.second)
            }.toSet()
        )
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

    fun createMessageNickName(nickName: String, userSeqInGroup: Long?, postSeq: Long): MessageNickName {
        return MessageNickName(nickName, userSeqInGroup, postSeq)
    }

    fun createLuckyDraw(userSeq: Long, groupSeq: Long, luckyDrawSeq: Long): UserGroupPost {
        return UserGroupPost(userSequence = userSeq, groupSequence = groupSeq, postSequence = luckyDrawSeq)
    }

    fun createTag(tags: List<TagDto.Simple>, groupSeq: Long): List<Tag> {
        return tags.map { Tag(groupSeq = groupSeq, tagName = it.tagName) }
    }

    fun TagAndPostConnection(postSeq: Long, tagSeqs: Set<Long>): List<PostTagConnection> {
        return tagSeqs.map { PostTagConnection(postSeq = postSeq, tagSeq = it) }
    }

}
