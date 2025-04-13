package org.jy.jamye.common.exception

import com.google.api.Http
import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val message: String) {
    PASSWORD_ERROR(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    ALREADY_JOINED_GROUP(HttpStatus.CONFLICT, "이미 그룹에 가입된 상태입니다."),
    DUPLICATE_GROUP_NICKNAME(HttpStatus.CONFLICT, "이미 그룹에 등록된 닉네임입니다."),
    MEMBER_NOT_IN_GROUP(HttpStatus.BAD_REQUEST, "그룹에 존재하지 않는 회원입니다."),
    ALREADY_REGISTERED_ID(HttpStatus.CONFLICT, "이미 가입된 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "존재하지 않는 초대코드입니다."),
    GROUP_DELETION_PERMISSION(HttpStatus.BAD_REQUEST, "그룹 개설자만 그룹을 삭제 가능합니다."),
    POST_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "보유하지않은 게시글입니다."),
    ALREADY_DELETE_VOTE(HttpStatus.BAD_REQUEST, "이미 삭제 투표 진행중인 그룹입니다."),
    ALL_POSTS_ALREADY_OWNED(HttpStatus.BAD_REQUEST, "더 이상 뽑을 수 있는 잼얘가 존재하지 않습니다."),
    NON_EXISTENT_USER(HttpStatus.UNAUTHORIZED, "없는 유저 번호를 입력하셨습니다."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해주세요."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알수없는 오류입니다. 운영자에게 문의해주세요.")
}
