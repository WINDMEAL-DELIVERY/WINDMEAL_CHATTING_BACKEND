package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
public class ChatroomResponse {

  private final Slice<ChatroomSpecResponse> chatroomSpecResponses;

  private ChatroomResponse(Slice<ChatroomSpecResponse> chatroomSpecResponses) {
    this.chatroomSpecResponses = chatroomSpecResponses;
  }

  public static ChatroomResponse of(Slice<ChatroomSpecResponse> chatroomSpecResponses) {
    return new ChatroomResponse(chatroomSpecResponses);
  }

  @Getter
  public static class ChatroomSpecResponse {

    private String chatroomId;
    private String lastMessage;
    private MessageType messageType;
    private LocalDateTime createdDate;
    private int uncheckedMessageCount;

    private ChatroomSpecResponse(String chatroomId, String lastMessage, MessageType messageType,
        LocalDateTime createdDate,
        int uncheckedMessageCount) {
      this.chatroomId = chatroomId;
      this.lastMessage = lastMessage;
      this.messageType = messageType;
      this.createdDate = createdDate;
      this.uncheckedMessageCount = uncheckedMessageCount;
    }

    public static ChatroomSpecResponse of(ChatroomDocument chatroomDocument,
        MessageDocument messageDocument, int uncheckedMessageCount) {
      String lastMessage = messageDocument != null ? messageDocument.getMessage() : "";
      // 마지막으로 전송된 메시지가 없어서 전송 시간을 표시할 수 없는 경우는 채팅방이 생성된 시간을 대신 반환한다.
      // 디펜시브다. 채팅방 입장과 동시에 시스템 메시지가 전송되기 때문에 메시지가 비어있을 일은 없다.
      LocalDateTime createdTime = messageDocument != null ? messageDocument.getCreatedTime()
          : chatroomDocument.getCreatedTime();
      MessageType lastMessageType =
          messageDocument != null ? messageDocument.getMessageType() : MessageType.SYSTEM;
      return new ChatroomSpecResponse(chatroomDocument.getId(), lastMessage, lastMessageType,
          createdTime,
          uncheckedMessageCount);
    }
  }
}
