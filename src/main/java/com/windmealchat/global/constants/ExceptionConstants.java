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

  // 엑세스 토큰이 유효하지 않은 경우
  public static final String INVALID_ACCESS_TOKEN = "INVALID_ACCESS_TOKEN";

  // 알람 토큰이 유효하지 않은 경우
  public static final String INVALID_ALARM_TOKEN = "INVALID_ALARM_TOKEN";

  // 암호화 관련 서버 내부의 문제가 발생한 경우
  public static final String INTERNAL_ENCRYPTION_EXCEPTION = "INTERNAL_ENCRYPTION_EXCEPTION";

  // 직렬화 과정에서 문제가 발생한 경우
  public static final String SERIALIZATION_EXCEPTION = "SERIALIZATION_EXCEPTION";

  // 역질렬화 과정에서 문제가 발생한 경우
  public static final String DESERIALIZATION_EXCEPTION = "DESERIALIZATION_EXCEPTION";

  // 기타 서버 내부의 문제가 발생한 경우
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
