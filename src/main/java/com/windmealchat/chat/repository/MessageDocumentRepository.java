package com.windmealchat.chat.repository;

import com.windmealchat.chat.domain.MessageDocument;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


public interface MessageDocumentRepository extends MongoRepository<MessageDocument, String> {

  Slice<MessageDocument> findByChatroomId(String chatroomId, Pageable pageable);
}
