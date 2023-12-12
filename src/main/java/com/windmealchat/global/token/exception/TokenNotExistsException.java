package com.windmealchat.global.token.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class TokenNotExistsException extends GeneralException {

  public TokenNotExistsException(ErrorCode errorCode) {
    super(errorCode, "요청에 인증 정보가 존재하지 않습니다.");
  }
}
