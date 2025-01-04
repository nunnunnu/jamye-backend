package org.jy.jamye.ui.post

import org.springframework.web.multipart.MultipartFile

data class UserInGroupUpdateDto(
    val nickName: String? = null,
    val profile: MultipartFile? = null
) {
}