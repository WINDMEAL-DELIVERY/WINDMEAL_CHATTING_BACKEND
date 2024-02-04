package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "채팅 메시지 페이지네이션 응답")
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
  @Schema(title = "채팅 메시지 세부 정보")
  public static class ChatMessageSpecResponse {
    @Schema(description = "채팅 메시지 ID", example = "65ba11b9a1b2a0085d9b0cac")
    private String messageId;
    @Schema(description = "채팅 메시지", example = "예정보다 조금 일찍 도착할 것 같습니다!")
    private String message;
    @Schema(description = "메시지 유형", example = "MESSAGE")
    private MessageType messageType;
    @Schema(description = "메시지 전송자 ID", example = "3")
    private Long senderId;
    @Schema(description = "메시지 전송일", example = "2023-12-10T14:23:05.023")
    private LocalDateTime sendTime;
    @Schema(description = "자신이 보낸 메시지 여부", example = "false")
    private boolean isFromMe;

    public static ChatMessageSpecResponse of(MessageDocument messageDocument, boolean isFromMe) {
      return ChatMessageSpecResponse.builder()
          .messageType(messageDocument.getMessageType())
          .sendTime(messageDocument.getCreatedTime())
          .messageId(messageDocument.getMessageId())
          .senderId(messageDocument.getSenderId())
          .message(messageDocument.getMessage())
          .isFromMe(isFromMe)
          .build();
    }

  }
}
