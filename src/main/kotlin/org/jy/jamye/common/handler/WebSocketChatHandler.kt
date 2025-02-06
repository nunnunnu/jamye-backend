package org.jy.jamye.common.handler

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler


@Component
@RequiredArgsConstructor
class WebSocketChatHandler : TextWebSocketHandler() {
    val log: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    // 소켓 세션을 저장할 Set
    private val sessions: MutableSet<WebSocketSession> = HashSet()

    // 소켓 연결 확인
    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("{} 연결됨", session.id)
        sessions.add(session)
        session.sendMessage(TextMessage("WebSocket 연결 완료"))
    }

    // 소켓 메세지 처리
    @Throws(Exception::class)
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        log.info("payload {}", payload)

        for (s in sessions) {
            s.sendMessage(TextMessage(payload))
        }
    }

    // 소켓 연결 종료
    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        // TODO Auto-generated method stub
        log.info("{} 연결 끊김", session.id)
        sessions.remove(session)
        session.sendMessage(TextMessage("WebSocket 연결 종료"))
    }
}