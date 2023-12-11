package com.windmealchat.global.dto;

import com.windmealchat.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResultDataResponseDTO<T> extends ResponseDTO {

  private final T data;

  private ResultDataResponseDTO(T data) {
    super(true, ErrorCode.OK.getCode());
    this.data = data;
  }

  private ResultDataResponseDTO(T data, String message) {
    super(true, ErrorCode.OK.getCode(), message);
    this.data = data;
  }


  private ResultDataResponseDTO(T data, HttpStatus httpStatus) {
    super(true, httpStatus.value());
    this.data = data;
  }


  public static <T> ResultDataResponseDTO<T> of(T data) {
    return new ResultDataResponseDTO<>(data);
  }

  public static <T> ResultDataResponseDTO<T> of(T data, String message) {
    return new ResultDataResponseDTO<>(data, message);
  }

  public static <T> ResultDataResponseDTO<T> empty() {
    return new ResultDataResponseDTO<>(null);
  }
}
