package org.jy.jamye.ui

import org.jy.jamye.application.PostApplicationService
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.service.VisionService
import org.jy.jamye.ui.post.PostCreateMessageDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/post")
class PostController(
    private val postService: PostApplicationService, private val visionService: VisionService
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
    fun extractText(@RequestParam image: MultipartFile, @RequestParam sendUser: Set<String>): ResponseDto<MutableList<PostDto.MessagePost>> {
        val saveFile = visionService.saveFile(image)

        return ResponseDto(data = visionService.extractTextFromImageUrl(saveFile!!, sendUser), status = HttpStatus.OK)
    }

    @PostMapping("/message")
    fun createPostMessageType(@AuthenticationPrincipal user: UserDetails, data: PostCreateMessageDto<List<PostCreateMessageDto.Message>>): ResponseDto<Long> {
        val postSeq = postService.createPostMessage(userId = user.username, post = PostDto(
            title = data.title,
            groupSequence = data.groupSeq),
            content = data.content.map {
                PostDto.MessagePost(
            message = mutableListOf(it.content),
            sendDate = it.sendDate.toString(),
            myMessage = it.sendUserNickName == null)}
        )
        return ResponseDto(data = postSeq, status = HttpStatus.OK)
    }

    @PostMapping("/board")
    fun createPostBoardType(@AuthenticationPrincipal user: UserDetails, data: PostCreateMessageDto<PostCreateMessageDto.Board>): ResponseDto<Long> {
        val postSeq = postService.createPostBoard(userId = user.username, post = PostDto(
            title = data.title,
            groupSequence = data.groupSeq),
            content = PostDto.BoardPost(content = data.content.content)
        )
        return ResponseDto(data = postSeq, status = HttpStatus.OK)
    }
}
