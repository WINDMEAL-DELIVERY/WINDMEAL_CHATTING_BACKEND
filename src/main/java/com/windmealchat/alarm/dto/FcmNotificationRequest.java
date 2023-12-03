package com.windmealchat.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmNotificationRequest {
  private String title;
  private String body;

  public static FcmNotificationRequest of(String title, String body) {
    return FcmNotificationRequest.builder()
        .title(title)
        .body(body)
        .build();
  }
}
