package com.windmealchat.chat.service;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.dto.response.SingleMessageResponse;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.exception.AesException;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.global.util.ChatroomValidator;
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
  private final ChatroomValidator chatroomValidator;
  private final RabbitService rabbitService;
  private final AES256Util aes256Util;

  public void sendMessage(String encryptedChatroomId, MessageDTO messageDTO,
      MemberInfoDTO memberInfoDTO) {
    String chatroomId = aes256Util.decrypt(encryptedChatroomId)
        .orElseThrow(() -> new AesException(ErrorCode.ENCRYPT_ERROR));
    // TODO 여기 있는 모든 예외 메시지들 모두 MessageDeliveryException으로 바꿔보기
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroomId)
        .orElseThrow(() -> new ChatroomNotFoundException(ErrorCode.NOT_FOUND));
    chatroomValidator.checkChatroomForSend(chatroomId, memberInfoDTO);
    MessageDocument savedMessage = messageDocumentRepository.save(
        messageDTO.toDocument(chatroomId, memberInfoDTO));

    // 메시지를 각각 전송한다.
    boolean isOwner = memberInfoDTO.getEmail().equals(chatroomDocument.getOwnerEmail());
    String otherEmail =
        isOwner ? chatroomDocument.getGuestEmail() : chatroomDocument.getOwnerEmail();
    // 채팅 웹소켓
    rabbitService.createQueue(encryptedChatroomId, otherEmail);
    rabbitService.sendChatMessage(encryptedChatroomId, memberInfoDTO.getEmail(),
        ChatMessageSpecResponse.of(savedMessage, true));
    rabbitService.sendChatMessage(encryptedChatroomId, otherEmail,
        ChatMessageSpecResponse.of(savedMessage, false));

    // 채팅방 웹소켓
    SingleMessageResponse singleMessageResponse = new SingleMessageResponse(savedMessage);
    rabbitService.sendChatroomInfo(memberInfoDTO.getId(), memberInfoDTO.getEmail(),
        singleMessageResponse);
    rabbitService.sendChatroomInfo(
        isOwner ? chatroomDocument.getGuestId() : chatroomDocument.getOwnerId(),
        isOwner ? chatroomDocument.getGuestEmail() : chatroomDocument.getOwnerEmail(),
        singleMessageResponse);
  }


}
