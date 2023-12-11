package com.windmealchat.global.token.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class InvalidAccessTokenException extends GeneralException {

  public InvalidAccessTokenException(ErrorCode errorCode) {
    super(errorCode, "유효하지 않은 엑세스 토큰입니다.");
  }
}
