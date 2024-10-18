package org.jy.jamye.application

import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.PostService
import org.jy.jamye.domain.service.UserService
import org.springframework.stereotype.Service

@Service
class PostApplicationService(private val postService: PostService, private val userService: UserService, private val groupService: GroupService) {
    fun getPost(groupSequence: Long, postSequence: Long, userId: String): PostDto {
        val user = userService.getUser(id = userId)
        groupService.userInGroupCheckOrThrow(userSequence = user.sequence!!, groupSequence = groupSequence)

        val post = postService.getPost(
            groupSequence = groupSequence,
            postSequence = postSequence,
            userSequence = user.sequence
        )
        val createUserInfo =
            groupService.groupUserInfo(groupSequence = groupSequence, userSequence = post.createdUserSequence)
        if(createUserInfo!=null) {
            post.createdUserNickName = createUserInfo.nickname
        }
        return post
    }

}
