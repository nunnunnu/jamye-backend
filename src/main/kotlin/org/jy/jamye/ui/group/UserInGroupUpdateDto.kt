package org.jy.jamye.ui.group

import org.springframework.web.multipart.MultipartFile

data class UserInGroupUpdateDto(
    val nickName: String? = null,
    val profile: MultipartFile? = null
) {
}