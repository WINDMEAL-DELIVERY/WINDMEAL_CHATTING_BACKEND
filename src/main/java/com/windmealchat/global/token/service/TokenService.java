package com.windmealchat.global.token.service;

import static com.windmealchat.global.constants.TokenConstants.ALARM_TOKEN;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.token.exception.InvalidAccessTokenException;
import com.windmealchat.global.token.exception.TokenNotExistsException;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import com.windmealchat.member.exception.TokenEncryptionException;
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
  private final AES256Util aes256Util;

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

  public MemberInfoDTO resolveJwtTokenFromHeader(Optional<String> tokenOptional) {
    String token = tokenOptional.orElseThrow(
        () -> new TokenNotExistsException(ErrorCode.UNAUTHORIZED));
    Optional<String> decryptedTokenOptional = aes256Util.decrypt(token);
    String decryptedToken = decryptedTokenOptional.orElseThrow(
        () -> new TokenEncryptionException(ErrorCode.ENCRYPT_ERROR));
    return tokenProvider.getMemberInfoFromToken(decryptedToken)
        .orElseThrow(() -> new InvalidAccessTokenException(ErrorCode.UNAUTHORIZED));
  }

}
