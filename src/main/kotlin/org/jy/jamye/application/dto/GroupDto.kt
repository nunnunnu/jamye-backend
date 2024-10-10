package org.jy.jamye.application.dto

import java.time.LocalDateTime

class GroupDto(
    var name: String,
    var imageUrl: String? = null,
    var description: String? = null,
    val createDate: LocalDateTime? = LocalDateTime.now(),
    val updateDate: LocalDateTime? = LocalDateTime.now(),
    var sequence: Long? = null
){
    data class Detail (
        var name: String,
        var imageUrl: String? = null,
        var description: String? = null,
        val createDate: LocalDateTime = LocalDateTime.now(),
        val updateDate: LocalDateTime = LocalDateTime.now(),
        var sequence: Long,
        val users: List<UserInGroupDto>? = listOf()
    )
}