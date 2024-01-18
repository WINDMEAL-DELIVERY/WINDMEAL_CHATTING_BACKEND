package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "채팅 메시지")
public class ChatMessageResponse {

  private Slice<ChatMessageSpecResponse> chatMessageSpecResponses;

  public static ChatMessageResponse of(Slice<ChatMessageSpecResponse> chatMessageSpecResponses) {
    return ChatMessageResponse.builder()
        .chatMessageSpecResponses(chatMessageSpecResponses)
        .build();
  }


  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChatMessageSpecResponse {
    @Schema(description = "채팅 메시지", example = "예정보다 조금 일찍 도착할 것 같습니다!")
    private String message;
    @Schema(description = "메시지 유형", example = "MESSAGE")
    private MessageType messageType;
    @Schema(description = "메시지 전송자 ID", example = "3")
    private Long senderId;
    @Schema(description = "메시지 전송일", example = "2023-12-10T14:23:05.023")
    private LocalDateTime createdTime;

    public static ChatMessageSpecResponse of(MessageDocument messageDocument) {
      return ChatMessageSpecResponse.builder()
          .messageType(messageDocument.getMessageType())
          .createdTime(messageDocument.getCreatedTime())
          .senderId(messageDocument.getSenderId())
          .message(messageDocument.getMessage())
          .build();
    }

  }
}
