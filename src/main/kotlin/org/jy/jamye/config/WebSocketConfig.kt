package org.jy.jamye.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // 클라이언트가 연결할 수 있는 엔드포인트 설정
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("http://localhost:8081", "http://jamye-frontend.s3-website.ap-northeast-2.amazonaws.com")  // 수정 필요
            .withSockJS() // SockJS 지원
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 메시지 브로커 설정
        registry.enableSimpleBroker("/alarm/receive")  // 구독 경로 설정
    }
}
