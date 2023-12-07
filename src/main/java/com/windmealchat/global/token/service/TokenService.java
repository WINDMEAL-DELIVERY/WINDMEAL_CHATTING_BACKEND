package com.windmealchat.global.token.service;

import static com.windmealchat.global.constants.TokenConstants.ALARM_TOKEN;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

  private final TokenProvider tokenProvider;

  public String resolveAlarmToken(SimpMessageHeaderAccessor accessor) {
    Map<String, Object> session = accessor.getSessionAttributes();
    return (String) session.get(ALARM_TOKEN);
  }

  public Optional<MemberInfoDTO> resolveJwtToken(SimpMessageHeaderAccessor accessor) {
    Map<String, Object> session = accessor.getSessionAttributes();
    String accessToken = (String) session.get(TOKEN);
    Optional<MemberInfoDTO> result;
    try {
      result = tokenProvider.getMemberInfoFromToken(
          accessToken);
    } catch (Exception e) {
      e.printStackTrace();
      result = Optional.empty();
    }
    return result;
  }

  //  public <T> Optional<T> resolveToken(SimpMessageHeaderAccessor accessor, String tokenType,
//      TokenResolver<T> resolver) {
//    Map<String, Object> session = accessor.getSessionAttributes();
//    String accessToken = (String) session.get(tokenType);
//    Optional<T> result = Optional.empty();
//    try {
//      if(tokenType.equals(TOKEN)) {
//        result = tokenProvider.getMemberInfoFromToken(accessToken);
//      }
//      else {
//        result = (Optional<T>) accessToken;
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//    return result;
//  }

}
