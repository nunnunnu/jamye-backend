package org.jy.jamye.Security

import org.jy.jamye.infra.UserRepository
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import javax.naming.AuthenticationException

@Component
class CustomAuthenticationProvider(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationProvider {


    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication {
        val user = userRepository.findById(authentication.name).orElseThrow { throw IllegalArgumentException() }

        val decodePassword: String = authentication.credentials.toString()
        if (passwordEncoder.matches(decodePassword, user.password)) {
            return UsernamePasswordAuthenticationToken(user.username, user.password, user.authorities)
        }
        throw IllegalArgumentException("사용자 인증 실패")
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken::class.java)
    }
}