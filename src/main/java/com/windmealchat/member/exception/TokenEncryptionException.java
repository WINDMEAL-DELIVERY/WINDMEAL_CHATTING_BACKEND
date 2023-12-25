package com.windmealchat.member.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class TokenEncryptionException extends GeneralException {

  public TokenEncryptionException(ErrorCode errorCode) {
    super(errorCode, "암호화 과정에서 오류가 발생하였습니다.");
  }
}
