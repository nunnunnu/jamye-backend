package org.jy.jamye.security

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import org.jy.jamye.common.util.StringUtils.hasText
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JwtFilter(private val jwtTokenProvider: JwtTokenProvider) : GenericFilter() {
    val log: Logger = LoggerFactory.getLogger(JwtFilter::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as? HttpServletRequest
        val token = if (httpRequest != null) {
            resolveToken(httpRequest)
        } else {
            null
        }
        if (token != null && jwtTokenProvider.validateToken(token)) {
            val authentication: Authentication = jwtTokenProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        } else {
            log.debug("토큰 인증 실패")
        }
        chain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7)
        }
        return null
    }
}