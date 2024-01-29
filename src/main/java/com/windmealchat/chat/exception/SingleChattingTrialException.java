package com.windmealchat.chat.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class SingleChattingTrialException extends GeneralException {

  public SingleChattingTrialException(ErrorCode errorCode) {
    super(errorCode, "혼자 있는 채팅방에서는 채팅을 보낼 수 없습니다.");
  }
}
