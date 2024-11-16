package org.jy.jamye.ui

import org.jy.jamye.application.PostApplicationService
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.service.VisionService
import org.jy.jamye.ui.post.PostCreateDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import java.util.*

@RestController
@RequestMapping("/api/post")
class PostController(
    private val postService: PostApplicationService,
    private val visionService: VisionService,
    private val validationAutoConfiguration: ValidationAutoConfiguration
) {
    var log: Logger = LoggerFactory.getLogger(PostController::class.java.name)
    @GetMapping("/{groupSequence}/{postSequence}")
    fun getPost(@PathVariable("groupSequence") groupSequence: Long, @PathVariable("postSequence") postSequence: Long, @AuthenticationPrincipal user: UserDetails):
            ResponseDto<PostDto.PostContent<Any>> {
        val post = postService.getPost(groupSequence, postSequence, user.username)
        return ResponseDto(data = post, status = HttpStatus.OK)
    }

    @GetMapping("/{groupSequence}")
    fun getPosts(@PathVariable("groupSequence") groupSequence: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<List<PostDto.Detail>> {
        val posts = postService.getPosts(user.username, groupSequence)
        return ResponseDto(data = posts, status = HttpStatus.OK)
    }

    @GetMapping("/lucky-draw/{groupSeq}")
    fun postLuckyDraw(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<PostDto> {
        val post = postService.postLuckyDraw(groupSeq, user.username)
        return ResponseDto(data = post, status = HttpStatus.OK)
    }

    @PostMapping("/message-text")
    fun extractText(@RequestParam image: MultipartFile, @RequestParam sendUser: Set<String>): ResponseDto< MutableMap<Long, PostDto.MessagePost>> {
        val saveFile = visionService.saveFile(image)

        return ResponseDto(data = visionService.extractTextFromImageUrl(saveFile!!, sendUser), status = HttpStatus.OK)
    }

    @PostMapping("/message")
    fun createPostMessageType(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody data: PostCreateDto<MutableMap<Long, PostDto.MessagePost>>
    ): ResponseDto<Long> {
        val sortData = TreeMap(data.content)
        val contents: MutableList<PostDto.MessagePost> = mutableListOf()
        var seq = 0L
        sortData.entries.forEach { (key, value) ->
            value.message.sortBy { it.seq }
            value.message.forEach {
                contents.add(PostDto.MessagePost(
                    message = mutableListOf(PostDto.MessageSequence(
                        seq = ++seq,
                        message = it.message,
                        isReply = it.isReply,
                        replyMessage = it.replyMessage,
                        replyTo = it.replyTo
                    )),
                    sendDate = value.sendDate,
                    sendUser = value.sendUser
                ))
            }

        }
        val postSeq = postService.createPostMessage(userId = user.username, post = PostDto(
            title = data.title,
            groupSequence = data.groupSeq),
            content = contents
        )
        return ResponseDto(data = postSeq, status = HttpStatus.OK)
    }

    @PostMapping("/board")
    fun createPostBoardType(@AuthenticationPrincipal user: UserDetails, @RequestBody data: PostCreateDto<PostCreateDto.Board>): ResponseDto<Long> {
        val postSeq = postService.createPostBoard(userId = user.username, post = PostDto(
            title = data.title,
            groupSequence = data.groupSeq),
            content = PostDto.BoardPost(content = data.content.content)
        )
        return ResponseDto(data = postSeq, status = HttpStatus.OK)
    }
}
