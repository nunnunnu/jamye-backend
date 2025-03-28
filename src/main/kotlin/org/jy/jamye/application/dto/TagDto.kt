package org.jy.jamye.application.dto

class TagDto {
    data class Simple(
        var tagSeq: Long? = null,
        var tagName: String,
    )
    data class Detail(
        var tagSeq: Long,
        var tagName: String,
        var tagPostConnectionSeq: Long
        )
}