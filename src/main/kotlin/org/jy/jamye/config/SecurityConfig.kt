package org.jy.jamye.config

import org.jy.jamye.security.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtFilter: JwtFilter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorizeRequests ->
                authorizeRequests
                    .requestMatchers(
                        "/api/user/login", "/api/user/join", "/v3/api-docs/**", "/", "/error", "/api/user/check/**",
                        "/api/email/**", "/api/file/**", "/api/user/refresh",  "/ws/**",
                        "http://jamye-frontend.s3-website.ap-northeast-2.amazonaws.com"
                    ).permitAll()
                    .anyRequest().hasAnyRole("USER")
            }
            .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
        http
            .headers { headerConfig ->
                headerConfig.frameOptions { frameOptions -> frameOptions.disable() }
            }
            .cors { corsConfigurationSource: CorsConfigurer<HttpSecurity?> -> corsConfigurationSource.configurationSource(corsConfigurationSource()) }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    fun corsConfigurationSource(): CorsConfigurationSource {
        val urlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()

        val corsConfiguration = CorsConfiguration()
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL)
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL)

        corsConfiguration.allowedOrigins = listOf("http://localhost:8081", "http://localhost:8080")
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)

        corsConfiguration.allowCredentials = true
        corsConfiguration.maxAge = 7200 // 2시간(초)

        return urlBasedCorsConfigurationSource
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

}