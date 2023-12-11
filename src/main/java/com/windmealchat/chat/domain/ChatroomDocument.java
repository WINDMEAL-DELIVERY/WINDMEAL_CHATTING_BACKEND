package com.windmealchat.chat.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chatroom")
public class ChatroomDocument {

  @Id
  private String id;

  private Long ownerId;

  private Long guestId;

  private Long orderId;

  private boolean isDeletedByOwner = false;

  private boolean isDeletedByGuest = false;

  @CreatedDate
  private LocalDateTime createdTime;

  // TODO  이게 되나??
  @Builder
  public ChatroomDocument(Long ownerId, Long guestId, Long orderId) {
    this.ownerId = ownerId;
    this.guestId = guestId;
    this.orderId = orderId;
    this.isDeletedByOwner = false;
    this.isDeletedByGuest = false;
  }


}
