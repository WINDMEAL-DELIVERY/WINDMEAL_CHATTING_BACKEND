package com.windmealchat.chat.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chatroom")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatroomDocument {

  @Id
  private String id;

  private Long ownerId;

  private Long guestId;

  private Long orderId;

  private String ownerEmail;

  private String guestEmail;

  private boolean isDeletedByOwner = false;

  private boolean isDeletedByGuest = false;

  @CreatedDate
  private LocalDateTime createdTime;

  @Builder
  public ChatroomDocument(String id, Long ownerId, Long guestId, Long orderId, String ownerEmail,
      String guestEmail) {
    this.id = id;
    this.ownerId = ownerId;
    this.guestId = guestId;
    this.orderId = orderId;
    this.ownerEmail = ownerEmail;
    this.guestEmail = guestEmail;
    this.isDeletedByOwner = false;
    this.isDeletedByGuest = false;
  }

  public void updateIsDeletedByOwner() {
    this.isDeletedByOwner = true;
  }

  public void updateIsDeletedByGuest() {
    this.isDeletedByGuest = true;
  }

}
