package com.windmealchat.chat.repository;

import com.windmealchat.chat.domain.ChatroomDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatroomDocumentRepository extends MongoRepository<ChatroomDocument, String> {

  Slice<ChatroomDocument> findByOwnerIdOrGuestId(Long memberId, Pageable pageable);
}
