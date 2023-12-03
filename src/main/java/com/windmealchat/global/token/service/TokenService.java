package com.windmealchat.global.token.service;

import static com.windmealchat.global.constants.TokenConstants.ALARM_TOKEN;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.global.util.HeaderAccessorUtil;
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

  private final AES256Util aes256Util;
  private final TokenProvider tokenProvider;

  public Optional<String> resolveAlarmToken(SimpMessageHeaderAccessor accessor) {
    Map<String, Object> session = accessor.getSessionAttributes();
    String alarmToken = (String) session.get(ALARM_TOKEN);
    String result;
    try {
      result = aes256Util.decrypt(alarmToken);
    } catch (Exception e) {
      result = null;
    }
    return Optional.ofNullable(result);
  }

  public Optional<MemberInfoDTO> resolveJwtToken(SimpMessageHeaderAccessor accessor) {
    Map<String, Object> session = accessor.getSessionAttributes();
    String encryptedToken = (String) session.get(TOKEN);
    Optional<MemberInfoDTO> result;
    try {
      String accessToken = aes256Util.decrypt(encryptedToken);
      result = tokenProvider.getMemberInfoFromToken(
          accessToken);
    } catch (Exception e) {
      result = Optional.ofNullable(null);
    }
    return result;
  }

}
