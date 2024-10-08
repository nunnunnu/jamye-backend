package org.jy.jamye.Security

import jakarta.persistence.EntityNotFoundException
import org.jy.jamye.infra.UserRepository
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.security.core.AuthenticationException

@Component
class CustomAuthenticationProvider(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationProvider {


    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication {
        val user = userRepository.findByUserId(authentication.name).orElseThrow { throw EntityNotFoundException() }

        val decodePassword: String = authentication.credentials.toString()
        if (passwordEncoder.matches(decodePassword, user.password)) {
            return UsernamePasswordAuthenticationToken(user.username, null, user.authorities)
        }
        throw BadCredentialsException("사용자 인증 실패")
    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}