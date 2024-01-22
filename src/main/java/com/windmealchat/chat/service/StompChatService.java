package com.windmealchat.chat.service;

import static com.windmealchat.global.constants.ExceptionConstants.INTERNAL_ENCRYPTION_EXCEPTION;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.exception.ExitedChatroomException;
import com.windmealchat.chat.exception.NotChatroomMemberException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.exception.AesException;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StompChatService {

  private final ChatroomDocumentRepository chatroomDocumentRepository;
  private final MessageDocumentRepository messageDocumentRepository;
  private final RabbitService rabbitService;
  private final AES256Util aes256Util;

  public void sendMessage(String encryptedChatroomId, MessageDTO messageDTO,
      MemberInfoDTO memberInfoDTO) {

    MessageDocument messageDocument = messageDTO.toDocument(memberInfoDTO);
    ChatMessageSpecResponse chatMessageSpecResponse = ChatMessageSpecResponse.of(messageDocument);
    // TODO 여기 있는 모든 예외 메시지들 모두 MessageDeliveryException으로 바꿔보기
    String chatroomId = aes256Util.decrypt(encryptedChatroomId)
        .orElseThrow(() -> new AesException(ErrorCode.ENCRYPT_ERROR));
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroomId)
        .orElseThrow(() -> new ChatroomNotFoundException(ErrorCode.NOT_FOUND));
    checkChatroom(chatroomDocument, memberInfoDTO);
    messageDocumentRepository.save(messageDocument);
    // 메시지를 각각 전송한다.
    String otherEmail = memberInfoDTO.getEmail().equals(chatroomDocument.getOwnerEmail())
        ? chatroomDocument.getGuestEmail() : chatroomDocument.getOwnerEmail();
    rabbitService.createQueue(encryptedChatroomId, otherEmail);
    rabbitService.sendMessage(encryptedChatroomId, memberInfoDTO.getEmail(),
        chatMessageSpecResponse);
    rabbitService.sendMessage(encryptedChatroomId, otherEmail, chatMessageSpecResponse);
  }

  private void checkChatroom(ChatroomDocument chatroomDocument, MemberInfoDTO memberInfoDTO) {
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
