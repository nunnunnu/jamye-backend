package org.jy.jamye.domain.service

import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicReference
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
    fun extractTextFromImageUrl(imgFilePath: String): String {

        val path: Path = Paths.get(imgFilePath)
        val data: ByteArray = Files.readAllBytes(path)
        val imgBytes: ByteString = ByteString.copyFrom(data)

        val img: Image = Image.newBuilder().setContent(imgBytes).build()
        val feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder()
            .addFeatures(feat)
            .setImage(img)
            .build()
        val requests: MutableList<AnnotateImageRequest> = ArrayList()
        requests.add(request)

        ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(requests)
            val stringBuilder = StringBuilder()

            for (res in response.responsesList) {
                if (res.hasError()) {
                    println("Error: ${res.error.message}")
                    return "Error detected"
                }

//                stringBuilder.append("텍스트: ").append(res.fullTextAnnotation.text).append("\n")

                for (page in res.fullTextAnnotation.pagesList) {
                    for (block in page.blocksList) {
                        for (paragraph in block.paragraphsList) {
                            for (word in paragraph.wordsList) {
                                val wordText = word.symbolsList.joinToString("") { it.text }
                                stringBuilder.append("단어: ").append(wordText).append("\n")

                                // 각 단어의 위치 좌표 출력
//                                word.boundingBox.verticesList.forEachIndexed { index, vertex ->
//                                    stringBuilder.append("Vertex $index: (${vertex.x}, ${vertex.y})\n")
//                                }
                            }
                        }
                    }
                }
            }

            return stringBuilder.toString()
        }
    }

}
