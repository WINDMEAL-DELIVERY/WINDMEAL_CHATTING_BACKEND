package com.windmealchat.chat.service;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.dto.response.ChatMessageResponse;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.exception.NotChatroomMemberException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatroomService {

  private final ChatroomDocumentRepository chatroomDocumentRepository;
  private final MessageDocumentRepository messageDocumentRepository;

  public ChatMessageResponse findChatMessages(MemberInfoDTO memberInfoDTO, Pageable pageable,
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
    Slice<ChatMessageSpecResponse> chatMessageSpecResponses = messageDocuments.map(ChatMessageSpecResponse::of);
    return ChatMessageResponse.of(chatMessageSpecResponses);
  }
}
