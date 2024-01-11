package com.windmealchat.chat.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class CanNotDeleteQueueException extends GeneralException {

  public CanNotDeleteQueueException(ErrorCode errorCode) {
    super(errorCode, "큐를 삭제하지 못했습니다.");
  }
}
