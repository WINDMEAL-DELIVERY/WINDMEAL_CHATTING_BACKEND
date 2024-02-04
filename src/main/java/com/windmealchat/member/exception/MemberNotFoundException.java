package com.windmealchat.member.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class MemberNotFoundException extends GeneralException {

  public MemberNotFoundException() {
    super(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.");
  }

  public MemberNotFoundException(ErrorCode errorCode) {
    super(errorCode, "사용자를 찾을 수 없습니다.");
  }
}
