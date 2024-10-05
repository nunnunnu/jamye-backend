package org.jy.jamye.common.util

import org.jy.jamye.common.util.StringUtils.isStringEmpty

object StringUtils {
    /***
     * 문자열의 blank or null 여부 검증
     * @param 문자열
     * @return 문자열이 blank/null 이면 false
     */
    fun isStringEmpty(data: String?): Boolean {
        return data.isNullOrBlank()
    }
}