package com.windmealchat.chat.service;

import static com.windmealchat.global.constants.StompConstants.SUB_CHAT_ROOM;
import static com.windmealchat.global.constants.StompConstants.SUB_PREFIX;
import static com.windmealchat.global.constants.StompConstants.SYSTEM_GREETING;

import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.MessageResponse;
import com.windmealchat.chat.dto.response.SystemResponse;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.HeaderAccessorUtil;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  private final SimpMessageSendingOperations messageTemplate;
  private final MessageDocumentRepository messageDocumentRepository;

  public void enter(MessageDTO messageDTO) {
    SystemResponse systemResponse = SystemResponse.of(messageDTO, SYSTEM_GREETING);
    messageTemplate.convertAndSend(SUB_PREFIX + SUB_CHAT_ROOM + messageDTO.getChatRoomId(), systemResponse);
  }

  public void sendMessage(MessageDTO messageDTO, MemberInfoDTO memberInfoDTO) {
    MessageResponse messageResponse = MessageResponse.of(messageDTO, memberInfoDTO);
    MessageDocument messageDocument = messageDTO.toEntity(memberInfoDTO);
    messageDocumentRepository.save(messageDocument);
    messageTemplate.convertAndSend(SUB_PREFIX + SUB_CHAT_ROOM + messageDTO.getChatRoomId(), messageResponse);
  }

//  public void getMessagesByChatroomId(Long chatroomId) {
//    List<MessageDocument> messages = messageDocumentRepository.findByChatroomId(chatroomId);
//    for (MessageDocument message : messages) {
//      log.info(message.getMessageId() + " " + message.getMessage());
//    }
//  }
}
