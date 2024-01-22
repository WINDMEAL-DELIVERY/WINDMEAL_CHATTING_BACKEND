package com.windmealchat.global.exception;

public class AesException extends GeneralException {
  public AesException(ErrorCode errorCode) {
    super(errorCode, "암호화 과정에서 에러가 발생하였습니다.");
  }
}
