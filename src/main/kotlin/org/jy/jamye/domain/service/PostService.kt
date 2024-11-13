package org.jy.jamye.domain.service

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.exception.PostAccessDeniedException
import org.jy.jamye.domain.model.Message
import org.jy.jamye.domain.model.Post
import org.jy.jamye.domain.model.PostType
import org.jy.jamye.infra.*
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userGroupPostRepository: UserGroupPostRepository,
    private val postFactory: PostFactory,
    private val messageRepository: MessageRepository,
    private val boardRepository: BoardRepository
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
            postType = post.piType,
            content = if (post.piType == PostType.MSG) messageRepository.findAllByPostSeq(postSequence).map {
                var seq = 0L
                PostDto.MessagePost(
                    sendUserInGroupSeq = it.groupUserSequence,
                    message = mutableListOf(PostDto.MessageSequence(++seq, it.content)),
                    sendDate = it.sendDate.toString(),
                    myMessage = it.nickName == null
                )
            } else PostDto.BoardPost(
                    content = boardRepository.findByPostSeq(postSequence).detail
                )
            )
        return result
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

    fun luckyDraw(groupSeq: Long, userSeq: Long): Long {
        val postSeqs: MutableList<Long> = postRepository.countAllByAbleDrawPool(groupSeq, userSeq)

        val pickPostSeq = postSeqs[(Math.random() * postSeqs.size).toInt()]

        return pickPostSeq
    }

    fun createPostMessageType(data: PostDto, content: List<PostDto.MessagePost>, sequence: Long): Long {
        val messages: MutableList<Message> = mutableListOf()
        val post = postFactory.createPost(data, PostType.MSG)
        postRepository.save(post)
        content.forEach { messages.addAll(postFactory.createPostMessageType(data = it, postSeq = post.postSeq!!)) }

        messageRepository.saveAll(messages)
        return post.postSeq!!
    }

    fun createPostBoardType(userSeq: Long, data: PostDto, detailContent: PostDto.BoardPost): Long {
        val post = postFactory.createPost(data, PostType.BOR)
        postRepository.save(post)
        val content = postFactory.createPostBoardType(detailContent = detailContent, postSeq = post.postSeq!!)
        boardRepository.save(content)
        return post.postSeq!!
    }
}
