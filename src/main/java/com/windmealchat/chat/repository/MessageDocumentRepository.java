package com.windmealchat.chat.repository;

import com.windmealchat.chat.domain.MessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface MessageDocumentRepository extends MongoRepository<MessageDocument, String> {

  Slice<MessageDocument> findByChatroomId(String chatroomId, Pageable pageable);

  MessageDocument findTopByChatroomIdOrderByCreatedTimeDesc(String chatroomId);

}
