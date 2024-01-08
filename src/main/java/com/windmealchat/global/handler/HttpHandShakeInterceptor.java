package com.windmealchat.global.handler;

import static com.windmealchat.global.constants.TokenConstants.ALARM_TOKEN;
import static com.windmealchat.global.constants.TokenConstants.CODE;
import static com.windmealchat.global.constants.TokenConstants.CODE_A;
import static com.windmealchat.global.constants.TokenConstants.PREFIX_REFRESHTOKEN;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

import com.windmealchat.global.token.dao.RefreshTokenDAO;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@RequiredArgsConstructor
public class HttpHandShakeInterceptor implements HandshakeInterceptor {

  private final AES256Util aes256Util;
  private final TokenProvider tokenProvider;
  private final RefreshTokenDAO refreshTokenDAO;

  /*
      ServletHttpRequest에서 쿠키를 가져온 다음, attributes에 저장해준다.
      그리고 해당 값을 인바운드 채널 인터셉터에서 시큐리티의 필터처럼 검증해주면 된다.
   */
  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
    if (request instanceof ServletServerHttpRequest) {
      ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
      HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
      try {
        String token = resolveToken(servletRequest, CODE).get();
        String alarmToken = resolveToken(servletRequest, CODE_A).get();
        Optional<MemberInfoDTO> memberInfoDTO = resolveMemberInfo(token);
        if (memberInfoDTO.isPresent()) {
          String key =
              PREFIX_REFRESHTOKEN + memberInfoDTO.get().getId() + memberInfoDTO.get().getEmail();
          Optional<String> refreshToken = refreshTokenDAO.getRefreshToken(aes256Util.encrypt(key));
          if (refreshToken.isPresent()) {
            attributes.put(TOKEN, token);
            attributes.put(ALARM_TOKEN, alarmToken);
            return true;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Exception exception) {
    // 핸드쉐이크 이후의 콜백 메서드. 일단은 처리할 것이 없다고 판단되어 비워두겠다.
    log.info("Handshake 완료");
  }

  private Optional<String> resolveToken(HttpServletRequest servletRequest, String type) {
    String queryString = servletRequest.getParameter(type);
    return aes256Util.decrypt(queryString);
  }

  private Optional<MemberInfoDTO> resolveMemberInfo(String token) {
    Optional<MemberInfoDTO> memberInfoDTO = Optional.empty();
    if (token != null) {
      memberInfoDTO = tokenProvider.getMemberInfoFromToken(token);
    }
    return memberInfoDTO;
  }
}
