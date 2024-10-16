package org.jy.jamye.application.dto

import java.time.LocalDateTime

class GroupDto(
    var name: String,
    var imageUrl: String? = null,
    var description: String? = null,
    val createDate: LocalDateTime? = null,
    val updateDate: LocalDateTime? = null,
    var groupSequence: Long? = null
){
    data class Detail (
        var name: String,
        var imageUrl: String? = null,
        var description: String? = null,
        val createDate: LocalDateTime,
        val updateDate: LocalDateTime,
        var groupSequence: Long,
        val users: List<UserInGroupDto>? = listOf()
    )
    data class UserInfo(
        var name: String,
        var imageUrl: String? = null,
        var description: String? = null,
        val createDate: LocalDateTime? = null,
        val updateDate: LocalDateTime? = null,
        var groupSequence: Long? = null,
        var userNickName: String
    )
}