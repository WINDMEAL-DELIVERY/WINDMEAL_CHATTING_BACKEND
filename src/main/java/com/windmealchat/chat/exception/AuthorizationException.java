package com.windmealchat.chat.exception;

import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;

public class AuthorizationException extends GeneralException {
    public AuthorizationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
