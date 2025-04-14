package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.MessageNickNameDto
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.application.dto.TagDto
import org.jy.jamye.common.exception.AllPostsAlreadyOwnedException
import org.jy.jamye.common.exception.PostAccessDeniedException
import org.jy.jamye.domain.model.*
import org.jy.jamye.infra.*
import org.jy.jamye.ui.post.PostCreateDto
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
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
    private val messageNickNameRepository: MessageNickNameRepository,
    private val commentRepository: CommentRepository,
    private val tagRepository: TagRepository,
    private val postTagRepository: PostTagRepository,
    private val postReader: PostReader,
    private val tagCacheService: TagCacheService
) {
    fun postCheck(groupSequence: Long, postSequence: Long, userSequence: Long) {
        if(!postReader.postCheck(userSeq = userSequence, groupSeq = groupSequence, postSeq = postSequence)) {
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
        val tagsInPost = getTagsInPost(postSequence)
        val result = PostDto.PostContent(
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
                    ),
            tags = tagsInPost
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

        val nickNameMap = getMessageAllNickNameMap(postSeq)

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
                            replyNickNameSeq = it.replyTo,
                            replyMessage = it.replyMessage,
                            isReply = it.replyMessage != null
                            )
                    ),
                    sendDate = it.sendDate,
                    myMessage = it.messageNickNameSeq == null,
                )
                messageResponse[key++] = messagePost!!
            } else if(messagePost!!.sendUserSeq == it.messageNickNameSeq) {
                messagePost!!.message.add(PostDto.MessageSequence(
                    ++seq,
                    it.content,
                    imageUri = imageUriMap.getOrDefault(it.messageSeq, mutableSetOf()),
                    messageSeq = it.messageSeq,
                    replyMessageSeq = it.replyToMessageSeq,
                    replyNickNameSeq = it.replyTo,
                    replyMessage = it.replyMessage,
                    isReply = it.replyMessage != null,
                ))
            }
        }

        return PostDto.MessageNickNameInfo(message = messageResponse, nickName = nickNameMap)
    }

    fun getMessageAllNickNameMap(postSeq: Long): Map<Long, MessageNickNameDto> {
        val nickNameMap =
            messageNickNameRepository.findAllByPostSeq(postSeq).associate {
                it.messageNickNameSeq!! to MessageNickNameDto(
                    nickName = it.nickname,
                    userSeqInGroup = it.userSeqInGroup
                )
            }
        return nickNameMap
    }

    private fun getPostOrThrow(groupSequence: Long, postSequence: Long): Post {
        return postRepository.findByGroupSeqAndPostSeq(groupSequence, postSequence).orElseThrow { throw EntityNotFoundException("잘못된 게시글 번호입니다.") }
    }

    fun getPosts(userSeq: Long, groupSeq: Long, keyword: String?, tagSeqs: Set<Long>, page: Pageable, type: Set<PostType>): Page<PostDto.Detail> {
        //보유 게시글만 조회
        val isViewable =
            userGroupPostRepository.findPostSeqByGroupSequenceAndUserSequence(groupSeq, userSeq)

        val posts: Page<Post> = postRepository.findByGroupSeqAndPostSeqInAndFilter(groupSeq, isViewable, keyword, tagSeqs, type, page)

        val tagInfo = postTagRepository.findByPostSeqIn(posts.map { it.postSeq!! }.toSet())
        val tagMap = tagInfo.groupBy({ it.postSeq }, {
            TagDto.Detail(it.tagSeq, it.tag!!.tagName, it.postTagSeq!!)
        }).mapValues { it.value.toMutableSet() }.toMutableMap()

        return posts.map {
            PostDto.Detail(groupSequence = it.groupSeq,
                postSequence = it.postSeq!!,
                createdUserSequence = it.userSeq,
                title = it.title,
                postType = it.postType,
                createDate = it.createDate,
                updateDate = it.updateDate,
                isViewable = isViewable.contains(it.postSeq),
                tags = tagMap.getOrDefault(it.postSeq, mutableSetOf()).toList()
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
        replySeqMap: MutableMap<String, Long>,
        tagSeqs: MutableSet<Long>,
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

        tagPostConnect(post.postSeq!!, tagSeqs)

        return post.postSeq!!
    }

    private fun createMessage(
        content: List<PostDto.MessagePost>,
        postSeq: Long,
        nickNameMap: Map<String, Long> = mapOf(),
        replySeqMap: MutableMap<String, Long> = mutableMapOf(),
    ): Map<String, Long> {
        val replyEntitySeqMap = mutableMapOf<String, Message>()
        val replyKeyMap = mutableMapOf<Message, String>()
        val messages: MutableList<Message> = mutableListOf()
        content.forEach {
            it.message.forEach { msg ->
                run {
                    val message = postFactory.createPostMessageType(
                        data = it, postSeq = postSeq, message = msg,
                        nickNameMap = nickNameMap
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

    fun createPostBoardType(userSeq: Long, data: PostDto, detailContent: PostDto.BoardPost, tagSeqs: MutableSet<Long>): Long {
        val post = postFactory.createPost(data, PostType.BOR)
        postRepository.save(post)
        val content = postFactory.createPostBoardType(detailContent = detailContent, postSeq = post.postSeq!!)
        boardRepository.save(content)

        userGroupPostRepository.save(postFactory.createUserGroupFactory(
            groupSeq = data.groupSequence,
            userSeq = userSeq,
            postSeq = post.postSeq!!))

        tagPostConnect(post.postSeq!!, tagSeqs) //

        return post.postSeq!!
    }

    fun tagPostConnect(postSeq: Long, tagSeqs: Set<Long>) {
        val tagAndPostConnection = postFactory.TagAndPostConnection(postSeq, tagSeqs)
        postTagRepository.saveAll(tagAndPostConnection)
    }

    @Transactional(readOnly = true)
    fun updateAbleCheckOrThrow(groupSeq: Long, postSeq: Long, userSeq: Long) {
        if(!postRepository.existsByGroupSeqAndPostSeqAndUserSeq(groupSeq, postSeq, userSeq)) {
            throw EntityNotFoundException("본인 작성 게시글이 아닙니다")
        }
    }

    fun updateMessagePost(
        groupSeq: Long,
        postSeq: Long,
        message: MutableCollection<PostDto.MessagePost>,
        deleteMessage: Set<Long>,
        deleteImage: Set<Long>,
        replyMap: MutableMap<String, Long>,
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
                            replyNickNameSeq = msg.replyNickNameSeq,
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

    fun messagePostNickNameAdd(postSeq: Long, createInfo: Set<PostCreateDto.MessageNickNameDto>): Map<String, Long> {
        val messageNickName = createInfo
            .map { postFactory.createMessageNickName(
                nickName = it.nickName, userSeqInGroup = it.userSeqInGroup, postSeq = postSeq) }
        messageNickNameRepository.saveAll(messageNickName)
        return messageNickName.associate { it.nickname to it.messageNickNameSeq!! }
    }

    fun updateNickNameInfo(
        groupSeq: Long,
        postSeq: Long,
        userId: String,
        data: Map<Long, PostCreateDto.MessageNickNameDto>,
        deleteMessageNickNameSeqs: Set<Long>,
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

    private fun postTagUpdate(postSeq: Long, tagDisconnected: Set<Long>, tags: List<TagDto.Detail>, groupSeq: Long) {
        //tag 연결 끊기
        postTagRepository.deleteByPostSeqAndPostTagSeqIn(postSeq, tagDisconnected)

        //신규 생성 태그
        val createTags = createTag(tags.map { TagDto.Simple(it.tagSeq, it.tagName) }, groupSeq)

        //신규생성태그 - post 연결관계 생성
        tagPostConnect(postSeq, createTags)

        deleteNoUseTag() //현재 미사용 태그 삭제
    }

    fun getPostUserSeqs(groupSeq: Long, postSeq: Long): Set<Long> {
        return userGroupPostRepository.findAllByPostSequenceAndGroupSequence(groupSeq = groupSeq, postSeq = postSeq)
            .map { it.userSequence }.toSet()
    }

    private fun getBoardPostOrThrow(postSeq: Long): Board {
        return boardRepository.findByPostSeq(postSeq).orElseThrow { EntityNotFoundException("잘못된 게시글 번호입니다.") }
    }

    fun updatePost(groupSeq: Long, postSeq: Long, title: String, tagDisconnected: Set<Long>, tags: List<TagDto.Detail>) {
        val post = getPostOrThrow(groupSequence = groupSeq, postSequence = postSeq)
        post.titleUpdate(title)
        postTagUpdate(postSeq, tagDisconnected, tags, groupSeq)
    }

    fun createTempUser(
        postSeq: Long,
        tempSendUser: List<PostDto.MessagePost>,
    ) {
        val count = messageNickNameRepository.countByPostSeq(postSeq)
        if(count == 0L) {
            val nickNameMap =
                messagePostNickNameAdd(postSeq, setOf(PostCreateDto.MessageNickNameDto(nickName = "임시")))
            tempSendUser.forEach { it.sendUserSeq = nickNameMap[it.sendUser] }

        }
    }

    @Transactional
    @CacheEvict(cacheNames = ["postExistCache"], key = "#groupSeq+#postSeq+#userSeq")
    fun deletePost(groupSeq: Long, postSeq: Long, userSeq: Long) {
        //post detail 삭제
        messageImageRepository.deleteByPostSeq(postSeq)
        messageNickNameRepository.deleteByPostSeq(postSeq)
        messageRepository.deleteByPostSeq(postSeq)
        boardRepository.deleteByPostSeq(postSeq)
        postTagRepository.deleteByPostSeq(postSeq)
        //post 삭제
        postRepository.deleteById(postSeq)

        //사용자 보유 post 삭제
        userGroupPostRepository.deleteByPostSequence(postSeq)
    }

    @Transactional(readOnly = true)
    fun postCountInGroup(groupSequence: Long, userSeq: Long): PostDto.Count {
        val totalCount = postRepository.countByGroupSeq(groupSequence)
        val haveCount = userGroupPostRepository.countByUserSequenceAndGroupSequence(
            userSeq = userSeq,
            groupSequence = groupSequence
        )
        return PostDto.Count(totalCount = totalCount, haveCount = haveCount)
    }

    @Transactional(readOnly = true)
    fun getTags(groupSeq: Long, page: Pageable, userSeq: Long): Slice<TagDto.Simple> {
        val tags: Slice<Tag> = tagRepository.findSliceByGroupSeqAndUserSeq(groupSeq = groupSeq, page = page, userSeq =  userSeq)
        return tags.map { TagDto.Simple(it.tagSeq, it.tagName) }
    }

    @Transactional(readOnly = true)
    fun getGroupAllTags(groupSeq: Long, keyword: String): List<TagDto.Simple> {
        val tags: List<Tag> = tagCacheService.getTagList(groupSeq).filter { it.tagName.contains(keyword) }
        tags.sortedByDescending { it.postUseTotalCount }
        return tags.map { TagDto.Simple(it.tagSeq, it.tagName) }
    }

    @Transactional
    fun createTag(tags: List<TagDto.Simple>, groupSequence: Long): Set<Long> {
        if(tags.isEmpty()) {
            return setOf()
        }
        val tagEntityMap =
            tagRepository.findByTagNameInAndGroupSeq(tags.map { it.tagName }.toSet(), groupSequence).associate { it.tagName to it.tagSeq }
        tags.forEach { it.tagSeq = tagEntityMap[it.tagName] }
        val createTags = postFactory.createTag(tags.filter { it.tagSeq == null }, groupSequence)
        tagRepository.saveAll(createTags)
        val result = createTags.map { it.tagSeq!! }.toMutableSet()
        result.addAll(tags.filter { it.tagSeq != null }.map { it.tagSeq!! }.toMutableSet())
        return result
    }

    @Transactional
    fun deleteNoUseTag() {
        tagRepository.deleteByNoUseTag()
    }

    @Transactional(readOnly = true)
    fun getTagsInPost(postSequence: Long): List<TagDto.Detail> {
        return postTagRepository.findByPostSeq(postSequence).map {
            TagDto.Detail(
                it.tagSeq,
                it.tag!!.tagName,
                it.postTagSeq!!
            )
        }
    }

}
