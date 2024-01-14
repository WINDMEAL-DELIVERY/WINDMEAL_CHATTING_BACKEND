package com.windmealchat.global.handler;

import static com.windmealchat.global.constants.TokenConstants.ALARM_TOKEN;
import static com.windmealchat.global.constants.TokenConstants.AUTHORIZATION_HEADER;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windmealchat.alarm.dto.FcmNotificationRequest;
import com.windmealchat.alarm.service.FcmNotificationService;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientInboundChannelHandler implements ChannelInterceptor {

  private final FcmNotificationService fcmNotificationService;
  private final TokenProvider tokenProvider;
  private final ObjectMapper objectMapper;
  private final AES256Util aes256Util;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    boolean isValid;
    try {
      isValid = stompFilter(message);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return isValid ? message : null;
  }

  /**
   * SEND 타입의 메시지가 전송 성공했을 경우에만 알람을 발생시킨다.
   *
   * @param message
   * @param channel
   * @param sent
   */
  @Override
  public void postSend(Message<?> message, MessageChannel channel, boolean sent) {

    if (!sent) {
      return;
    }

    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    if (StompCommand.SEND.equals(accessor.getCommand())) {
      Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
      String alarmToken = (String) sessionAttributes.get(ALARM_TOKEN);
      String serializedMessageDTO = new String((byte[]) message.getPayload(),
          StandardCharsets.UTF_8);

      try {
        MessageDTO messageDTO = objectMapper.readValue(serializedMessageDTO, MessageDTO.class);
        log.info(messageDTO.getMessage());
        sendNotification(messageDTO, alarmToken);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean stompFilter(Message<?> message) throws JsonProcessingException {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);

    // CONNECT나 SUBSCRIBE가 아니라면 별도의 토큰 검사는 진행하지 않는다.
    if (!StompCommand.CONNECT.equals(accessor.getCommand()) && !StompCommand.SUBSCRIBE.equals(
        accessor.getCommand())) {
      return true;
    }

    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
    String accessToken = (String) sessionAttributes.get(TOKEN);
    String authorizationHeader = accessor.getNativeHeader(AUTHORIZATION_HEADER).get(0);
    String decrypt = aes256Util.decrypt(authorizationHeader)
        .orElseThrow(() -> new MessageDeliveryException("인증 헤더가 존재하지 않습니다."));
    if (!decrypt.equals(accessToken)) {
      throw new MessageDeliveryException("인증 헤더와 토큰이 일치하지 않습니다.");
    }
    Optional<MemberInfoDTO> memberInfoFromToken = tokenProvider.getMemberInfoFromToken(
        accessToken);

    if (memberInfoFromToken.isEmpty()) {
      return false;
    }
    return true;

  }

  private void sendNotification(MessageDTO messageDTO, String token) {
    fcmNotificationService.sendNotification(
        FcmNotificationRequest.of("CHATTING", messageDTO.getMessage()),
        token);
  }
}
