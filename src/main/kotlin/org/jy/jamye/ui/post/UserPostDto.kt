package org.jy.jamye.ui.post

import jakarta.persistence.Column

class UserPostDto (
    val email: String,
    val id: String,
    val nickname: String,
    var password: String
)
