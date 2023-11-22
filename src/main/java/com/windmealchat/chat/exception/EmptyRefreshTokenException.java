package com.windmealchat.chat.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class EmptyRefreshTokenException extends GeneralException {

    public EmptyRefreshTokenException() {
        super("리프레쉬 토큰이 존재하지 않습니다.");
    }
    public EmptyRefreshTokenException(String message) {
        super(message);
    }

    public EmptyRefreshTokenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
