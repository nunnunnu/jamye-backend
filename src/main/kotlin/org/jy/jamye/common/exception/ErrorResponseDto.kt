package org.jy.jamye.common.exception

import java.time.LocalDateTime

class ErrorResponseDto(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String? = null,
    val message: String? = null
) {
}