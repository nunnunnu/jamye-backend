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
        return PostDto(groupSequence = post.groupSeq,
            postSequence = post.postSeq!!,
            createdUserSequence = post.userSeq,
            title = post.title,
            createDate = post.createDate,
            updateDate = post.updateDate
            )
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
}
