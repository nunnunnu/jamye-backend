package org.jy.jamye.common.util

import java.util.concurrent.ThreadLocalRandom

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

    fun generateRandomCode(length: Int): String {
        // 숫자 + 대문자 + 소문자
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder()
        val random: ThreadLocalRandom = ThreadLocalRandom.current()

        for (i in 0 until length) {
            val index: Int = random.nextInt(characters.length)
            sb.append(characters[index])
        }

        return sb.toString()
    }
}