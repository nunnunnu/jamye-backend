package org.jy.jamye.ui

import org.jy.jamye.application.PostApplicationService
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.service.VisionService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/api/post")
class PostController(
    private val postService: PostApplicationService, private val visionService: VisionService
) {
    @GetMapping("/{groupSequence}/{postSequence}")
    fun getPost(@PathVariable("groupSequence") groupSequence: Long, @PathVariable("postSequence") postSequence: Long, @AuthenticationPrincipal user: UserDetails):
            ResponseDto<PostDto> {
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
    fun extractText(@RequestParam image: MultipartFile): String {
        return try {
            val saveFile = visionService.saveFile(image)
            visionService.extractTextFromImageUrl(saveFile!!)
        } catch (e: Exception) {
            //e.printStackTrace();
            "Failed to extract text: " + e.message
        }
    }
}
