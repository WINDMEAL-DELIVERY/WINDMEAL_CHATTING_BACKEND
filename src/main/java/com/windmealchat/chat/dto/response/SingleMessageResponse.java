package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SingleMessageResponse {
  @Schema(description = "채팅방 ID", example = "65ba11b9a1b2a0085d9b0cac")
  private String chatroomId;
  @Schema(description = "채팅 메시지", example = "예정보다 조금 일찍 도착할 것 같습니다!")
  private String message;
  @Schema(description = "메시지 유형", example = "MESSAGE")
  private MessageType messageType;
  @Schema(description = "메시지 전송일", example = "2023-12-10T14:23:05.023")
  private LocalDateTime createdTime;

  public SingleMessageResponse(MessageDocument messageDocument) {
    this.chatroomId = messageDocument.getChatroomId();
    this.message = messageDocument.getMessage();
    this.messageType = messageDocument.getMessageType();
    this.createdTime = messageDocument.getCreatedTime();
  }
}
