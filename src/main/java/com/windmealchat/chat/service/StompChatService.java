package com.windmealchat.chat.service;

import static com.windmealchat.global.constants.RabbitConstants.CHAT_AMQ_TOPIC;
import static com.windmealchat.global.constants.RabbitConstants.CHAT_EXCHANGE_NAME;
import static com.windmealchat.global.constants.StompConstants.SYSTEM_GREETING;

import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StompChatService {

  private final MessageDocumentRepository messageDocumentRepository;
//  private final SimpMessageSendingOperations messageTemplate;
  private final RabbitTemplate rabbitTemplate;

  public void enter(String chatroomId, MessageDTO messageDTO) {
    MessageDocument systemMessageDocument = messageDTO.toDocument(SYSTEM_GREETING);
    ChatMessageSpecResponse systemMessageSpecResponse = ChatMessageSpecResponse.of(systemMessageDocument);
    messageDocumentRepository.save(systemMessageDocument);
//    messageTemplate.convertAndSend(SUB_PREFIX + SUB_CHAT_ROOM + messageDTO.getChatRoomId(), systemMessageSpecResponse);
//    rabbitTemplate.convertAndSend(CHAT_AMQ_TOPIC, "room." + chatroomId, systemMessageSpecResponse);
    rabbitTemplate.convertAndSend( "room." + chatroomId + ".*", systemMessageSpecResponse);
  }

  public void sendMessage(String chatroomId, MessageDTO messageDTO, MemberInfoDTO memberInfoDTO) {
    MessageDocument messageDocument = messageDTO.toDocument(memberInfoDTO);
    ChatMessageSpecResponse chatMessageSpecResponse = ChatMessageSpecResponse.of(messageDocument);
    messageDocumentRepository.save(messageDocument);
//    messageTemplate.convertAndSend(SUB_PREFIX + SUB_CHAT_ROOM + messageDTO.getChatRoomId(), chatMessageSpecResponse);
//    rabbitTemplate.convertAndSend(CHAT_AMQ_TOPIC, "room." + chatroomId, chatMessageSpecResponse);
    rabbitTemplate.convertAndSend( "room." + chatroomId + ".*", chatMessageSpecResponse);
  }


}
