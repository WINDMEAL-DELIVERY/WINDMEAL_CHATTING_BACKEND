package com.windmealchat.chat.service;

import com.windmealchat.chat.dto.response.ChatMessageResponse;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.awt.print.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatroomService {
  private final ChatroomDocumentRepository chatroomDocumentRepository;
  private final MessageDocumentRepository messageDocumentRepository;

  public ChatMessageResponse findChatMessages(MemberInfoDTO memberInfoDTO, Pageable pageable, String chatRoomId) {

  }
}
