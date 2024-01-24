package com.windmealchat.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
public enum ErrorCode {

    OK(200,HttpStatus.OK),
    CREATED(201, HttpStatus.CREATED),
    NO_CONTENT(204, HttpStatus.NO_CONTENT),

    BAD_REQUEST(400, HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(400, HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, HttpStatus.FORBIDDEN),
    NOT_FOUND(404, HttpStatus.NOT_FOUND),

    S3_TYPE_EXCEPTION(400,HttpStatus.BAD_REQUEST),
    ENCRYPT_ERROR(500, HttpStatus.BAD_REQUEST),
    S3_ERROR(503,HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR(600, HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_ACCESS_ERROR(605, HttpStatus.INTERNAL_SERVER_ERROR),


    INVALID_EMAIL_AND_PASSWORD_REQUEST(400,HttpStatus.BAD_REQUEST);



    private final Integer code;
    private final HttpStatus httpStatus;

    ErrorCode(Integer code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getMessage(Throwable e) {
        return e.getMessage();
        // 결과 예시 - "Validation error - Reason why it isn't valid"
    }

    public String getMessage(String message) {
        return Optional.ofNullable(message)
                .filter(Predicate.not(String::isBlank))
                .orElse(message);
    }

    public static ErrorCode valueOf(HttpStatus httpStatus,Exception ex) {
        if (httpStatus == null) {
            throw new GeneralException("HttpStatus is null.");
        }

        return Arrays.stream(values())
                .filter(errorCode -> errorCode.getHttpStatus() == httpStatus)
                .findFirst()
                .orElseGet(() -> {

                    if (httpStatus.is4xxClientError()) {
                        return ErrorCode.BAD_REQUEST;
                    } else if (httpStatus.is5xxServerError()) {
                        System.out.println("ex = " + ex);

                        return ErrorCode.INTERNAL_ERROR;

                    } else {
                        return ErrorCode.OK;
                    }
                });
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", this.name(), this.getCode());
    }
}
