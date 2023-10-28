package com.windmealchat.global.configuration;

import com.windmealchat.global.handler.ClientInboundChannelHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static com.windmealchat.global.constants.StompConstants.PUB;
import static com.windmealchat.global.constants.StompConstants.SUB;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    private final ClientInboundChannelHandler clientInboundChannelHandler;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
        // enableSimpleBroker는 내장 브로커를 활용하겠단 말이다.
        // 매개변수로 전달한 경로들은 메세지를 발행하는 경로이다.
//        messageBrokerRegistry.enableSimpleBroker(QUEUE,TOPIC);
        messageBrokerRegistry.enableSimpleBroker(SUB);
        // 매개변수로 전달한 경로들은 메세지 핸들러로 라우팅되는 경로들이다.
        messageBrokerRegistry.setApplicationDestinationPrefixes(PUB);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        // 클라이언트에서 웹소켓에 접속할 수 있는 엔드포인트를 지정한다.
        // ws://localhost:8080/stomp/chat 의 형태가 될 것이다.
        stompEndpointRegistry.addEndpoint("/stomp/chat")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(clientInboundChannelHandler);
    }
}
