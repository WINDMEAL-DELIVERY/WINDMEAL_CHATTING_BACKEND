package com.windmealchat.chat.domain;

import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDocument {

  @Id
  private String messageId;

  private String chatroomId;

  private MessageType messageType;

  private String message;

  private Long senderId;
  private String senderEmail;

  @CreatedDate
  private LocalDateTime createdTime;

  public MessageDocument(String decryptedId, MessageDTO messageDTO, MemberInfoDTO memberInfoDTO) {
    this.chatroomId = decryptedId;
    this.messageType = messageDTO.getType();
    this.message = messageDTO.getMessage();
    this.senderId = memberInfoDTO.getId();
    this.senderEmail = memberInfoDTO.getEmail();
  }

  @Builder
  public MessageDocument(String messageId, String chatroomId, MessageType messageType, String message, Long senderId,
      String senderEmail) {
    this.messageId = messageId;
    this.chatroomId = chatroomId;
    this.messageType = messageType;
    this.message = message;
    this.senderId = senderId;
    this.senderEmail = senderEmail;
  }

}
