package org.jy.jamye.common.util

object StringUtils {
    /***
     * 문자열의 blank or null 여부 검증
     * @param 문자열
     * @return 문자열이 blank/null 이면 false
     */
    fun isEmpty(data: String?): Boolean {
        return data.isNullOrBlank()
    }

    fun hasText(data: String?): Boolean {
        return !data.isNullOrBlank()
    }
}