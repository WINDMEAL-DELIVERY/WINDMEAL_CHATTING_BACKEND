package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.domain.MessageDocument;
import java.util.Optional;
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
    private int uncheckedMessageCount;

    private ChatroomSpecResponse(String chatroomId, String lastMessage, int uncheckedMessageCount) {
      this.chatroomId = chatroomId;
      this.lastMessage = lastMessage;
      this.uncheckedMessageCount = uncheckedMessageCount;
    }

//    public static ChatroomSpecResponse of(String chatroomId,
//        Optional<MessageDocument> lastMessageOptional, int uncheckedMessageCount) {
//      String lastMessage =
//          lastMessageOptional.isPresent() ? lastMessageOptional.get().getMessage() : "";
//      return new ChatroomSpecResponse(chatroomId, lastMessage, uncheckedMessageCount);
//    }

    public static ChatroomSpecResponse of(String chatroomId,
        MessageDocument messageDocument, int uncheckedMessageCount) {
      return new ChatroomSpecResponse(chatroomId,
          messageDocument != null ? messageDocument.getMessage() : "", uncheckedMessageCount);
    }
  }
}
