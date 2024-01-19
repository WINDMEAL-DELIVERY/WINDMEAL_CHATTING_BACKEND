package com.windmealchat.global.configuration;

import static com.windmealchat.global.constants.RabbitConstants.AMQ_QUEUE;
import static com.windmealchat.global.constants.RabbitConstants.EXCHANGE;
import static com.windmealchat.global.constants.StompConstants.PUB_PREFIX;
import static com.windmealchat.global.constants.StompConstants.QUEUE;
import static com.windmealchat.global.constants.StompConstants.TOPIC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windmealchat.alarm.service.FcmNotificationService;
import com.windmealchat.global.handler.ClientInboundChannelHandler;
import com.windmealchat.global.handler.HttpHandShakeInterceptor;
import com.windmealchat.global.handler.StompErrorHandler;
import com.windmealchat.global.token.dao.RefreshTokenDAO;
import com.windmealchat.global.token.impl.RefreshTokenDAOImpl;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Value("${spring.rabbitmq.host}")
  private String host;
  @Value("${domain.url.ws_host}")
  private String ws_host;
  @Value("domain.url.local")
  private String local;
  @Value("${domain.url.chat_host}")
  private String chat_host;
  @Value("${domain.url.chat1}")
  private String chat1;
  @Value("${domain.url.chat2}")
  private String chat2;

  private final AES256Util aes256Util;
  private final ObjectMapper objectMapper;
  private final RedisTemplate redisTemplate;
  private final TokenProvider tokenProvider;
  private final FcmNotificationService fcmNotificationService;

  @Bean
  public RefreshTokenDAO refreshTokenDAO() {
    return new RefreshTokenDAOImpl(redisTemplate);
  }

  @Bean
  public ChannelInterceptor channelInterceptor() {
    return new ClientInboundChannelHandler(fcmNotificationService, tokenProvider, objectMapper, aes256Util);
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
    messageBrokerRegistry.setPathMatcher(new AntPathMatcher("."));
    messageBrokerRegistry.setApplicationDestinationPrefixes(PUB_PREFIX);
    messageBrokerRegistry.enableStompBrokerRelay(QUEUE, TOPIC, EXCHANGE, AMQ_QUEUE)
        .setRelayHost(host)
        .setVirtualHost("/")
        .setRelayPort(61613);
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
    stompEndpointRegistry.addEndpoint("/stomp/chat").setAllowedOrigins("*")
//                .setAllowedOrigins(local, chat_host, chat1, chat2, ws_host)
//        .setAllowedOriginPatterns("*")
        .addInterceptors(HttpHandShakeInterceptor());
//                .withSockJS();
    stompEndpointRegistry.setErrorHandler(stompErrorHandler());
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(channelInterceptor());
  }
}
