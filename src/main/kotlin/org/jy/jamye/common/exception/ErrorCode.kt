package org.jy.jamye.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val message: String) {
    PASSWORD_ERROR(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    ALREADY_JOINED_GROUP(HttpStatus.CONFLICT, "이미 그룹에 가입된 상태입니다."),
    DUPLICATE_GROUP_NICKNAME(HttpStatus.CONFLICT, "이미 그룹에 등록된 닉네임입니다."),
    MEMBER_NOT_IN_GROUP(HttpStatus.NOT_FOUND, "그룹에 존재하지 않는 회원입니다."),
    ALREADY_REGISTERED_ID(HttpStatus.CONFLICT, "이미 가입된 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "존재하지 않는 초대코드입니다.");
}
