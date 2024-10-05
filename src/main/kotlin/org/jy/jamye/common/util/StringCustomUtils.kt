package org.jy.jamye.common.util

import org.springframework.util.StringUtils.hasText

class StringCustomUtils {
    companion object {
        //문자열이 비어있으면 true
        fun StringIsEmpty(data: String): Boolean {
            return !hasText(data)
        }

    }
}