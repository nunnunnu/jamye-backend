package org.jy.jamye.Security

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import org.jy.jamye.common.util.StringUtils.hasText
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JwtFilter(private val jwtTokenProvider: JwtTokenProvider) : GenericFilter() {


    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val token = resolveToken(request as HttpServletRequest)
        if (token != null && jwtTokenProvider.validateToken(token)) {
            val authentication: Authentication = jwtTokenProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
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