package com.windmealchat.global.token.impl;

import com.windmealchat.member.dto.response.MemberInfoDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Optional;

import static com.windmealchat.global.constants.TokenConstants.EMAIL;

@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    @Value("${spring.jwt.secret}")
    private String secretKey;
    private Key key;


    // TODO 이 예외는 어떻게 처리해줄지 고민해보기
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 토큰을 넘기면 사용자의 정보를 알려준다.
     * @param token
     * @return MemberInfoDto (memberId, memberEmail)
     * @throws JwtException 파싱과정에서 토큰 오류가 발생하면 이를 호출한 클래스로 예외를 위임한다.
     */
    public Optional<MemberInfoDTO> getMemberInfoFromToken(String token) {
        Long userId = null;
        String email = null;
        if(validateToken(token)) {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            // 사용자 ID
            userId = Long.parseLong(claims.getSubject());
            // 사용자 이메일
            email = (String)claims.get(EMAIL);
        }
        return MemberInfoDTO.ofNullable(userId, email);
    }

    private boolean validateToken(String token) {
        try{
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
