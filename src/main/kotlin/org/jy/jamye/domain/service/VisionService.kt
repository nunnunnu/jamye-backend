package org.jy.jamye.domain.service

import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import org.jy.jamye.application.dto.PostDto
import org.jy.jamye.common.util.StringUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList


@Service
class VisionService {
    private var DIR_PATH: String = "/Users/jinhee/image/"

    @Throws(IOException::class, IllegalStateException::class)
    fun saveFile(file: MultipartFile): String? {
        if (file.isEmpty) return null

        val originalName = file.originalFilename // 파일 원래 이름
        val uuid: String = UUID.randomUUID().toString() // 파일 식별자
        val extension = originalName!!.substring(originalName!!.lastIndexOf(".")) // 파일 확장자 추출
        val savedName = uuid + extension // 이미지 파일의 새로운 이름
        val savedPath = DIR_PATH + savedName // 파일 경로

        file.transferTo(File(savedPath)) // local에 파일 저장

        return savedPath
    }

    @Throws(Exception::class)
    fun extractTextFromImageUrl(imgFilePath: String, sendUser: Set<String>): MutableList<PostDto.MessagePost>? {

        val path: Path = Paths.get(imgFilePath)
        val data: ByteArray = Files.readAllBytes(path)
        val imgBytes: ByteString = ByteString.copyFrom(data)

        val img: Image = Image.newBuilder().setContent(imgBytes).build()
        val feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder()
            .addFeatures(feat)
            .setImage(img)
            .build()
        val requests: MutableList<AnnotateImageRequest> = mutableListOf(request)
        val result: MutableList<PostDto.MessagePost> = mutableListOf()

        ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(requests)

            for (res in response.responsesList) {
                if (res.hasError()) {
                    println("Error: ${res.error.message}")
                    return null
                }

                var currentUser: String? = null
                val messageMap: MutableMap<Long, PostDto.MessagePost> = mutableMapOf()
                var sequence = 0L

                // 이미지 전체에서 가장 오른쪽 x좌표
                val maxRightX = res.fullTextAnnotation.pagesList.flatMap { page ->
                    page.blocksList.flatMap { block ->
                        block.paragraphsList.flatMap { paragraph ->
                            paragraph.boundingBox.verticesList.map { it.x }
                        }
                    }
                }.maxOrNull() ?: 0

                res.fullTextAnnotation.pagesList.forEach { page ->
                    page.blocksList.forEach { block ->
                        block.paragraphsList.forEach { paragraph ->
                            val lineText = paragraph.wordsList.joinToString(" ") { word ->
                                word.symbolsList.joinToString("") { it.text }
                            }

                            val lineRightX = paragraph.boundingBox.verticesList.maxOfOrNull { it.x } ?: 0
                            val isRightmost = lineRightX >= maxRightX - 10  // 오른쪽 끝 기준 조정

                            if (sendUser.map { it.replace(" ", "") }.contains(lineText.replace(" ", ""))) {
                                currentUser = sendUser.first { it.replace(" ", "") == lineText.replace(" ", "") }
                                sequence++
                            } else if (sequence == 0L) {
                                messageMap[++sequence] = PostDto.MessagePost(sendUser = currentUser)
                            }

                            val messagePost = messageMap[sequence]
                            if (messagePost == null) {
                                messageMap[sequence] = PostDto.MessagePost(sendUser = currentUser)
                            } else if (lineText.isNotBlank() && !org.h2.util.StringUtils.isNumber(lineText) && !lineText.contains("오전") && !lineText.contains("오후")) {
                                if (isRightmost && ((currentUser == null && sequence != 1L) || (currentUser != null))) {
                                    sequence++
                                    currentUser = null
                                    messageMap[sequence] = PostDto.MessagePost(sendUser = currentUser, message = mutableListOf(lineText), myMessage = true)
                                } else {
                                    messagePost.message.add(lineText)
                                }

                            } else if (lineText.contains("오전") || lineText.contains("오후")) {
                                messagePost.sendDate = lineText
                            }
                        }
                    }
                }

                messageMap.entries.forEach { (key, value) ->
                    val messages = value.message
                    messages.forEachIndexed { index, it ->
                        run {
                            val messages = res.fullTextAnnotation.text.split("\n")
                            for (message in messages) {
                                val replace = it.replace(" ", "")
                                if (replace.length > 5 && message.replace(" ", "").contains(replace.substring(1 until replace.length - 1))) {
                                    value.message[index] = message
                                }
                            }
                        }
                    }
                    result.add(value)
                }
            }

            return result
        }
    }



}
