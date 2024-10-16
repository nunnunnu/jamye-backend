package org.jy.jamye.common.exception.Custom

import org.jy.jamye.common.exception.ErrorCode
import org.springframework.http.HttpStatus

// 공통 CustomException 클래스
open class BasicException(val errorCode: ErrorCode, detailMessage: String?) : RuntimeException(errorCode.message + if (detailMessage != null) ": $detailMessage" else ""){
    val status: HttpStatus
        get() = errorCode.status
}

// 각 에러에 대한 CustomException
class PasswordErrorException(detailMessage: String? = null) : BasicException(ErrorCode.PASSWORD_ERROR, detailMessage)

class AlreadyJoinedGroupException(detailMessage: String? = null) : BasicException(ErrorCode.ALREADY_JOINED_GROUP, detailMessage)

class DuplicateGroupNicknameException(detailMessage: String? = null) : BasicException(ErrorCode.DUPLICATE_GROUP_NICKNAME, detailMessage)

class MemberNotInGroupException(detailMessage: String? = null) : BasicException(ErrorCode.MEMBER_NOT_IN_GROUP, detailMessage)

class AlreadyRegisteredIdException(detailMessage: String? = null) : BasicException(ErrorCode.ALREADY_REGISTERED_ID, detailMessage)

class DuplicateEmailException(detailMessage: String? = null) : BasicException(ErrorCode.DUPLICATE_EMAIL, detailMessage)

class InvalidInviteCodeException(detailMessage: String? = null) : BasicException(ErrorCode.INVALID_INVITE_CODE, detailMessage)
