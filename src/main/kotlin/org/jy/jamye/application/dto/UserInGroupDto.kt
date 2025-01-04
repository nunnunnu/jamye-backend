package org.jy.jamye.application.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.jy.jamye.domain.model.Grade
import java.time.LocalDateTime

data class UserInGroupDto (
    val userSequence: Long,
    val groupSequence: Long,
    val grade: Grade,
    var nickname: String,
    var imageUrl: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    val createDate: LocalDateTime = LocalDateTime.now(),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    val updateDate: LocalDateTime = LocalDateTime.now(),
    val groupUserSequence: Long,
){
    data class Simple (
        var nickname: String,
        var imageUrl: String? = null
    )
}