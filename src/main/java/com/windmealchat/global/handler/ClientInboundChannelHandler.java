package com.windmealchat.global.handler;

import static com.windmealchat.global.constants.ExceptionConstants.DESERIALIZATION_EXCEPTION;
import static com.windmealchat.global.constants.ExceptionConstants.EXPIRED_JWT;
import static com.windmealchat.global.constants.ExceptionConstants.INVALID_ACCESS_TOKEN;
import static com.windmealchat.global.constants.ExceptionConstants.INVALID_STOMP_AUTHORIZATION_HEADER;
import static com.windmealchat.global.constants.ExceptionConstants.NOT_EXISTING_STOMP_AUTHORIZATION_HEADER;
import static com.windmealchat.global.constants.ExceptionConstants.NOT_MATCHING_TOKEN;
import static com.windmealchat.global.constants.TokenConstants.ALARM_TOKEN;
import static com.windmealchat.global.constants.TokenConstants.AUTHORIZATION_HEADER;
import static com.windmealchat.global.constants.TokenConstants.NICKNAME_KEY;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windmealchat.alarm.dto.FcmNotificationRequest;
import com.windmealchat.alarm.service.FcmNotificationService;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import io.jsonwebtoken.ExpiredJwtException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientInboundChannelHandler implements ChannelInterceptor {

  private final FcmNotificationService fcmNotificationService;
  private final TokenProvider tokenProvider;
  private final ObjectMapper objectMapper;
  private final AES256Util aes256Util;

  /**
   * 메시지를 전송하기 전 검증을 수행한다. <br/> 예외가 발생하면 예외 메시지를 커스텀한 뒤
   * {@link com.windmealchat.global.handler.StompErrorHandler} 에게 전달한다.
   *
   * @param message
   * @param channel
   * @return
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    boolean isValid;
    try {
      isValid = verify(message);
    } catch (ExpiredJwtException jwt) {
      throw new MessageDeliveryException(EXPIRED_JWT);
    } catch (Exception e) {
      throw new MessageDeliveryException(e.getMessage());
    }
    return isValid ? message : null;
  }

  /**
   * 메시지가 전송된 후 성공 여부와 관계 없이 호출된다. <br/> 여기서 SEND 타입의 메시지가 전송 성공했을 경우에만 알람을 발생시킨다.
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
      String accessToken = (String) sessionAttributes.get(TOKEN);
      String serializedMessageDTO = new String((byte[]) message.getPayload(),
          StandardCharsets.UTF_8);

      try {
        MessageDTO messageDTO = objectMapper.readValue(serializedMessageDTO, MessageDTO.class);
        MemberInfoDTO memberInfoFromToken = tokenProvider.getMemberInfoFromToken(
            accessToken).get();
        sendNotification(messageDTO, alarmToken, memberInfoFromToken.getNickname());
      } catch (JsonProcessingException e) {
        throw new MessageDeliveryException(DESERIALIZATION_EXCEPTION);
      } catch (NoSuchElementException e) {
        throw new MessageDeliveryException(INVALID_ACCESS_TOKEN);
      }
    }
  }

  /**
   * 메시지를 전송해도 되는지 검증하는 필터의 역할을 수행한다. <br/>
   * {@link org.springframework.messaging.simp.stomp.StompCommand}의 SEND, CONNECT, SUBSCRIBE 명령에 대하여
   * 검증을 수행한다. <br/> 검증이 수행되는 경우, 다음과 같은 항목을 검증한다.
   * <li>STOMP 헤더에 토큰이 정상적으로 존재하는지</li>
   * <li>STOMP 헤더에 존재하는 토큰이 세션에 저장된 토큰과 일치하는지</li>
   * <li>토큰의 유효기간, 형식 등이 유효한지</li>
   *
   * @param message
   * @return boolean
   */
  private boolean verify(Message<?> message) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    if (!StompCommand.CONNECT.equals(accessor.getCommand()) && !StompCommand.SUBSCRIBE.equals(
        accessor.getCommand()) && !StompCommand.SEND.equals(accessor.getCommand())) {
      return true;
    }

    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
    String accessToken = (String) sessionAttributes.get(TOKEN);
    List<String> headers = accessor.getNativeHeader(AUTHORIZATION_HEADER);

    if (headers == null || headers.size() == 0) {
      throw new MessageDeliveryException(NOT_EXISTING_STOMP_AUTHORIZATION_HEADER);
    }

    String authorizationHeader = accessor.getNativeHeader(AUTHORIZATION_HEADER).get(0);
    String decrypt = aes256Util.decrypt(authorizationHeader)
        .orElseThrow(() -> new MessageDeliveryException(INVALID_STOMP_AUTHORIZATION_HEADER));

    if (!decrypt.equals(accessToken)) {
      throw new MessageDeliveryException(NOT_MATCHING_TOKEN);
    }

    Optional<MemberInfoDTO> memberInfoFromToken = tokenProvider.getMemberInfoFromToken(
        accessToken);
    return memberInfoFromToken.isPresent();

  }

  private void sendNotification(MessageDTO messageDTO, String token, String nickname) {
    StringBuffer alarmMessage = new StringBuffer(nickname).append(" : ")
        .append(messageDTO.getMessage());
    fcmNotificationService.sendNotification(
        FcmNotificationRequest.of("CHATTING", alarmMessage.toString()), token);
  }
}
