package com.lgcns.pipeline.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 메시지 브로커 중개 전달
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/broadcast");    // for WebSocket
        registry.addEndpoint("/broadcast")     // for SockJS
//                .setAllowedOrigins("http://localhost:3000", // 서버포트 8080
//                        "http://localhost:5173")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(60_000); // 1분마다 소켓 체크(클라이언트가)
    }
    // 휴면 상태는 연결 종료
    // 브라우저에 접근 시 다시 재연결 요청 (브라우저에 의해)
    // 연결은 가벼우나 프로토콜(Http 기반)은 무거운 편
    // http 대신 SockJs 로 간편하게? 가능? // websocket 기반 대신 sockJs 기반으로 접속 가능?? node..

    // 
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");    // topic 이라는 이름으로 발행, 구독
        config.setApplicationDestinationPrefixes("/app");
    }

}


//메시지 실행
//        서버가 /topic 으로 메시지 전달 broadCast
//        client 들은 이 topic 을 구독 중
//        메시지를 받을 수 있다.
