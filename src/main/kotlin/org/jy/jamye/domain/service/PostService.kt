package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.MessageNickNameDto
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.exception.AllPostsAlreadyOwnedException
import org.jy.jamye.common.exception.PostAccessDeniedException
import org.jy.jamye.domain.model.*
import org.jy.jamye.infra.*
import org.jy.jamye.ui.post.PostCreateDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
            updateDate = post.updateDate,
            type = post.postType
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
                        content = getBoardPostOrThrow(postSequence).detail
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
                            messageSeq = it.messageSeq,
                            replyMessageSeq = it.replyToMessageSeq,
                            replyTo = it.replyTo,
                            replyMessage = it.replyMessage,
                            isReply = it.replyTo != null
                            )
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

    @Transactional
    fun deletePostInGroup(groupSeq: Long) {
        //post detail 삭제
        messageImageRepository.deleteAllPostInGroup(groupSeq)
        messageNickNameRepository.deleteAllPostInGroup(groupSeq)
        messageRepository.deleteAllByGroupSeq(groupSeq)
        boardRepository.deleteAllByGroupSeq(groupSeq)
        //post 삭제
        postRepository.deleteByGroupSeq(groupSeq)

        //사용자 보유 post 삭제
        userGroupPostRepository.deleteByGroupSequence(groupSeq)
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
        nickNameMap: Map<String, Long?>,
        replySeqMap: MutableMap<String, Long>
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

        val nickNameSeqMap = nickNames.associate { it.nickname to it.messageNickNameSeq!! }

        createMessage(content, post.postSeq!!, nickNameSeqMap, replySeqMap)

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
        replySeqMap: MutableMap<String, Long> = mutableMapOf()
    ): Map<String, Long> {
        val replyEntitySeqMap = mutableMapOf<String, Message>()
        val replyKeyMap = mutableMapOf<Message, String>()
        val messages: MutableList<Message> = mutableListOf()
        content.forEach {
            it.message.forEach { msg ->
                run {
                    val message = postFactory.createPostMessageType(
                        data = it, postSeq = postSeq, message = msg,
                        messageNickNameSeq = if (nickNameMap[it.sendUser] == null) it.sendUserSeq else nickNameMap[it.sendUser]
                    )
                    replyKeyMap[message] = msg.replyStringKey()

                    replySeqMap.entries.forEach { (key, value) ->
                        if (value == msg.seq) {
                            replyEntitySeqMap[key] = message
                        }
                    }

                    messages.add(message)
                }
            }
        }
        messageRepository.saveAll(messages)
        messages.forEach { message ->
            val keySeq = replyKeyMap[message]
            if(replyEntitySeqMap.containsKey(keySeq)) {
                message.replyToMessageSeq = replyEntitySeqMap[keySeq]!!.messageSeq
            }
        }
        return replyEntitySeqMap.map { it.key to it.value.messageSeq!! }.toMap()
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
        deleteImage: Set<Long>,
        replyMap: MutableMap<String, Long>
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
        var replyPrimaryKeyMap = mapOf<String, Long>()
        if(createMessage.isNotEmpty()) {
            replyPrimaryKeyMap = createMessage(content = createMessage, postSeq = postSeq, replySeqMap = replyMap)
        }

        if(updateMessage.isNotEmpty()) {
            val messageEntityMap: Map<Long, Message> =
                messageRepository.findAllById(updateMessage.flatMap { it.message.map { msg -> msg.messageSeq!! } })
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
                            replyToMessageSeq =
                                if(msg.replyMessageSeq != null) {
                                    if (deleteMessage.contains(msg.replyMessageSeq)) null
                                    else msg.replyMessageSeq
                                } else if(msg.replyToKey != null && msg.replyToSeq != null){
                                    replyPrimaryKeyMap[msg.replyStringKey()]
                                 } else null,
                            orderNumber = msg.seq,
                            sendUserSeq = it.sendUserSeq
                        )
                    }
                }
            }
            messageRepository.saveAll(messageEntityMap.values)
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
        messageNickNameRepository.saveAll(messageNickNames)


    }

    fun createLuckyDraw(userSeq: Long, groupSeq: Long, luckyDrawSeq: Long) {
        val createLuckyDraw = postFactory.createLuckyDraw(userSeq, groupSeq, luckyDrawSeq)
        userGroupPostRepository.save(createLuckyDraw)
    }

    fun updateBoardPost(groupSeq: Long, postSeq: Long, data: PostCreateDto.Board) {
        val board = getBoardPostOrThrow(postSeq)
        board.contentUpdate(data.content)
        boardRepository.save(board)
    }

    private fun getBoardPostOrThrow(postSeq: Long): Board {
        return boardRepository.findByPostSeq(postSeq).orElseThrow { EntityNotFoundException("잘못된 게시글 번호입니다.") }
    }
}
