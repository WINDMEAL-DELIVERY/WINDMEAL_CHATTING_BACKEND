package com.windmealchat.global.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windmealchat.global.auth.SimpleUserPrincipal;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.windmealchat.global.constants.TokenConstants.AUTHORIZATION_HEADER;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientInboundChannelHandler implements ChannelInterceptor {

  private final TokenProvider tokenProvider;
  private final ObjectMapper objectMapper;

  /*
   * 웹소켓 연결을 맺은 클라이언트가 메세지를 보내기 전에, 권한이 있는지 체크하는 과정이다.
   * 아래 코드를 보면 accessor.getSessionAttributes(); 부분이 나오는데, 이 부분은 Map 형식의 세션으로부터 토큰을 얻어오는 부분이다.
   * 그리고 이 토큰은 HttpHandshakeInterceptor에서 핸드쉐이크 과정 중간에 가져온 쿠키의 토큰값이다.
   * 공식문서에 따르면, 해당 메서드가 null을 반환할 시 실제 메세지 전송은 일어나지 않는다고 한다.
   * 따라서 인증 정보가 유효하지 않다면 null을 반환하도록 하자.
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    boolean isValid = false;
    try {
      isValid = TokenProcessing(message);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return isValid ? message : null;
  }

  private boolean TokenProcessing(Message<?> message) throws JsonProcessingException {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
        StompHeaderAccessor.class);
    log.error("커맨드 타입 : " + accessor.getCommand());

    if (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(
        accessor.getCommand())) {
      // 이 세션은 HandshakeInterceptor 혹은 이를 상속받은 클래스에서 핸드쉐이크 과정 중간에 난입하여 얻은 토큰 정보 등을 저장해둔 곳이다.
      Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
      String accessToken = (String) sessionAttributes.get(TOKEN);
      // CONNECT 명령의 경우
      Optional<MemberInfoDTO> memberInfoFromToken = tokenProvider.getMemberInfoFromToken(
          accessToken);
      if (memberInfoFromToken.isPresent()) {
        // TODO disconnect 시점에 큐를 삭제하는 로직도 추가해야 함
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          MemberInfoDTO memberInfoDTO = memberInfoFromToken.get();
          accessor.setUser(new SimpleUserPrincipal(memberInfoDTO.getId(),
              memberInfoDTO.getEmail(), memberInfoDTO.getNickname()));
        }
        return true;
      }
      return false;
    }
    // 다른 명령어라면 통과
    return true;
  }
}
