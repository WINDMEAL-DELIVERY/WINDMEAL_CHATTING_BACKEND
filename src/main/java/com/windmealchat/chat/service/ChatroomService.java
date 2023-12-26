package com.windmealchat.chat.service;

import com.rabbitmq.client.AMQP;
import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.response.ChatMessageResponse;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse.ChatroomSpecResponse;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.exception.NotChatroomMemberException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatroomService {

  private final ChatroomDocumentRepository chatroomDocumentRepository;
  private final MessageDocumentRepository messageDocumentRepository;
  private final RabbitTemplate rabbitTemplate;


  public ChatroomResponse getChatrooms(MemberInfoDTO memberInfoDTO, Pageable pageable) {
    Slice<ChatroomDocument> chatroomDocumentSlice = chatroomDocumentRepository.findByOwnerIdOrGuestId(
        memberInfoDTO.getId(), memberInfoDTO.getId(), pageable);

    Slice<ChatroomSpecResponse> map = chatroomDocumentSlice.map(
        chatroomDocument -> toChatroomSpecResponse(chatroomDocument, memberInfoDTO));
    return ChatroomResponse.of(map);
  }

  public ChatMessageResponse getChatMessages(MemberInfoDTO memberInfoDTO, Pageable pageable,
      String chatRoomId) {

    // 먼저 사용자가 전달한 채팅방 id로 존재하는 채팅방이 있는지 찾는다.
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatRoomId)
        .orElseThrow(() -> new ChatroomNotFoundException(
            ErrorCode.NOT_FOUND));

    // 찾아온 채팅방에 사용자가 속하는지 확인한다.
    if (!chatroomDocument.getOrderId().equals(memberInfoDTO.getId())
        && !chatroomDocument.getGuestId().equals(memberInfoDTO.getId())) {
      throw new NotChatroomMemberException(ErrorCode.BAD_REQUEST);
    }
    // 채팅방 아이디로 페이지네이션이 적용된 채팅 메시지를 가져온다.
    Slice<MessageDocument> messageDocuments = messageDocumentRepository.findByChatroomId(
        chatroomDocument.getId(), pageable);
    Slice<ChatMessageSpecResponse> chatMessageSpecResponses = messageDocuments.map(
        ChatMessageSpecResponse::of);
    return ChatMessageResponse.of(chatMessageSpecResponses);
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
    String queueName = "room." + chatroomDocument.getId() + "." + memberInfoDTO.getEmail().split("@")[0];
    AMQP.Queue.DeclareOk dok = rabbitTemplate.execute(
        channel -> channel.queueDeclare(queueName, true, false, false, new HashMap<>()));
    return ChatroomSpecResponse.of(chatroomDocument.getId(), messageDocument,
        dok.getMessageCount());
  }

}
