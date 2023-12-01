package com.windmealchat.chat.domain;

import com.windmealchat.chat.dto.request.MessageDTO;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document(collection = "chat")
public class MessageDocument {

  @Id
  private String messageId;

  private Long chatroomId;

  private MessageType messageType;

  private String message;

  @CreatedDate
  private LocalDate createdTime;

  public MessageDocument(MessageDTO messageDTO) {
    this.chatroomId = Long.valueOf(messageDTO.getChatRoomId());
    this.messageType = messageDTO.getType();
    this.message = messageDTO.getMessage();
  }
}
