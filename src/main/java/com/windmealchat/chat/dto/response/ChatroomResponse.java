package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
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
    private int uncheckedMessageCount;

    private ChatroomSpecResponse(String chatroomId, String lastMessage, MessageType messageType,
        int uncheckedMessageCount) {
      this.chatroomId = chatroomId;
      this.lastMessage = lastMessage;
      this.messageType = messageType;
      this.uncheckedMessageCount = uncheckedMessageCount;
    }

    public static ChatroomSpecResponse of(String chatroomId,
        MessageDocument messageDocument, int uncheckedMessageCount) {
      String lastMessage = messageDocument != null ? messageDocument.getMessage() : "";
      MessageType lastMessageType =
          messageDocument != null ? messageDocument.getMessageType() : MessageType.SYSTEM;
      return new ChatroomSpecResponse(chatroomId, lastMessage, lastMessageType,
          uncheckedMessageCount);
    }
  }
}
