package com.windmealchat.chat.service;

import static com.windmealchat.global.constants.StompConstants.SYSTEM_GREETING;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.exception.ExitedChatroomException;
import com.windmealchat.chat.exception.NotChatroomMemberException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StompChatService {

  private final ChatroomDocumentRepository chatroomDocumentRepository;
  private final MessageDocumentRepository messageDocumentRepository;
  private final RabbitTemplate rabbitTemplate;

  public void enter(String chatroomId, MessageDTO messageDTO, MemberInfoDTO memberInfoDTO) {
    MessageDocument systemMessageDocument = messageDTO.toDocument(SYSTEM_GREETING);
    ChatMessageSpecResponse systemMessageSpecResponse = ChatMessageSpecResponse.of(
        systemMessageDocument);
    checkChatroom(chatroomId, memberInfoDTO);
    messageDocumentRepository.save(systemMessageDocument);
    rabbitTemplate.convertAndSend(
        "room." + chatroomId + "." + memberInfoDTO.getEmail().split("@")[0],
        systemMessageSpecResponse);
  }

  public void sendMessage(String chatroomId, MessageDTO messageDTO, MemberInfoDTO memberInfoDTO) {
    MessageDocument messageDocument = messageDTO.toDocument(memberInfoDTO);
    ChatMessageSpecResponse chatMessageSpecResponse = ChatMessageSpecResponse.of(messageDocument);
    chatroomDocumentRepository.findById(chatroomId).orElseThrow(() -> new ChatroomNotFoundException(
        ErrorCode.NOT_FOUND));
    checkChatroom(chatroomId, memberInfoDTO);
    messageDocumentRepository.save(messageDocument);
    rabbitTemplate.convertAndSend(
        "room." + chatroomId + "." + memberInfoDTO.getEmail().split("@")[0],
        chatMessageSpecResponse);
  }

  private void checkChatroom(String chatroomId, MemberInfoDTO memberInfoDTO) {
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroomId)
        .orElseThrow(() -> new ChatroomNotFoundException(ErrorCode.NOT_FOUND));
    if (!chatroomDocument.getOwnerId().equals(memberInfoDTO.getId())
        && !chatroomDocument.getGuestId().equals(memberInfoDTO.getId())) {
      throw new NotChatroomMemberException(ErrorCode.VALIDATION_ERROR);
    }
    if((chatroomDocument.getOwnerId().equals(memberInfoDTO.getId()) && chatroomDocument.isDeletedByOwner())
    || (chatroomDocument.getGuestId().equals(memberInfoDTO.getId()) && chatroomDocument.isDeletedByGuest())) {
      throw new ExitedChatroomException(ErrorCode.BAD_REQUEST);
    }
  }


}
