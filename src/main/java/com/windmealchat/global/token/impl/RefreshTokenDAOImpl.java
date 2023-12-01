package com.windmealchat.global.token.impl;

import com.windmealchat.global.token.dao.RefreshTokenDAO;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.windmealchat.global.constants.TokenConstants.PREFIX_REFRESHTOKEN;

@RequiredArgsConstructor
public class RefreshTokenDAOImpl implements RefreshTokenDAO {

  private final RedisTemplate redisTemplate;

  @Override
  public Optional<String> getRefreshToken(String key) {
    // opsForValue 연산은 키에 해당되는 value가 없거나 트랜잭션, 파이프라인 진행 중일 경우 null을 반환한다.
    // 따라서 optional로 감싼 상태로 반환하고, 이를 호출하는 부분(HttpHandShakeInterceptor)에서 검증하겠다.
    String refreshToken = (String) redisTemplate.opsForValue()
        .get(key);
    return Optional.ofNullable(refreshToken);
  }
}
