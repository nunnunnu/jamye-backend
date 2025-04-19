package org.jy.jamye.application.post

import org.jy.jamye.application.post.dto.MessageNickNameDto
import org.jy.jamye.application.post.dto.PostDto
import org.jy.jamye.application.post.dto.TagDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.listener.NotifyInfo
import org.jy.jamye.domain.post.model.PostType
import org.jy.jamye.domain.post.service.CommentService
import org.jy.jamye.domain.group.service.GroupService
import org.jy.jamye.domain.post.service.PostService
import org.jy.jamye.domain.user.service.UserService
import org.jy.jamye.ui.post.PostCreateDto
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostApplicationService(
    private val postService: PostService,
    private val userService: UserService,
    private val groupService: GroupService,
    private val redisClient: RedisClient,
    private val publisher: ApplicationEventPublisher,
    private val commentService: CommentService
) {
    fun getPost(groupSequence: Long, postSequence: Long, userId: String): PostDto.PostContent<Any> {
        val user = userService.getUser(id = userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSequence)

        postService.postCheck(groupSequence, postSequence, user.sequence)
        val post = postService.getPost(
            groupSequence = groupSequence,
            postSequence = postSequence
        )
        val createUserInfo =
            groupService.groupUserInfo(groupSequence = groupSequence, userSequence = post.createdUserSequence)
        if(createUserInfo!=null) {
            post.createdUserNickName = createUserInfo.nickname
        }
        if(post.postType == PostType.MSG) {
            val messageInfo = post.content as PostDto.MessageNickNameInfo
            val userInfoInGroupMap =
                groupService.userInfoInGroup(messageInfo.nickName.values.filter { it.userSeqInGroup != null }
                    .map { it.userSeqInGroup!! }.toSet())
            messageInfo.nickName.forEach { (_, value) ->
                value.userSeqInGroup.let {
                    userInfoInGroupMap[value.userSeqInGroup]?.let {
                        value.userNameInGroup = it.nickname
                        value.imageUri = it.imageUrl
                    }
                }

            }
        }
        return post
    }

    fun getPosts(userId: String, groupSequence: Long, page: Pageable, keyword: String?, tagSeqs: Set<Long>, type: Set<PostType>) : Page<PostDto.Detail> {
        val user = userService.getUser(id = userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSequence)
        val posts = postService.getPosts(user.sequence, groupSequence, keyword, tagSeqs, page, type)
        val userInfoMap = groupService.getGroupInUsersNickName(groupSequence, posts.map { it.createdUserSequence }.toList())

        posts.forEach { it.createdUserNickName = userInfoMap[it.createdUserSequence] }
        return posts
    }

    fun postLuckyDraw(groupSeq: Long, userId: String): PostDto {
        val luckyDrawMap = redisClient.getLuckyDrawMap()
        val user = userService.getUser(id = userId)
        val userSeq = user.sequence!!
        val count = luckyDrawMap.getOrDefault("$userSeq-$groupSeq", 0)
        if(count >= 3) {
            throw IllegalArgumentException("뽑기는 그룹 당 하루에 3번까지 가능합니다.")
        }

        val luckyDrawSeq = postService.luckyDraw(groupSeq, userSeq)
        val result =
            postService.getPostTitle(groupSeq = groupSeq, postSeq = luckyDrawSeq)
        val createUserInfo =
            groupService.groupUserInfo(groupSequence = groupSeq, userSequence = result.createdUserSequence!!)
        if(createUserInfo!=null) {
            result.createdUserNickName = createUserInfo.nickname
        }

        postService.createLuckyDraw(userSeq, groupSeq, luckyDrawSeq)
        redisClient.setLuckyDrawMap("$userSeq-$groupSeq")

        return result
    }

    fun createPostMessage(
        userId: String,
        post: PostDto,
        content: List<PostDto.MessagePost>,
        nickNameMap: Map<String, Long?>,
        replySeqMap: MutableMap<String, Long>,
        tags: List<TagDto.Simple>
    ): Long {
        val user = userService.getUser(userId)
        post.createdUserSequence = user.sequence!!
        val sendUserSeqs: Set<Long> = content.filter { it.sendUserSeq != null }.map { it.sendUserSeq!! }.toSet()
        groupService.usersInGroupCheckOrThrow(sendUserSeqs, post.groupSequence)
        val tagSeqs = tags.filter { it.tagSeq != null }.map { it.tagSeq!! }.toMutableSet()
        tagSeqs.addAll(postService.createTag(tags, post.groupSequence))

        sendNotifyPostCreate(groupSeq = post.groupSequence, userSeq = user.sequence)

        return postService.createPostMessageType(post, content, user.sequence, nickNameMap, replySeqMap, tagSeqs)
    }

    private fun sendNotifyPostCreate(groupSeq: Long, userSeq: Long) {
        val userSeqsInGroup = groupService.getUserSeqsInGroup(groupSeq = groupSeq).toMutableSet()
        userSeqsInGroup.remove(userSeq)
        val group = groupService.getGroup(userSequence = userSeq, groupSequence = groupSeq)
        val event = NotifyInfo(
            message = "[${group.name}] 새로운 잼얘가 등록되었습니다 지금 접속하여 뽑아보세요",
            userSeqs = userSeqsInGroup
        )
        publisher.publishEvent(event)
    }

    fun createPostBoard(userId: String, post: PostDto, content: PostDto.BoardPost, tags: List<TagDto.Simple>): Long {
        val user = userService.getUser(userId)
        post.createdUserSequence = user.sequence!!
        val tagSeqs = tags.filter { it.tagSeq != null }.map { it.tagSeq!! }.toMutableSet()
        tagSeqs.addAll(postService.createTag(tags, post.groupSequence))
        val postSeq = postService.createPostBoardType(user.sequence, post, content, tagSeqs)

        sendNotifyPostCreate(groupSeq = post.groupSequence, userSeq = user.sequence)

        return postSeq
    }

    @Transactional
    fun updateMessagePost(
        groupSeq: Long,
        postSeq: Long,
        userId: String,
        data: PostDto.MessageUpdate,
        replyMap: MutableMap<String, Long>
    ) {
        val tempSendUser = data.message.values.filter { it.sendUser.equals("임시") }
        if(tempSendUser.isNotEmpty()) {
            postService.createTempUser(postSeq, tempSendUser)
        }
        val user = userService.getUser(userId)
        postService.updateAbleCheckOrThrow(groupSeq = groupSeq, postSeq = postSeq, userSeq = user.sequence!!)

        var seq = 1L
        data.message.values.forEach { it.message.forEach { msg -> msg.seq = seq++ } }
        data.title?.let { if(it.isNotBlank()) postService.updatePost(groupSeq, postSeq, data.title, data.tagDisconnected, data.tags) }
        postService.updateMessagePost(groupSeq, postSeq, data.message.values, data.deleteMessage, data.deleteImage, replyMap)
        sendNotifyPostUpdate(groupSeq = groupSeq, postSeq = postSeq)
        userService.getNotifyNoReadCount(user.sequence)
    }

    fun updateMessageNickNameInfo(
        groupSeq: Long,
        postSeq: Long,
        data: Map<Long, PostCreateDto.MessageNickNameDto>,
        userId: String,
        deleteMessageNickNameSeqs: Set<Long>,
        createInfo: Set<PostCreateDto.MessageNickNameDto>
    ): Map<Long, MessageNickNameDto> {
        val user = userService.getUser(userId)
        postService.updateAbleCheckOrThrow(groupSeq = groupSeq, postSeq = postSeq, userSeq = user.sequence!!)

        postService.messagePostNickNameAdd(postSeq, createInfo)
        postService.updateNickNameInfo(groupSeq, postSeq, userId, data, deleteMessageNickNameSeqs)

        val nickNameMap = postService.getMessageAllNickNameMap(postSeq)
        val userInfoInGroupMap =
            groupService.userInfoInGroup(nickNameMap.filter { it.value.userSeqInGroup != null }.map { it.value.userSeqInGroup!! }.toSet())
        nickNameMap.values.toSet().forEach { value ->
            value.let {
                userInfoInGroupMap[value.userSeqInGroup]?.let {
                    value.userNameInGroup = it.nickname
                    value.imageUri = it.imageUrl
                }
            }

        }
        return nickNameMap
    }

    fun updateBoardPost(groupSeq: Long, postSeq: Long, data: PostCreateDto.Board, userId: String) {
        val user = userService.getUser(userId)
        postService.updateAbleCheckOrThrow(groupSeq = groupSeq, postSeq = postSeq, userSeq = user.sequence!!)
        data.title?.let { if(it.isNotBlank()) postService.updatePost(
            groupSeq,
            postSeq,
            data.title,
            data.tagDisconnected,
            data.tags
        ) }
        postService.updateBoardPost(groupSeq, postSeq, data)

        sendNotifyPostUpdate(groupSeq, postSeq)
        userService.getNotifyNoReadCount(user.sequence)
    }

    private fun sendNotifyPostUpdate(groupSeq: Long, postSeq: Long) {
        val postUserSeqs = postService.getPostUserSeqs(groupSeq, postSeq)
        val group = groupService.getGroupSimpleInfo(groupSeq)
        val post = postService.getPostTitle(groupSeq = groupSeq, postSeq = postSeq)
        val event = NotifyInfo(
            userSeqs = postUserSeqs,
            groupSeq = groupSeq,
            postSeq = postSeq,
            message = "보유하신 " + group.name +"의 잼얘 " + post.title + "이 업데이트되었습니다."
        )
        publisher.publishEvent(event)
    }

    fun deletePost(groupSeq: Long, postSeq: Long, userId: String) {
        val user = userService.getUser(id = userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSeq)

        postService.postCheck(groupSeq, postSeq, user.sequence)

        postService.deletePost(groupSeq, postSeq, user.sequence)
        commentService.deleteCommentByPost(postSeq)
        postService.deleteNoUseTag()
    }

    fun getGroupTags(groupSeq: Long, userId: String, page: Pageable): Slice<TagDto.Simple> {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSeq)

        return postService.getTags(groupSeq, page, user.sequence)
    }

    fun getGroupAllTags(groupSeq: Long, keyword: String, userId: String): List<TagDto.Simple> {
        val user = userService.getUser(userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSeq)

        return postService.getGroupAllTags(groupSeq, keyword)
    }

}
