package org.jy.jamye.common.exception

import org.springframework.http.HttpStatus

open class BasicException(
    val errorCode: ErrorCode,
    detailMessage: String?
) : RuntimeException(
    if (detailMessage != null) "${errorCode.message}: $detailMessage" else errorCode.message
) {
    val status: HttpStatus
        get() = errorCode.status
}

class PasswordErrorException(detailMessage: String? = null) : BasicException(ErrorCode.PASSWORD_ERROR, detailMessage)

class AlreadyJoinedGroupException(detailMessage: String? = null) : BasicException(ErrorCode.ALREADY_JOINED_GROUP, detailMessage)

class DuplicateGroupNicknameException(detailMessage: String? = null) : BasicException(ErrorCode.DUPLICATE_GROUP_NICKNAME, detailMessage)

class MemberNotInGroupException(detailMessage: String? = null) : BasicException(ErrorCode.MEMBER_NOT_IN_GROUP, detailMessage)

class AlreadyRegisteredIdException(detailMessage: String? = null) : BasicException(ErrorCode.ALREADY_REGISTERED_ID, detailMessage)

class DuplicateEmailException(detailMessage: String? = null) : BasicException(ErrorCode.DUPLICATE_EMAIL, detailMessage)

class InvalidInviteCodeException(detailMessage: String? = null) : BasicException(ErrorCode.INVALID_INVITE_CODE, detailMessage)

class GroupDeletionPermissionException(detailMessage: String? = null) : BasicException(ErrorCode.GROUP_DELETION_PERMISSION, detailMessage)

class PostAccessDeniedException(detailMessage: String? = null) : BasicException(ErrorCode.POST_ACCESS_DENIED, detailMessage)

class AlreadyDeleteVoting(detailMessage: String? = null) : BasicException(ErrorCode.ALREADY_DELETE_VOTE, detailMessage)

class AllPostsAlreadyOwnedException(detailMessage: String? = null) : BasicException(ErrorCode.ALL_POSTS_ALREADY_OWNED, detailMessage)

class NonExistentUser(detailMessage: String? = null) : BasicException(ErrorCode.NON_EXISTENT_USER, detailMessage)