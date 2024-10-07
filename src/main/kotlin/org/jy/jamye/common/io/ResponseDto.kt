package org.jy.jamye.common.io

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ResponseDto<T> (
    val message: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val time: LocalDateTime = LocalDateTime.now(),
    var data: T? = null,
    val status: HttpStatus
){
}