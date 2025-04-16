package org.jy.jamye.application.dto

import com.fasterxml.jackson.annotation.JsonFormat
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
        val isMaster: Boolean? = null,
        val users: List<UserInGroupDto>? = listOf()
    )
    data class UserInfo(
        var name: String,
        var imageUrl: String? = null,
        var description: String? = null,
        val createDate: LocalDateTime? = null,
        val updateDate: LocalDateTime? = null,
        var groupSequence: Long? = null,
        var userNickName: String,
        val totalUsers: Long
    )
    interface GroupTotalUser {
        val groupSeq: Long
        val totalUser: Long
    }
}

data class DeleteVote(
    var startDateTime: String,
    var endDateTime: String,
    var standardVoteCount: Int,
    var agreeUserSeqs: MutableSet<Long> = mutableSetOf(),
    var disagreeUserSeqs: MutableSet<Long> = mutableSetOf<Long>(),
    var hasRevoted: Boolean
): Serializable {
    var isWaitingDeleteReVoted: Boolean = false
    var isNowVoting: Boolean = true
    var groupName: String? = null
    fun startDateAsLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(startDateTime)  // String을 LocalDateTime으로 변환
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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
    ) {
        this.isNowVoting = false
    }

    data class VoteDto (
            val isNowVoting: Boolean,
            val hasUserInDeletionVote: Boolean
        )
}