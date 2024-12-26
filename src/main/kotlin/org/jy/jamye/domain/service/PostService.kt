package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.MessageNickNameDto
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.exception.AllPostsAlreadyOwnedException
import org.jy.jamye.common.exception.PostAccessDeniedException
import org.jy.jamye.domain.model.Message
import org.jy.jamye.domain.model.MessageNickName
import org.jy.jamye.domain.model.Post
import org.jy.jamye.domain.model.PostType
import org.jy.jamye.infra.*
import org.jy.jamye.ui.post.PostCreateDto
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userGroupPostRepository: UserGroupPostRepository,
    private val postFactory: PostFactory,
    private val messageRepository: MessageRepository,
    private val boardRepository: BoardRepository,
    private val messageImageRepository: MessageImageRepository,
    private val groupUserRepository: GroupUserRepository,
    private val messageNickNameRepository: MessageNickNameRepository
) {
    fun postCheck(groupSequence: Long, postSequence: Long, userSequence: Long) {
        if(!userGroupPostRepository.existsByUserSequenceAndGroupSequenceAndPostSequence(userSequence, groupSequence, postSequence)) {
            throw PostAccessDeniedException()
        }
    }

    fun getPostTitle(groupSeq: Long, postSeq: Long): PostDto {
        val post = getPostOrThrow(groupSeq, postSeq)
        return PostDto(
            groupSequence = post.groupSeq,
            postSequence = post.postSeq!!,
            createdUserSequence = post.userSeq,
            title = post.title,
            createDate = post.createDate,
            updateDate = post.updateDate
        )
    }

    fun getPost(groupSequence: Long, postSequence: Long): PostDto.PostContent<Any> {
        val post = getPostOrThrow(groupSequence, postSequence)
        val result = PostDto.PostContent<Any>(
            groupSequence = post.groupSeq,
            postSequence = post.postSeq!!,
            createdUserSequence = post.userSeq,
            title = post.title,
            createDate = post.createDate,
            updateDate = post.updateDate,
            postType = post.postType,
            content = if (post.postType == PostType.MSG) getMessagePost(postSequence)
                    else PostDto.BoardPost(
                        content = boardRepository.findByPostSeq(postSequence).detail
                    )
            )
        return result
    }

    private fun getMessagePost(postSeq: Long): PostDto.MessageNickNameInfo {
        val messageResponse = mutableMapOf<Long, PostDto.MessagePost>()
        var messagePost: PostDto.MessagePost? = null
        var key = 1L
        var seq = 0L
        val messages = messageRepository.findAllByPostSeqOrderByOrderNumber(postSeq)
        val imageUriMap: Map<Long?, MutableSet<Pair<Long, String>>> = messageImageRepository.findByMessageSeqIn(messages.map { it.messageSeq!! }.toSet())
            .groupBy{
                it.messageSeq
            }.mapValues { entry -> entry.value.map { it.messageImageSeq!! to it.imageUri }.toMutableSet() }

        val nickNameMap =
            messageNickNameRepository.findAllByPostSeq(postSeq).associate { it.messageNickNameSeq!! to MessageNickNameDto(nickName = it.nickname, userSeqInGroup = it.userSeqInGroup) }

        messages.forEach{
            if(seq == 0L || messagePost!!.sendUserSeq != it.messageNickNameSeq) {
                messagePost = PostDto.MessagePost(
                    sendUserSeq = it.messageNickNameSeq,
                    message = mutableListOf(
                        PostDto.MessageSequence(
                            seq = ++seq,
                            message = it.content,
                            imageUri = imageUriMap.getOrDefault(it.messageSeq, mutableSetOf()),
                            messageSeq = it.messageSeq)
                    ),
                    sendDate = it.sendDate,
                    myMessage = it.messageNickNameSeq == null,
                )
                messageResponse[key++] = messagePost!!
            } else if(messagePost!!.sendUserSeq == it.messageNickNameSeq) {
                messagePost!!.message.add(PostDto.MessageSequence(++seq, it.content, imageUri = imageUriMap.getOrDefault(it.messageSeq, mutableSetOf()), messageSeq = it.messageSeq))
            }

        }

        return PostDto.MessageNickNameInfo(message = messageResponse, nickName = nickNameMap)
    }

    private fun getPostOrThrow(groupSequence: Long, postSequence: Long): Post {
        return postRepository.findByGroupSeqAndPostSeq(groupSequence, postSequence).orElseThrow { throw EntityNotFoundException("잘못된 게시글 번호입니다.") }
    }

    fun getPosts(userSeq: Long, groupSeq: Long): List<PostDto.Detail> {
        val posts = postRepository.findByGroupSeq(groupSeq)

        val isViewable =
            userGroupPostRepository.findPostSeqByGroupSequenceAndUserSequence(groupSeq, userSeq)

        return posts.map {
            PostDto.Detail(groupSequence = it.groupSeq,
                postSequence = it.postSeq!!,
                createdUserSequence = it.userSeq,
                title = it.title,
                postType = it.postType,
                createDate = it.createDate,
                updateDate = it.updateDate,
                isViewable = isViewable.contains(it.postSeq)
            )
        }
    }

    fun deletePostInGroup(groupSeq: Long) {
        postRepository.deleteByGroupSeq(groupSeq)
        userGroupPostRepository.deleteByGroupSequence(groupSeq)
        //todo: post detail 삭제 로직 추가
    }

    fun deleteUserAllPostInGroup(agreeUserSeqs: Set<Long>, groupSeq: Long) {
        postRepository.deleteByUserSeqInAndGroupSeq(agreeUserSeqs, groupSeq)
        userGroupPostRepository.deleteByGroupSequenceAndUserSequenceIn(groupSeq, agreeUserSeqs)

    }

    fun luckyDraw(groupSeq: Long, userSeq: Long): Long {
        val postSeqs: MutableList<Long> = postRepository.countAllByAbleDrawPool(groupSeq, userSeq)

        if(postSeqs.isEmpty()) {
            throw AllPostsAlreadyOwnedException()
        }

        val pickPostSeq = postSeqs[(Math.random() * postSeqs.size).toInt()]

        return pickPostSeq
    }

    fun createPostMessageType(
        data: PostDto,
        content: List<PostDto.MessagePost>,
        userSeq: Long,
        nickNameMap: Map<String, Long?>
    ): Long {
        val post = postFactory.createPost(data, PostType.MSG)
        postRepository.save(post)

        val nickNames = mutableListOf<MessageNickName>()
        nickNameMap.forEach{(key, value) ->
            run {
                nickNames.add(postFactory.createMessageNickName(key, value, post.postSeq!!))
            }
        }

        messageNickNameRepository.saveAll(nickNames)

        val nickNameMap = nickNames.associate { it.nickname to it.messageNickNameSeq!! }

        createMessage(content, post.postSeq!!, nickNameMap)

        userGroupPostRepository.save(postFactory.createUserGroupFactory(
            groupSeq = data.groupSequence,
            userSeq = userSeq,
            postSeq = post.postSeq!!))

        return post.postSeq!!
    }

    private fun createMessage(
        content: List<PostDto.MessagePost>,
        postSeq: Long,
        nickNameMap: Map<String, Long> = mapOf(),
    ) {
        val messages: MutableList<Message> = mutableListOf()
        content.forEach { messages.addAll(postFactory.createPostMessageType(data = it, postSeq = postSeq,
            nickNameMap[it.sendUser]
        )) }

        messageRepository.saveAll(messages)
    }

    fun createPostBoardType(userSeq: Long, data: PostDto, detailContent: PostDto.BoardPost): Long {
        val post = postFactory.createPost(data, PostType.BOR)
        postRepository.save(post)
        val content = postFactory.createPostBoardType(detailContent = detailContent, postSeq = post.postSeq!!)
        boardRepository.save(content)

        userGroupPostRepository.save(postFactory.createUserGroupFactory(
            groupSeq = data.groupSequence,
            userSeq = userSeq,
            postSeq = post.postSeq!!))

        return post.postSeq!!
    }

    fun updateAbleCheckOrThrow(groupSeq: Long, postSeq: Long, userSeq: Long) {
        if(!postRepository.existsByGroupSeqAndPostSeqAndUserSeq(groupSeq, postSeq, userSeq)) {
            throw EntityNotFoundException("본인 작성 게시글이 아닙니다")
        }
    }

    fun postUpdate(
        groupSeq: Long,
        postSeq: Long,
        message: MutableCollection<PostDto.MessagePost>,
        deleteMessage: Set<Long>,
        deleteImage: Set<Long>
    ) {
        val updateMessage = mutableListOf<PostDto.MessagePost>()
        val createMessage = mutableListOf<PostDto.MessagePost>()
        message.forEach {
            val update = it.copy()
            update.message = it.message.filter { msg -> msg.messageSeq != null }.toMutableList()
            updateMessage.add(update)

            val create = it.copy()
            create.message = it.message.filter { msg -> msg.messageSeq == null }.toMutableList()
            createMessage.add(create)

        }

        if(updateMessage.isNotEmpty()) {
            val messageEntityMap: Map<Long, Message> =
                messageRepository.findAllById(updateMessage.flatMap { it.message.map { it.messageSeq!! } })
                    .associateBy { it.messageSeq!! }

            updateMessage.forEach { it ->
                run {
                    it.message.forEach { msg ->
                        messageImageRepository.saveAll(postFactory.createMessageImage(msg.messageSeq!!, msg.imageUri.filter { it.first == 0L }.map { it.second }))
                        messageEntityMap[msg.messageSeq]!!.update(
                            content = msg.message,
                            messageNickNameSeq = it.sendUserSeq,
                            replyTo = msg.replyTo,
                            replyMessage = msg.replyMessage,
                            orderNumber = msg.seq
                        )
                    }
                }
            }
            messageRepository.saveAll(messageEntityMap.values)
        }

        if(createMessage.isNotEmpty()) {
            createMessage(content = createMessage, postSeq = postSeq)
        }

        if(deleteMessage.isNotEmpty()) {
            messageRepository.deleteAllById(deleteMessage)
            messageImageRepository.deleteAllByMessageSeqIn(deleteMessage)
        }

        if(deleteImage.isNotEmpty()) {
            messageImageRepository.deleteAllById(deleteImage)
        }
    }

    fun messagePostNickNameAdd(groupSeq: Long, postSeq: Long, userSeqInGroup: Long?, userId: String, nickName: String): Long {
        val messageNickName =
            postFactory.createMessageNickName(nickName = nickName, userSeqInGroup = userSeqInGroup, postSeq = postSeq)

        messageNickNameRepository.save(messageNickName)

        return messageNickName.messageNickNameSeq!!
    }

    fun updateNickNameInfo(
        groupSeq: Long,
        postSeq: Long,
        userId: String,
        data: Map<Long, PostCreateDto.MessageNickNameDto>,
        deleteMessageNickNameSeqs: Set<Long>
    ) {
        messageNickNameRepository.deleteAllById(deleteMessageNickNameSeqs)
        val messageNickNames = messageNickNameRepository.findAllById(data.keys)

        messageNickNames.forEach {
            run {
                val updateInfo = data[it.messageNickNameSeq]
                if (updateInfo != null) {
                    it.update(updateInfo.userSeqInGroup, updateInfo.nickName)
                }


            }
        }


    }

    fun createLuckyDraw(userSeq: Long, groupSeq: Long, luckyDrawSeq: Long) {
        val createLuckyDraw = postFactory.createLuckyDraw(userSeq, groupSeq, luckyDrawSeq)
        userGroupPostRepository.save(createLuckyDraw)
    }
}
