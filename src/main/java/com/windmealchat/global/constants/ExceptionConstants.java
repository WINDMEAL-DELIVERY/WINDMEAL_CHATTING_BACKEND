package com.windmealchat.global.constants;

public class ExceptionConstants {

  // jwt가 만료되었을때의 메시지
  public static final String EXPIRED_JWT = "EXPIRED_JWT";

  // STOMP 인증 헤더에 값이 없을 경우
  public static final String NOT_EXISTING_STOMP_AUTHORIZATION_HEADER = "NOT_EXISTING_STOMP_AUTHORIZATION_HEADER";

  // STOMP 인증 헤더의 토큰 값이 유효하지 않을 경우
  public static final String INVALID_STOMP_AUTHORIZATION_HEADER = "INVALID_STOMP_AUTHORIZATION_HEADER";

  // STOMP 인증 헤더와 쿼리 파라미터로 전달받은 토큰의 값이 다를 경우
  public static final String NOT_MATCHING_TOKEN = "NOT_MATCHING_TOKEN";

  public static final String INTERNAL_ENCRYPTION_EXCEPTION = "INTERNAL_ENCRYPTION_EXCEPTION";

  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
