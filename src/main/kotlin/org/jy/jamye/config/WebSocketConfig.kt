package org.jy.jamye.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val corsOriginProperties: CorsOriginProperties,
) : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // SockJS는 allowedOriginPatterns만 보는 경우가 있어 exact origin도 패턴으로 넘긴다.
        val patterns = (corsOriginProperties.exactOrigins() + corsOriginProperties.wildcardPatterns()).distinct()
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns(*patterns.toTypedArray())
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 메시지 브로커 설정
        registry.enableSimpleBroker("/alarm/receive", "/alarm/group/delete")

    }
}
