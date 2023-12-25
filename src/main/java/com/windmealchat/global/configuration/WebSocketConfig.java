package com.windmealchat.global.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windmealchat.global.handler.ClientInboundChannelHandler;
import com.windmealchat.global.handler.HttpHandShakeInterceptor;
import com.windmealchat.global.handler.StompErrorHandler;
import com.windmealchat.global.token.dao.RefreshTokenDAO;
import com.windmealchat.global.token.impl.RefreshTokenDAOImpl;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import static com.windmealchat.global.constants.RabbitConstants.AMQ_QUEUE;
import static com.windmealchat.global.constants.RabbitConstants.EXCHANGE;
import static com.windmealchat.global.constants.StompConstants.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AES256Util aes256Util;
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final TokenProvider tokenProvider;

    @Bean
    public RefreshTokenDAO refreshTokenDAO() {
        return new RefreshTokenDAOImpl(redisTemplate);
    }

    @Bean
    public ChannelInterceptor channelInterceptor() {
        return new ClientInboundChannelHandler(tokenProvider, objectMapper);
    }

    @Bean
    public StompSubProtocolErrorHandler stompErrorHandler() {
        return new StompErrorHandler();
    }

    @Bean
    public HandshakeInterceptor HttpHandShakeInterceptor() {
        return new HttpHandShakeInterceptor(aes256Util, tokenProvider, refreshTokenDAO());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
        // enableSimpleBroker는 내장 브로커를 활용하겠단 말이다.
        // 추후 외부의 메세지 브로커로 변경할 예정이다.
        // 매개변수로 전달한 경로들은 메세지를 발행하는 경로이다.
//        messageBrokerRegistry.enableSimpleBroker(QUEUE,TOPIC);

//        messageBrokerRegistry.enableSimpleBroker(QUEUE);
        // 매개변수로 전달한 경로들은 메세지 핸들러로 라우팅되는 경로들이다.
//         messageBrokerRegistry.setApplicationDestinationPrefixes(TOPIC);
        messageBrokerRegistry.setPathMatcher(new AntPathMatcher("."));
        messageBrokerRegistry.setApplicationDestinationPrefixes(PUB_PREFIX);
//        messageBrokerRegistry.enableSimpleBroker(SUB_PREFIX);
        messageBrokerRegistry.enableStompBrokerRelay(QUEUE, TOPIC, EXCHANGE, AMQ_QUEUE);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        // 클라이언트에서 웹소켓에 접속할 수 있는 엔드포인트를 지정한다.
        // ws://localhost:8080/stomp/chat 의 형태가 될 것이다.
        stompEndpointRegistry.addEndpoint("/stomp/chat")
//                .setAllowedOrigins("http://localhost:8081", "http://localhost:3000", "http://localhost:8080")
                .setAllowedOriginPatterns("*")
                .addInterceptors(HttpHandShakeInterceptor());
//                .withSockJS();
        stompEndpointRegistry.setErrorHandler(stompErrorHandler());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor());
    }
}
