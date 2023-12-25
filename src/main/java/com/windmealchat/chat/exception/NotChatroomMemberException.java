package com.windmealchat.chat.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class NotChatroomMemberException extends GeneralException {

  public NotChatroomMemberException(ErrorCode errorCode) {
    super(errorCode, "사용자는 해당 채팅방의 멤버가 아닙니다.");
  }
}
