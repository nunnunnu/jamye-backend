package org.jy.jamye.application.group

import com.fasterxml.jackson.annotation.JsonFormat
import org.jy.jamye.application.user.dto.UserInGroupDto
import java.io.Serializable
import java.time.LocalDateTime
import kotlin.math.ceil

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
    var isNowVoting: Boolean = true
    fun addVote(type: VoteType, userSeq: Long) {
        if(type == VoteType.AGREE) this.agreeUserSeqs.add(userSeq)
        else this.disagreeUserSeqs.add(userSeq)
    }
    fun alreadyVoteCheck(userSeq: Long): Boolean {
        return (this.agreeUserSeqs.contains(userSeq) || this.disagreeUserSeqs.contains(userSeq))
    }

    fun endDateAsLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(endDateTime)  // String을 LocalDateTime으로 변환
    }

    fun resultCheck(): Boolean {
        return this.agreeUserSeqs.size >= (ceil(this.standardVoteCount.toDouble()/2))
    }

    enum class VoteType {
        AGREE, DISAGREE
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

    data class Detail(
        val deleteVote: DeleteVote? = null,
        val userSeq: Long,
        var hasUserInDeletionVote: Boolean? = null,
        var isWaitingDeleteReVoted: Boolean? = null,
        val alreadyVoteCheck: Boolean = deleteVote?.alreadyVoteCheck(userSeq) ?: false,
        val groupName: String? = null,
        val groupSeq: Long? = null,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        val endDateAsLocalDateTime: LocalDateTime? = deleteVote?.endDateAsLocalDateTime()
    ) {
    }
}