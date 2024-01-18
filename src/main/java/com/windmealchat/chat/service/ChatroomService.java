package com.windmealchat.chat.service;

import com.rabbitmq.client.AMQP;
import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.request.ChatroomLeaveRequest;
import com.windmealchat.chat.dto.response.ChatMessageResponse;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse.ChatroomSpecResponse;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.exception.ExitedChatroomException;
import com.windmealchat.chat.exception.NotChatroomMemberException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
  private final RabbitService rabbitService;


  /**
   * 요청자가 속한 채팅방을 페이지네이션을 적용하여 조회한다. <br/>
   * 채팅방의 정보와 더불어 사용자가 읽지 않은 채팅의 수, 마지막 채팅 메시지를 조회한다.
   * @param pageable
   * @return
   */
  public ChatroomResponse getChatrooms(MemberInfoDTO memberInfoDTO, Pageable pageable) {
    List<ChatroomDocument> activeChatrooms = chatroomDocumentRepository.findAllActiveChatrooms(
        memberInfoDTO.getId(), memberInfoDTO.getId());
    List<ChatroomSpecResponse> chatroomSpecResponses = activeChatrooms.stream()
        .map(chatroomDocument -> toChatroomSpecResponse(chatroomDocument, memberInfoDTO)).sorted()
        .collect(Collectors.toList());
    Slice<ChatroomSpecResponse> ChatroomSpecResponseSlice = new SliceImpl<>(chatroomSpecResponses, pageable,
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
      String chatRoomId) {
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatroomNotFoundException(
            ErrorCode.NOT_FOUND));
    checkChatroom(chatRoomId, memberInfoDTO);
    Slice<MessageDocument> messageDocuments = messageDocumentRepository.findByChatroomId(
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
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(
            chatroomLeaveRequest.getChatroomId())
        .orElseThrow(() -> new ChatroomNotFoundException(ErrorCode.NOT_FOUND));
    checkChatroom(chatroomLeaveRequest.getChatroomId(), memberInfoDTO);
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
    String oppositeAlarmToken = memberInfoDTO.getId().equals(chatroomDocument.getOwnerId())
        ? chatroomDocument.getOwnerAlarmToken() : chatroomDocument.getGuestAlarmToken();
    return ChatroomSpecResponse.of(chatroomDocument, messageDocument, queueMessageCount,
        oppositeAlarmToken);
  }

  /**
   * 사용자가 이전에 나갔던 채팅방은 아닌지 검증한다. 채팅방에 사용자가 주인이나 손님으로 존재하는지, 존재한다면 이전에 나가지는 않았는지 순으로 검증한다.
   *
   * @param chatroomId
   * @param memberInfoDTO
   */
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

  private String buildQueueName(ChatroomDocument chatroomDocument, MemberInfoDTO memberInfoDTO) {
    return "room." + chatroomDocument.getId() + "." + memberInfoDTO.getEmail().split("@")[0];
  }

}
