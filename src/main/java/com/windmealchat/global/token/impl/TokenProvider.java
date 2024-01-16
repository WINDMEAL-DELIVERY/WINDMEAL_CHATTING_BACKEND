package com.windmealchat.global.token.impl;

import static com.windmealchat.global.constants.TokenConstants.EMAIL;
import static com.windmealchat.global.constants.TokenConstants.NICKNAME_KEY;

import com.windmealchat.global.exception.GeneralException;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.security.Key;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenProvider implements InitializingBean {

  @Value("${spring.jwt.secret}")
  private String secretKey;
  private Key key;


  @Override
  public void afterPropertiesSet() throws Exception {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * 토큰을 넘기면 사용자의 정보를 알려준다.
   *
   * @param token
   * @return MemberInfoDto (memberId, memberEmail)
   * @throws JwtException 파싱과정에서 토큰 오류가 발생하면 이를 호출한 클래스로 예외를 위임한다.
   */
  public Optional<MemberInfoDTO> getMemberInfoFromToken(String token)
      throws SecurityException, MalformedJwtException, ExpiredJwtException {
    Long userId = null;
    String email = null;
    String nickname = null;

    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        .getBody();
    // 사용자 ID
    userId = Long.parseLong(claims.getSubject());
    // 사용자 이메일
    email = (String) claims.get(EMAIL);
    // 사용자 닉네임
    nickname = (String) claims.get(NICKNAME_KEY);
    return MemberInfoDTO.ofNullable(userId, email, nickname);
  }

  private boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("잘못된 JWT 서명입니다.");
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다.");
    } catch (UnsupportedJwtException e) {
      log.info("지원되지 않는 JWT 토큰입니다.");
    } catch (IllegalArgumentException e) {
      log.info("JWT 토큰이 잘못되었습니다.");
    }
    return false;
  }

}
