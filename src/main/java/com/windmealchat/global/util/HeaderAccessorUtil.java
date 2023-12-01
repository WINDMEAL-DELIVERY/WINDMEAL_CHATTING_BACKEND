package com.windmealchat.global.util;

import static com.windmealchat.global.constants.TokenConstants.TOKEN;

import com.windmealchat.chat.exception.AuthorizationException;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Map;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

public class HeaderAccessorUtil {

  public static MemberInfoDTO convertToMemberInfo(SimpMessageHeaderAccessor accessor, TokenProvider tokenProvider) {
    Map<String, Object> session = accessor.getSessionAttributes();
    String accessToken = (String) session.get(TOKEN);
    return tokenProvider.getMemberInfoFromToken(accessToken).orElseThrow(
        () -> new AuthorizationException(ErrorCode.UNAUTHORIZED, "사용자의 인증 정보가 유효하지 않습니다."));
  }

}
