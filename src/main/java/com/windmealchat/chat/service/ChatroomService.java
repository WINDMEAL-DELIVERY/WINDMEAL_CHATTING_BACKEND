package com.windmealchat.chat.service;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.request.ChatroomLeaveRequest;
import com.windmealchat.chat.dto.response.ChatMessageResponse;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse.ChatroomSpecResponse;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.exception.AesException;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.global.util.ChatroomValidator;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatroomService {

  private final ChatroomDocumentRepository chatroomDocumentRepository;
  private final MessageDocumentRepository messageDocumentRepository;
  private final ChatroomValidator chatroomValidator;
  private final RabbitService rabbitService;
  private final AES256Util aes256Util;


  /**
   * 요청자가 속한 채팅방을 페이지네이션을 적용하여 조회한다. <br/> 채팅방의 정보와 더불어 사용자가 읽지 않은 채팅의 수, 마지막 채팅 메시지를 조회한다.
   *
   * @param pageable
   * @return
   */
  public ChatroomResponse getChatrooms(MemberInfoDTO memberInfoDTO, Pageable pageable) {
    List<ChatroomDocument> activeChatrooms = chatroomDocumentRepository.findAllActiveChatrooms(
        memberInfoDTO.getId(), memberInfoDTO.getId());
    List<ChatroomSpecResponse> chatroomSpecResponses = activeChatrooms.stream()
        .map(chatroomDocument -> toChatroomSpecResponse(chatroomDocument, memberInfoDTO)).sorted()
        .collect(Collectors.toList());
    Slice<ChatroomSpecResponse> ChatroomSpecResponseSlice = new SliceImpl<>(chatroomSpecResponses,
        pageable,
        chatroomSpecResponses.size() > pageable.getPageSize() * pageable.getPageNumber());
    return ChatroomResponse.of(ChatroomSpecResponseSlice);
  }

  /**
   * 특정 채팅방에 속하는 채팅을 페이지네이션을 적용하여 조회한다.
   *
   * @param memberInfoDTO
   * @param pageable
   * @param chatRoomId
   * @return
   */
  public ChatMessageResponse getChatMessages(MemberInfoDTO memberInfoDTO, Pageable pageable,
      String encryptedChatroomId) {
    String chatroomId = aes256Util.decrypt(encryptedChatroomId)
        .orElseThrow(() -> new AesException(ErrorCode.ENCRYPT_ERROR));
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroomId)
        .orElseThrow(() -> new ChatroomNotFoundException(
            ErrorCode.NOT_FOUND));
    chatroomValidator.checkChatroomForRead(chatroomId, memberInfoDTO);
    Slice<MessageDocument> messageDocuments = messageDocumentRepository.findByChatroomIdOrderByMessageIdDesc(
        chatroomDocument.getId(), pageable);
    Slice<ChatMessageSpecResponse> chatMessageSpecResponses = messageDocuments.map(
        ChatMessageSpecResponse::of);
    return ChatMessageResponse.of(chatMessageSpecResponses);
  }

  /**
   * 특정 채팅방에서 나간다.
   *
   * @param memberInfoDTO
   * @param chatroomLeaveRequest
   */
  public void leaveChatroom(MemberInfoDTO memberInfoDTO,
      ChatroomLeaveRequest chatroomLeaveRequest) {
    String chatroomId = aes256Util.decrypt(chatroomLeaveRequest.getChatroomId())
        .orElseThrow(() -> new AesException(ErrorCode.ENCRYPT_ERROR));
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroomId)
        .orElseThrow(() -> new ChatroomNotFoundException(ErrorCode.NOT_FOUND));
    chatroomValidator.checkChatroomForRead(chatroomId, memberInfoDTO);
    if (chatroomDocument.getOwnerId().equals(memberInfoDTO.getId())) {
      chatroomDocument.updateIsDeletedByOwner();
    } else {
      chatroomDocument.updateIsDeletedByGuest();
    }
    chatroomDocumentRepository.save(chatroomDocument);
    // rabbitmq queue 나가기
    rabbitService.deleteQueue(buildQueueName(chatroomDocument, memberInfoDTO));
  }

  /**
   * 채팅방 정보에 마지막 메시지와 읽지 않은 메시지의 수를 함께 조회하기 위해서 관련 로직을 처리해준다.
   * {@link com.windmealchat.chat.domain.ChatroomDocument}를 매개변수로 받아서,
   * {@link com.windmealchat.chat.dto.response.ChatroomResponse.ChatroomSpecResponse}로 반환해준다.
   *
   * @param chatroomDocument
   * @param memberInfoDTO
   * @return {@link com.windmealchat.chat.dto.response.ChatroomResponse.ChatroomSpecResponse}
   */
  private ChatroomSpecResponse toChatroomSpecResponse(ChatroomDocument chatroomDocument,
      MemberInfoDTO memberInfoDTO) {
    MessageDocument messageDocument = messageDocumentRepository.findTopByChatroomIdOrderByCreatedTimeDesc(
        chatroomDocument.getId());
    int queueMessageCount = rabbitService.getQueueMessages(
        buildQueueName(chatroomDocument, memberInfoDTO));

    try {
      String encrypt = aes256Util.encrypt(chatroomDocument.getId());
      return ChatroomSpecResponse.of(chatroomDocument, encrypt, messageDocument, queueMessageCount,
          memberInfoDTO);
    } catch (Exception e) {
      throw new AesException(ErrorCode.ENCRYPT_ERROR);
    }
  }


  private String buildQueueName(ChatroomDocument chatroomDocument, MemberInfoDTO memberInfoDTO) {
    return "room." + chatroomDocument.getId() + "." + memberInfoDTO.getEmail().split("@")[0];
  }

}
