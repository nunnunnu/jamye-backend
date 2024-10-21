package org.jy.jamye.application.dto

import java.io.Serializable
import java.time.LocalDateTime

data class GroupDto(
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

data class DeleteVote(
    var startDateTime: String,
    var endDateTime: String,
    var standardVoteCount: Long,
    var agreeUserSeqs: MutableSet<Long> = mutableSetOf(),
    var disagreeUserSeqs: MutableSet<Long> = mutableSetOf<Long>(),
    var hasRevoted: Boolean
): Serializable {

    fun startDateAsLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(startDateTime)  // String을 LocalDateTime으로 변환
    }

    fun endDateAsLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(endDateTime)  // String을 LocalDateTime으로 변환
    }

    constructor() : this(
        startDateTime = LocalDateTime.now().toString(),  // 기본 값으로 현재 시간을 사용하거나 적절한 기본 값을 설정
        endDateTime = LocalDateTime.now().plusDays(7).toString(),  // 기본 값으로 현재 시간을 사용하거나 적절한 기본 값을 설정
        standardVoteCount = 0,
        agreeUserSeqs = mutableSetOf(),
        disagreeUserSeqs = mutableSetOf(),
        hasRevoted = false
    )
}