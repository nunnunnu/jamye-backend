package org.jy.jamye.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * REST와 STOMP/SockJS(/ws)가 같은 Origin 목록을 쓰도록 맞춘다.
 * 브라우저 Origin 헤더에는 끝 슬래시가 없으므로 설정값의 trailing slash는 제거한다.
 * file:// 은 Cordova WebView origin이라 슬래시를 유지한다.
 */
@Component
class CorsOriginProperties(
    @Value("\${app.cors.allowed-origins}")
    private val allowedOrigins: String,
) {
    fun exactOrigins(): List<String> = normalized().filter { !it.contains('*') }

    fun wildcardPatterns(): List<String> = normalized().filter { it.contains('*') }

    private fun normalized(): List<String> {
        return allowedOrigins
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { normalize(it) }
            .distinct()
    }

    private fun normalize(origin: String): String {
        if (origin.startsWith("file:")) {
            return origin
        }
        return origin.trimEnd('/')
    }
}
