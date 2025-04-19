package org.jy.jamye.ui.post

import org.jy.jamye.application.post.PostApplicationService
import org.jy.jamye.application.post.dto.MessageNickNameDto
import org.jy.jamye.application.post.dto.PostDto
import org.jy.jamye.application.post.dto.TagDto
import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.post.model.PostType
import org.jy.jamye.domain.post.service.PostService
import org.jy.jamye.domain.post.service.VisionService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import java.util.*

@RestController
@RequestMapping("/api/post")
class PostController(
    private val postAppService: PostApplicationService,
    private val postService: PostService,
    private val visionService: VisionService,
) {
    var log: Logger = LoggerFactory.getLogger(PostController::class.java.name)
    @Value("\${image.url}")
    var imageUrl: String? = null

    @GetMapping("/{groupSequence}/{postSequence}")
    fun getPost(@PathVariable("groupSequence") groupSequence: Long, @PathVariable("postSequence") postSequence: Long, @AuthenticationPrincipal user: UserDetails):
            ResponseDto<PostDto.PostContent<Any>> {
        val post = postAppService.getPost(groupSequence, postSequence, user.username)
        return ResponseDto(data = post, status = HttpStatus.OK)
    }

    @GetMapping("/title/{groupSequence}/{postSequence}")
    fun getPostTitle(@PathVariable("groupSequence") groupSequence: Long, @PathVariable("postSequence") postSequence: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<PostDto> {
        val post = postService.getPostTitle(groupSequence, postSequence)
        return ResponseDto(data = post, status = HttpStatus.OK)
    }

    @GetMapping("/{groupSequence}")
    fun getPosts(
        @PathVariable("groupSequence") groupSequence: Long,
        @AuthenticationPrincipal user: UserDetails,
        @PageableDefault(size = 5, sort = ["createDate"], direction = Sort.Direction.DESC) page: Pageable,
        @RequestParam keyword: String?,
        @RequestParam tagSeqs: Set<Long> = setOf(),
        @RequestParam types: Set<PostType> = setOf(),
    ): ResponseDto<Page<PostDto.Detail>> {
        val posts = postAppService.getPosts(user.username, groupSequence, page, keyword, tagSeqs, types)
        return ResponseDto(data = posts, status = HttpStatus.OK)
    }

    @GetMapping("/lucky-draw/{groupSeq}")
    fun postLuckyDraw(@PathVariable("groupSeq") groupSeq: Long, @AuthenticationPrincipal user: UserDetails): ResponseDto<PostDto> {
        val post = postAppService.postLuckyDraw(groupSeq, user.username)
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
        @RequestPart nickNameMap: Map<String, Long?>,
        @RequestPart tags: List<TagDto.Simple> = listOf()
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
                contents.add(
                    PostDto.MessagePost(
                    message = mutableListOf(
                        PostDto.MessageSequence(
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
        val postSeq = postAppService.createPostMessage(userId = user.username, post = PostDto(
                title = data.title,
                groupSequence = data.groupSeq,
                type = PostType.MSG
            ),
            content = contents,
            nickNameMap = nickNameMap,
            replySeqMap = replyMap,
            tags
        )
        return ResponseDto(data = postSeq, status = HttpStatus.OK)
    }

    @PostMapping("/board")
    fun createPostBoardType(@AuthenticationPrincipal user: UserDetails,
                            @RequestParam imageMap: Map<String, MultipartFile>,
                            @RequestPart data: PostCreateDto<PostCreateDto.Board>
    ): ResponseDto<Long> {
        val imageUriMap = imageUriMap(imageMap)

        data.content.replaceUri(imageUriMap, imageUrl)
        val postSeq = postAppService.createPostBoard(userId = user.username, post = PostDto(
            title = data.title,
            groupSequence = data.groupSeq,
            type = PostType.BOR),
            content = PostDto.BoardPost(content = data.content.content),
            data.tags
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
        postAppService.updateMessagePost(groupSeq, postSeq, user.username, data, replyMap)
        return ResponseDto(data = null, status = HttpStatus.OK)
    }

    @PostMapping("/message/{groupSeq}/{postSeq}/nickName")
    fun messagePostNickNameUpdate(
        @PathVariable groupSeq: Long,
        @PathVariable postSeq: Long,
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody data: PostCreateDto.MessageNickNameUpdate
    ): ResponseDto<Map<Long, MessageNickNameDto>> {
        val nickNameMap = postAppService.updateMessageNickNameInfo(
            groupSeq,
            postSeq,
            data.updateInfo,
            user.username,
            data.deleteMessageNickNameSeqs,
            data.createInfo
        )
        return ResponseDto(data = nickNameMap, status = HttpStatus.OK)
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
        data.replaceUri(imageUriMap, imageUrl)
        postAppService.updateBoardPost(groupSeq, postSeq, data, user.username)
        return ResponseDto(data = data.content, status = HttpStatus.OK)
    }

    @DeleteMapping("/{groupSeq}/{postSeq}")
    fun deletePost(@PathVariable groupSeq: Long,
                   @PathVariable postSeq: Long,
                   @AuthenticationPrincipal user: UserDetails) {
        postAppService.deletePost(groupSeq, postSeq, user.username)
    }

    @GetMapping("/tag/{groupSeq}")
    fun searchGroupTag(@PathVariable groupSeq: Long,
                        @AuthenticationPrincipal user: UserDetails,
                       @PageableDefault(size = 10, sort = ["postUseTotalCount"], direction = Sort.Direction.DESC) page: Pageable,
    ): ResponseDto<Slice<TagDto.Simple>> {
        val tags = postAppService.getGroupTags(groupSeq, user.username, page)
        return ResponseDto(data = tags)
    }

    @GetMapping("/tag/all/{groupSeq}")
    fun searchGroupAllTag(@PathVariable groupSeq: Long,
                       @RequestParam keyword: String,
                        @AuthenticationPrincipal user: UserDetails,
    ): ResponseDto<List<TagDto.Simple>> {
        val tags = postAppService.getGroupAllTags(groupSeq, keyword, user.username)
        return ResponseDto(data = tags)
    }
}
