package com.windmealchat.alarm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "알람 요청")
public class FcmNotificationRequest {

  @Schema(description = "알람 제목", example = "채팅")
  private String title;
  @Schema(description = "알람 내용", example = "어디쯤 오셨나요?")
  private String body;

  public static FcmNotificationRequest of(String title, String body) {
    return FcmNotificationRequest.builder()
        .title(title)
        .body(body)
        .build();
  }
}
