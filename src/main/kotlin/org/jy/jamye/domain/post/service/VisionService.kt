package org.jy.jamye.domain.post.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.IOUtils
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import jakarta.servlet.http.HttpServletRequest
import org.jy.jamye.application.post.dto.PostDto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.URLEncoder
import java.util.*


@Service
class VisionService(private val s3Client: AmazonS3) {
    @Value("\${cloud.aws.s3.bucket}")
    var bucket: String? = null
//    private var DIR_PATH: String = "/Users/jinhee/image/"

    @Throws(IOException::class, IllegalStateException::class)
    fun saveFile(file: MultipartFile): String? {
        if (file.isEmpty) return null

        val originalName = file.originalFilename // 파일 원래 이름
        val uuid: String = UUID.randomUUID().toString() // 파일 식별자
        val extension = originalName!!.substring(originalName.lastIndexOf(".")) // 파일 확장자 추출
        val savedName = uuid + extension // 이미지 파일의 새로운 이름

        val metadata = ObjectMetadata()
        metadata.contentType = file.contentType
        metadata.contentLength = file.size

        s3Client.putObject(bucket, savedName, file.inputStream, metadata)
        return savedName
    }

    @Throws(Exception::class)
    fun extractTextFromImageUrl(image: MultipartFile, sendUser: Set<String>): MutableMap<Long, MessagePost>? {
        val imgBytes = image.bytes
        val img = Image.newBuilder().setContent(ByteString.copyFrom(imgBytes)).build()
        val feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder()
            .addFeatures(feat)
            .setImage(img)
            .build()

        val requests: MutableList<AnnotateImageRequest> = mutableListOf(request)
        val result: MutableMap<Long, MessagePost> = mutableMapOf()

        ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(requests)

            for (res in response.responsesList) {
                if (res.hasError()) {
                    println("Error: ${res.error.message}")
                    return null
                }

                var currentUser: String? = null
                val messageMap: MutableMap<Long, MessagePost> = mutableMapOf()
                var sequence = 0L

                // 이미지 전체에서 가장 오른쪽 x좌표
                val maxRightX = res.fullTextAnnotation.pagesList.flatMap { page ->
                    page.blocksList.flatMap { block ->
                        block.paragraphsList.flatMap { paragraph ->
                            paragraph.boundingBox.verticesList.map { it.x }
                        }
                    }
                }.maxOrNull() ?: 0
                var isReply = false
                var replyTo = ""
                res.fullTextAnnotation.pagesList.forEach { page ->
                    page.blocksList.forEach { block ->
                        block.paragraphsList.forEach { paragraph ->
                            var lineText = paragraph.wordsList.joinToString(" ") { word ->
                                word.symbolsList.joinToString("") { it.text }
                            }

                            val lineRightX = paragraph.boundingBox.verticesList.maxOfOrNull { it.x } ?: 0
                            val isRightmost = lineRightX >= maxRightX - 80  // 오른쪽 끝 기준 조정
                            val spaceRemoveNickname = sendUser.map { it.replace(" ", "") }
                            val spaceRemoveLineText = lineText.replace(" ", "")
                            if (spaceRemoveNickname.contains(spaceRemoveLineText)) {
                                currentUser = sendUser.first { it.replace(" ", "") == spaceRemoveLineText }
                                sequence++
                            } else {
                                for (it in spaceRemoveNickname) {
                                    if (!lineText.endsWith("에게 답장") && spaceRemoveLineText.startsWith(it)) {
                                        currentUser = sendUser.first { nickName -> nickName.replace(" ", "") == it}
                                        sequence++
                                        lineText = spaceRemoveLineText.replace(it, "")
                                        break
                                    }
                                }
                            }
                            if (sequence == 0L) {
                                messageMap[++sequence] = MessagePost(sendUser = currentUser)
                            }

                            val messagePost = messageMap[sequence]
                            if(lineText.endsWith("에게 답장")) {
                                isReply = true
                                replyTo = lineText.replace("에게 답장", "").replace(" ", "")
                            } else {
                                if (messagePost == null) {
                                    val currentUserMessage = MessagePost(sendUser = currentUser)
                                    if(isReply) {
                                        currentUserMessage.message.add(MessageSequence(1L, replyTo = replyTo, isReply = true))
                                    }
                                    messageMap[sequence] = currentUserMessage
                                } else if (lineText.isNotBlank() && !org.h2.util.StringUtils.isNumber(lineText) && !lineText.contains("오전") && !lineText.contains("오후")) {
                                    if (isRightmost && ((currentUser == null && sequence != 1L) || (currentUser != null))) {
                                        sequence++
                                        currentUser = null
                                        if (isReply) {
                                            messageMap[sequence] = MessagePost(message = mutableListOf(MessageSequence(
                                                seq = 1L, replyMessage = lineText, isReply = true, replyTo = replyTo)), myMessage = true)
                                            replyTo = ""
                                            isReply = false
                                        } else {
                                            if(messagePost.message.isNotEmpty() && messagePost.message.last().isReply == true) {
                                                messagePost.message.last().message = lineText
                                            } else {
                                                messageMap[sequence] = MessagePost(message = mutableListOf(
                                                    MessageSequence(1L, lineText)
                                                ), myMessage = true)
                                            }

                                        }
                                    } else {
                                        val last = if (messagePost.message.isEmpty()) 1 else messagePost.message.last().seq + 1
                                        if(isReply){
                                            messagePost.message.add(MessageSequence(seq = last, message = lineText, isReply = true, replyMessage = lineText, replyTo = replyTo))
                                            isReply = false
                                        } else {
                                            if(messagePost.message.isNotEmpty() && messagePost.message.last().isReply == true) {
                                                val message = messagePost.message.last()
                                                if(message.replyMessage == null) {
                                                    message.replyMessage = lineText
                                                } else {
                                                    message.message = lineText
                                                }
                                            } else {
                                                messagePost.message.add(MessageSequence(last, lineText))
                                            }
                                        }
                                    }
                                } else if (lineText.contains("오전") || lineText.contains("오후")) {
                                    println(lineText)
                                    println(lineText.contains("오전") || lineText.contains("오후"))
                                    println(lineText.contains("오전"))
                                    println(lineText.contains("오후"))
                                    messagePost.sendDate = lineText
                                }
                            }
                        }

                    }
                }
                var sendUser: String? = null
                var seqKey = 0L
                messageMap.entries.forEach { (key, value) ->
                    val messages = value.message
                    if(seqKey == 0L || sendUser != value.sendUser) {
                        sendUser = value.sendUser
                        seqKey++
                    }
                    if(messages.isNotEmpty()) {
                        messages.forEachIndexed { index, it ->
                            run {
                                val originMessage = res.fullTextAnnotation.text.split("\n")
                                for (message in originMessage) {
                                    if(it.message != null) {
                                        val replace = it.message!!.replace(" ", "")
                                        if (replace.length > 5 && message.replace(" ", "").contains(replace.substring(1 until replace.length - 1))) {
                                            value.message[index] = MessageSequence(it.seq, message)
                                        }
                                    }

                                }
                            }
                        }
                        if(result.containsKey(seqKey)) {
                            var seqMax: Long = result[seqKey]!!.message.maxBy { it.seq }.seq
                            value.message.forEach { it.seq = ++seqMax }
                            result[seqKey]!!.message.addAll(value.message)
                        } else {
                            result[seqKey] = value
                        }

                    }

                }
            }

            return result
        }
    }

    @Throws(java.lang.Exception::class)
    fun getImage(
        @PathVariable uri: String,
        request: HttpServletRequest,
    ): ResponseEntity<ByteArrayResource> {
        val o = s3Client.getObject(GetObjectRequest(bucket, "$uri"))
        val objectInputStream = o.objectContent
        val bytes: ByteArray = IOUtils.toByteArray(objectInputStream)

        val resource = ByteArrayResource(bytes)
        val split = uri.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val ext = split[split.size - 1]
        val exportName = "$uri.$ext"
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ("attachment; filename=\"" + URLEncoder.encode(exportName, "UTF-8")).toString() + "\""
            )
            .body(resource)
    }

}
