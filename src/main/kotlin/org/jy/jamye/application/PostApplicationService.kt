package org.jy.jamye.application

import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.PostService
import org.jy.jamye.domain.service.UserService
import org.springframework.stereotype.Service

@Service
class PostApplicationService(private val postService: PostService, private val userService: UserService, private val groupService: GroupService, private val redisClient: RedisClient) {
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
        return post
    }

    fun getPosts(userId: String, groupSequence: Long) : List<PostDto.Detail> {
        val user = userService.getUser(id = userId)
        groupService.userInGroupCheckOrThrow(userSeq = user.sequence!!, groupSeq = groupSequence)
        val posts = postService.getPosts(user.sequence, groupSequence)
        val userInfoMap = groupService.getGroupInUsersNickName(groupSequence, posts.map { it.createdUserSequence })

        posts.forEach { it.createdUserNickName = userInfoMap[it.createdUserSequence] }
        return posts
    }

    fun postLuckyDraw(groupSeq: Long, userId: String): PostDto {
        val luckyDrawMap = redisClient.getLuckyDrawMap()
        val user = userService.getUser(id = userId)
        val userSeq = user.sequence!!
        val count = luckyDrawMap.getOrDefault(userSeq, 0)
        if(count >= 2) {
            throw IllegalArgumentException("뽑기는 하루에 2번까지 가능합니다.")
        }

        val luckyDrawSeq = postService.luckyDraw(groupSeq, userSeq)
        val result =
            postService.getPostTitle(groupSeq = groupSeq, postSeq = luckyDrawSeq)
        val createUserInfo =
            groupService.groupUserInfo(groupSequence = groupSeq, userSequence = result.createdUserSequence!!)
        if(createUserInfo!=null) {
            result.createdUserNickName = createUserInfo.nickname
        }

        return result
    }

    fun createPostMessage(userId: String, post: PostDto, content: List<PostDto.MessagePost>): Long {
        val user = userService.getUser(userId)
        post.createdUserSequence = user.sequence!!
        val sendUserSeqs: Set<Long> = content.filter { it.sendUserInGroupSeq != null }.map { it.sendUserInGroupSeq!! }.toSet()
        groupService.usersInGroupCheckOrThrow(sendUserSeqs, post.groupSequence)

        return postService.createPostMessageType(post, content, user.sequence)
    }

    fun createPostBoard(userId: String, post: PostDto, content: PostDto.BoardPost): Long {
        val user = userService.getUser(userId)
        post.createdUserSequence = user.sequence!!
        return postService.createPostBoardType(user.sequence, post, content)
    }

    fun updateMessagePost(groupSeq: Long, postSeq: Long, userId: String, data: PostDto.MessageUpdate) {
        val user = userService.getUser(userId)
        postService.updateAbleCheckOrThrow(groupSeq = groupSeq, postSeq = postSeq, userSeq = user.sequence!!)

        postService.postUpdate(groupSeq, postSeq, data.message.values, data.nickName, data.deleteMessage)

    }

}
