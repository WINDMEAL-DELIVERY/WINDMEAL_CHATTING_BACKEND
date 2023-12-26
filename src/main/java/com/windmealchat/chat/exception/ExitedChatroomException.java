package com.windmealchat.chat.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class ExitedChatroomException extends GeneralException {

  public ExitedChatroomException(ErrorCode errorCode) {
    super(errorCode, "이미 사용자가 나간 채팅방입니다.");
  }
}
