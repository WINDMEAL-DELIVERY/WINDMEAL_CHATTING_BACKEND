package com.windmealchat.chat.repository;

import com.windmealchat.chat.domain.ChatroomDocument;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ChatroomDocumentRepository extends MongoRepository<ChatroomDocument, String> {

//  Slice<ChatroomDocument> findByOwnerIdOrGuestId(Long ownerId, Long guestId, Pageable pageable);

  @Query("{ $or: [ { 'guestId': ?0, 'isDeletedByGuest': false }, { 'ownerId': ?1, 'isDeletedByOwner': false } ] }")
  Slice<ChatroomDocument> findActiveChatrooms(Long guestId, Long ownerId, Pageable pageable);


  Optional<ChatroomDocument> findById(String id);
}
