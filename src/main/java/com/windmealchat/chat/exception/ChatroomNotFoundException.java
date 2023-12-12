package com.windmealchat.chat.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class ChatroomNotFoundException extends GeneralException {

  public ChatroomNotFoundException(ErrorCode errorCode) {
    super(errorCode, "채팅방을 찾을 수 없습니다.");
  }
}
