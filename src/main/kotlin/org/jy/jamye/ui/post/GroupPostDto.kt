package org.jy.jamye.ui.post

data class GroupPostDto (
    val name: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val nickname: String,
    val profileImageUrl: String? = null
) {
    class Invite (
        val groupSequence: Long,
        val inviteCode: String,
        val nickName: String,
        val profileImageUrl: String? = null
    )

    class Update (
        val name: String? = null,
        val description: String? = null,
    )
}