package org.jy.jamye.ui

import jakarta.servlet.http.HttpServletRequest
import org.jy.jamye.domain.post.service.VisionService
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/file")
class FileController(private val visionService: VisionService) {
    @GetMapping("/{uri}")
    @Throws(Exception::class)
    fun getImage(
        @PathVariable uri: String,
        request: HttpServletRequest,
    ): ResponseEntity<ByteArrayResource> {
        return visionService.getImage(uri, request)
    }
}