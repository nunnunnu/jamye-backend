package org.jy.jamye.infra

import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.domain.model.Post
import org.jy.jamye.domain.service.PostService
import org.springframework.stereotype.Service

@Service
class PostFactory(
    private val groupRepo: GroupRepository,
    private val userRepo: UserRepository,
    private val groupUserRepo: GroupUserRepository
) {

    fun createPost(data: PostDto): Post {
        if(!groupUserRepo.existsByUserSequenceAndGroupSequence(data.createdUserSequence, data.groupSequence)) {
            throw IllegalStateException("글 작성 권한없음")
        }
        return Post(title = data.title,
            createUserSequence = data.createdUserSequence,
            groupSequence = data.groupSequence)
    }

}
