package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.exception.PostAccessDeniedException
import org.jy.jamye.domain.model.Post
import org.jy.jamye.infra.PostRepository
import org.jy.jamye.infra.UserGroupPostRepository
import org.springframework.stereotype.Service

@Service
class PostService(private val postRepository: PostRepository, private val userGroupPostRepository: UserGroupPostRepository) {
    fun getPost(groupSequence: Long, postSequence: Long, userSequence: Long): PostDto {
        if(!userGroupPostRepository.existsByUserSequenceAndGroupSequenceAndPostSequence(userSequence, groupSequence, postSequence)) {
            throw PostAccessDeniedException()
        }
        val post = getPostOrThrow(groupSequence, postSequence)
        //todo: 게시글 detail 구현 필요
        return PostDto(groupSequence = post.groupSequence,
            postSequence = post.postSequence!!,
            createdUserSequence = post.createUserSequence,
            title = post.title,
            createDate = post.createDate,
            updateDate = post.updateDate
            )
    }

    private fun getPostOrThrow(groupSequence: Long, postSequence: Long): Post {
        return postRepository.findByGroupSequenceAndPostSequence(groupSequence, postSequence).orElseThrow { throw EntityNotFoundException("잘못된 게시글 번호입니다.") }
    }
}
