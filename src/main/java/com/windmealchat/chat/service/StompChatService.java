package com.windmealchat.chat.service;

import static com.windmealchat.global.constants.StompConstants.SYSTEM_GREETING;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
import com.windmealchat.chat.dto.request.ChatInitialRequest;
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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StompChatService {

  private final ChatroomDocumentRepository chatroomDocumentRepository;
  private final MessageDocumentRepository messageDocumentRepository;
  private final RabbitService rabbitService;

  public void enter(String chatroomId, ChatInitialRequest chatInitialRequest, MemberInfoDTO memberInfoDTO) {
    MessageDTO messageDTO = MessageDTO.builder()
        .chatRoomId(chatroomId)
        .type(MessageType.SYSTEM)
        .build();
    MessageDocument systemMessageDocument = messageDTO.toDocument(SYSTEM_GREETING);
    ChatMessageSpecResponse systemMessageSpecResponse = ChatMessageSpecResponse.of(
        systemMessageDocument);
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroomId)
        .orElseThrow(() -> new ChatroomNotFoundException(ErrorCode.NOT_FOUND));
    if (chatroomDocument.isDeletedByGuest() || chatroomDocument.isDeletedByOwner()) {
      throw new ExitedChatroomException(ErrorCode.BAD_REQUEST);
    }
    // 상대방의 큐를 생성한다.
    rabbitService.createQueue(chatroomId, chatInitialRequest);
    messageDocumentRepository.save(systemMessageDocument);
    // 메시지를 각각 전송한다.
    rabbitService.sendMessage(chatroomId, memberInfoDTO.getEmail(), systemMessageSpecResponse);
    rabbitService.sendMessage(chatroomId, chatInitialRequest.getOppositeEmail(), systemMessageSpecResponse);
  }

  public void sendMessage(String chatroomId, MessageDTO messageDTO, MemberInfoDTO memberInfoDTO) {
    MessageDocument messageDocument = messageDTO.toDocument(memberInfoDTO);
    ChatMessageSpecResponse chatMessageSpecResponse = ChatMessageSpecResponse.of(messageDocument);
    chatroomDocumentRepository.findById(chatroomId).orElseThrow(() -> new ChatroomNotFoundException(
        ErrorCode.NOT_FOUND));
    checkChatroom(chatroomId, memberInfoDTO);
    messageDocumentRepository.save(messageDocument);
    // 메시지를 각각 전송한다.
    rabbitService.sendMessage(chatroomId, memberInfoDTO.getEmail(), chatMessageSpecResponse);
    rabbitService.sendMessage(chatroomId, messageDTO.getOppositeEmail(), chatMessageSpecResponse);
  }

  private void checkChatroom(String chatroomId, MemberInfoDTO memberInfoDTO) {
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroomId)
        .orElseThrow(() -> new ChatroomNotFoundException(ErrorCode.NOT_FOUND));
    if (!chatroomDocument.getOwnerId().equals(memberInfoDTO.getId())
        && !chatroomDocument.getGuestId().equals(memberInfoDTO.getId())) {
      throw new NotChatroomMemberException(ErrorCode.VALIDATION_ERROR);
    }
    if ((chatroomDocument.getOwnerId().equals(memberInfoDTO.getId())
        && chatroomDocument.isDeletedByOwner())
        || (chatroomDocument.getGuestId().equals(memberInfoDTO.getId())
        && chatroomDocument.isDeletedByGuest())) {
      throw new ExitedChatroomException(ErrorCode.BAD_REQUEST);
    }
  }



}
