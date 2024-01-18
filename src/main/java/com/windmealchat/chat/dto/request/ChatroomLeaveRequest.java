package com.windmealchat.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "채팅방 나가기 요청")
public class ChatroomLeaveRequest {

  @Schema(description = "채팅방 아이디", example = "65a8c06b58fdc8725d6b10b1")
  private String chatroomId;
}
