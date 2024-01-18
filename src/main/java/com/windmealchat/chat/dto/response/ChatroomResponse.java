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
  public static class ChatroomSpecResponse implements Comparable<ChatroomSpecResponse>{

    private String chatroomId;
    private String lastMessage;
    private MessageType messageType;
    private LocalDateTime lastMessageTime;
    private int uncheckedMessageCount;
    private String oppositeAlarmToken;

    private ChatroomSpecResponse(String chatroomId, String lastMessage, MessageType messageType,
        LocalDateTime lastMessageTime, int uncheckedMessageCount, String oppositeAlarmToken) {
      this.chatroomId = chatroomId;
      this.lastMessage = lastMessage;
      this.messageType = messageType;
      this.lastMessageTime = lastMessageTime;
      this.oppositeAlarmToken = oppositeAlarmToken;
      this.uncheckedMessageCount = uncheckedMessageCount;
    }

    public static ChatroomSpecResponse of(ChatroomDocument chatroomDocument,
        MessageDocument messageDocument, int uncheckedMessageCount, String oppositeAlarmToken) {
      String lastMessage = messageDocument != null ? messageDocument.getMessage() : "";
      // 마지막으로 전송된 메시지가 없어서 전송 시간을 표시할 수 없는 경우는 채팅방이 생성된 시간을 대신 반환한다.
      LocalDateTime createdTime = messageDocument != null ? messageDocument.getCreatedTime()
          : chatroomDocument.getCreatedTime();
      MessageType lastMessageType =
          messageDocument != null ? messageDocument.getMessageType() : MessageType.SYSTEM;
      return new ChatroomSpecResponse(chatroomDocument.getId(), lastMessage, lastMessageType,
          createdTime, uncheckedMessageCount, oppositeAlarmToken);
    }

    @Override
    public int compareTo(ChatroomSpecResponse o) {
      return o.lastMessageTime.compareTo(this.lastMessageTime);
    }
  }
}
