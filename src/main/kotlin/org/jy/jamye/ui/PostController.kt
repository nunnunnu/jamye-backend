package org.jy.jamye.ui

import org.jy.jamye.application.PostApplicationService
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.model.PostType
import org.jy.jamye.domain.service.VisionService
import org.jy.jamye.infra.MessageNickNameRepository
import org.jy.jamye.ui.post.PostCreateDto
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@RestController
@RequestMapping("/api/post")
class PostController(
    private val postService: PostApplicationService,
    private val visionService: VisionService,
    private val nickName: MessageNickNameRepository
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
        return ResponseDto(data = visionService.extractTextFromImageUrl(image, sendUser), status = HttpStatus.OK)
    }

    @PostMapping("/message", consumes = ["multipart/form-data"])
    fun createPostMessageType(
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam imageMap: Map<String, MultipartFile>,
        @RequestPart data: PostCreateDto<MutableMap<Long, PostDto.MessagePost>>,
        @RequestPart nickNameMap: Map<String, Long?>
    ): ResponseDto<Long> {
        val sortData = TreeMap(data.content)
        val imageUriMap = imageUriMap(imageMap)
        val replyMap = mutableMapOf<String, Long>()
        val replyKeySeqSet = mutableSetOf<String>()
        data.content.forEach{
            it.value.message.forEach { msg -> replyKeySeqSet.add(msg.replyStringKey()) }
        }
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
                        replyTo = it.replyTo,
                        imageKey = it.imageKey,
                        replyToKey = it.replyToKey,
                        replyToSeq = it.replyToSeq,
                        imageUri = if(it.imageKey.isNotEmpty())
                            it.imageKey.map { imageUriMap.getOrDefault(it, Pair(0L, "") /*수정 필요*/) }.toMutableSet()
                            else mutableSetOf()
                    )),
                    sendDate = value.sendDate,
                    sendUser = value.sendUser
                ))

                replyKeySeqSet.forEach { keySeq ->
                    if(key.toString() + it.seq.toString() == keySeq) {
                         replyMap[keySeq] = seq
                    }
                }
            }

        }
        val postSeq = postService.createPostMessage(userId = user.username, post = PostDto(
                title = data.title,
                groupSequence = data.groupSeq,
                type = PostType.MSG
            ),
            content = contents,
            nickNameMap = nickNameMap,
            replySeqMap = replyMap
        )
        return ResponseDto(data = postSeq, status = HttpStatus.OK)
    }

    @PostMapping("/board")
    fun createPostBoardType(@AuthenticationPrincipal user: UserDetails,
                            @RequestParam imageMap: Map<String, MultipartFile>,
                            @RequestPart data: PostCreateDto<PostCreateDto.Board>): ResponseDto<Long> {
        val imageUriMap = imageUriMap(imageMap)

        data.content.replaceUri(imageUriMap)
        val postSeq = postService.createPostBoard(userId = user.username, post = PostDto(
            title = data.title,
            groupSequence = data.groupSeq,
            type = PostType.BOR),
            content = PostDto.BoardPost(content = data.content.content)
        )
        return ResponseDto(data = postSeq, status = HttpStatus.OK)
    }

    private fun imageUriMap(imageMap: Map<String, MultipartFile>): MutableMap<String, Pair<Long, String>> {
        val imageUriMap = mutableMapOf<String, Pair<Long, String>>()
        if (imageMap.isNotEmpty()) {
            imageMap.forEach { (key, value) ->
                run {
                    val saveFile = visionService.saveFile(value)
                    saveFile?.let {
                        imageUriMap[key] = Pair(0L, saveFile)
                    }
                }
            }
        }
        return imageUriMap
    }

    @PostMapping("/message/{groupSeq}/{postSeq}")
    fun updateMessagePost(
        @PathVariable groupSeq: Long,
        @PathVariable postSeq: Long,
        @AuthenticationPrincipal user: UserDetails,
        @RequestPart data: PostDto.MessageUpdate,
        @RequestParam imageMap: Map<String, MultipartFile>,
    ): ResponseDto<Long> {
        val replyMap = mutableMapOf<String, Long>()
        val replyKeySeqSet = mutableSetOf<String>()
        data.message.forEach{
            it.value.message.forEach { msg -> replyKeySeqSet.add(msg.replyStringKey()) }
        }
        data.message.entries.forEach { (key, value) ->
            value.message.forEach {
                replyKeySeqSet.forEach { keySeq ->
                    if(key.toString() + it.seq.toString() == keySeq) {
                        replyMap[keySeq] = it.seq
                    }
                }
            }

        }
        val imageUriMap = imageUriMap(imageMap)
        data.message.forEach { (_, value) ->
            value.message.forEach {
                it.imageKey.forEach { img -> it.imageUri.add(imageUriMap[img]!!) }
             }
        }
        postService.updateMessagePost(groupSeq, postSeq, user.username, data, replyMap)
        return ResponseDto(data = null, status = HttpStatus.OK)
    }

    @PostMapping("/message/{groupSeq}/{postSeq}/nickNameAdd")
    fun messagePostNickNameAdd(
        @PathVariable groupSeq: Long,
        @PathVariable postSeq: Long,
        @AuthenticationPrincipal user: UserDetails,
        @RequestParam nickName: String,
        @RequestParam(required = false) userSeqInGroup: Long?
    ): ResponseDto<Long> {
        val messageNickNameSeq =
            postService.messagePostNickNameAdd(groupSeq, postSeq, nickName, userSeqInGroup, user.username)
        return ResponseDto(data = messageNickNameSeq, status = HttpStatus.OK)
    }

    @PostMapping("/message/{groupSeq}/{postSeq}/nickName")
    fun messagePostNickNameUpdate(
        @PathVariable groupSeq: Long,
        @PathVariable postSeq: Long,
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody data: PostCreateDto.MessageNickNameUpdate
    ): ResponseDto<Nothing> {
        postService.updateMessageNickNameInfo(groupSeq, postSeq, data.updateInfo, user.username, data.deleteMessageNickNameSeqs)
        return ResponseDto(data = null, status = HttpStatus.OK)
    }

    @PostMapping("/board/{groupSeq}/{postSeq}")
    fun updateBoardPost(
        @PathVariable groupSeq: Long,
        @PathVariable postSeq: Long,
        @AuthenticationPrincipal user: UserDetails,
        @RequestPart data: PostCreateDto.Board,
        @RequestParam imageMap: Map<String, MultipartFile>,
    ): ResponseDto<String> {
        val imageUriMap = imageUriMap(imageMap)
        data.replaceUri(imageUriMap)
        postService.updateBoardPost(groupSeq, postSeq, data, user.username)
        return ResponseDto(data = data.content, status = HttpStatus.OK)
    }
}
