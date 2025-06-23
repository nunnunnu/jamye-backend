package org.jy.jamye.ui.post

import jakarta.validation.constraints.NotBlank

data class CommentPostDto(
    @field:NotBlank
    val comment: String,
    val replySeq: Long?
) {
}
