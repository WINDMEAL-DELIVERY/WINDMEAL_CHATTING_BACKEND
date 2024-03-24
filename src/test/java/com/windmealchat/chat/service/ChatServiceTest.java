package com.windmealchat.chat.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.windmealchat.annotation.IntegrationTest;
import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageType;
import com.windmealchat.chat.dto.request.ChatroomLeaveRequest;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.ChatMessageResponse;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse.ChatroomSpecResponse;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.exception.ExitedChatroomException;
import com.windmealchat.chat.exception.NotChatroomMemberException;
import com.windmealchat.chat.exception.SingleChattingTrialException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.repository.MessageDocumentRepository;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;

@IntegrationTest
public class ChatServiceTest {

  @Autowired
  ChatroomDocumentRepository chatroomDocumentRepository;

  @Autowired
  MessageDocumentRepository messageDocumentRepository;

  @Autowired
  StompChatService stompChatService;

  @Autowired
  ChatroomService chatroomService;

  @MockBean
  RabbitService rabbitService;

  @Autowired
  AES256Util aes256Util;


  @AfterEach
  void tearDown() {
    chatroomDocumentRepository.deleteAll();
    messageDocumentRepository.deleteAll();
  }

  /**
   * 채팅방 생성은 채팅서버에서 이루어지지 않는다. <br/> 테스트를 위해서 임의로 채팅방을 생성해준다.
   */
  private ChatroomDocument createChatroom(Long orderId, Long ownerId, Long guestId,
      String ownerEmail,
      String guestEmail, String ownerNickname, String guestNickname) {
    ChatroomDocument chatroomDocument = ChatroomDocument.builder()
        .orderId(orderId)
        .ownerId(ownerId)
        .guestId(guestId)
        .ownerEmail(ownerEmail)
        .guestEmail(guestEmail)
        .build();
    return chatroomDocumentRepository.save(chatroomDocument);
  }

  private MemberInfoDTO createMemberInfoDTO(Long id, String email, String nickname) {
    return MemberInfoDTO.of(id, email, nickname);
  }

  private ChatroomLeaveRequest createChatroomLeaveRequest(String chatroomId) {
    return ChatroomLeaveRequest.builder()
        .chatroomId(chatroomId)
        .build();
  }

  private MessageDTO createMessageDTO(String chatroomId, String message) {
    return MessageDTO.builder()
        .chatRoomId(chatroomId)
        .type(MessageType.TEXT)
        .message(message)
        .build();
  }

  @Test
  @DisplayName("채팅방 목록 조회 - 성공")
  public void getChatroomsTest() throws Exception {
    //given
    List<MemberInfoDTO> ownerInfoList = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      ownerInfoList.add(createMemberInfoDTO((long) i, "owner" + i + "@gachon.ac.kr", "owner" + i));
    }
    MemberInfoDTO guestInfo = createMemberInfoDTO(6L, "guest@gachon.ac.kr", "guest");
    List<ChatroomDocument> chatroomDocumentList = ownerInfoList.stream()
        .map(owner -> createChatroom(owner.getId(), owner.getId(), guestInfo.getId(),
            owner.getEmail(),
            guestInfo.getEmail(), owner.getNickname(), guestInfo.getNickname()))
        .collect(Collectors.toList());
    List<ChatroomDocument> documentList = chatroomDocumentRepository.saveAll(
        chatroomDocumentList);

    // when
    when(
        rabbitService.getQueueMessages(any()))
        .thenReturn(4);
    PageRequest pageRequest = PageRequest.of(0, 5);

    //then
    ChatroomResponse response = chatroomService.getChatrooms(guestInfo, pageRequest);
    int size = response.getChatroomSpecResponses().getContent().size() - 1;
    for (int i = size; i >= 0; i--) {
      ChatroomSpecResponse each = response.getChatroomSpecResponses().getContent().get(size - i);
      assertThat(each).extracting(ChatroomSpecResponse::getChatroomId,
              ChatroomSpecResponse::getOrderId, ChatroomSpecResponse::getUncheckedMessageCount)
          .containsExactly(aes256Util.encrypt(documentList.get(i).getId()),
              documentList.get(i).getOrderId(), 4);
    }
  }

  @Test
  @DisplayName("채팅방 삭제(나가기) 테스트 - 성공")
  public void leaveChatroomTest() {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    chatroomService.leaveChatroom(ownerInfo, createChatroomLeaveRequest(chatroom.getId()));
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroom.getId()).get();

    //then
    assertThat(chatroomDocument.isDeletedByOwner()).isTrue();
  }

  @Test
  @DisplayName("채팅방 목록 조회 - 성공 : 나간 채팅방 제외")
  public void getActiveChatroomsTest() throws Exception {
    //given
    List<MemberInfoDTO> ownerInfoList = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      ownerInfoList.add(createMemberInfoDTO((long) i, "owner" + i + "@gachon.ac.kr", "owner" + i));
    }
    MemberInfoDTO guestInfo = createMemberInfoDTO(6L, "guest@gachon.ac.kr", "guest");
    List<ChatroomDocument> chatroomDocumentList = ownerInfoList.stream()
        .map(owner -> createChatroom(owner.getId(), owner.getId(), guestInfo.getId(),
            owner.getEmail(),
            guestInfo.getEmail(), owner.getNickname(), guestInfo.getNickname()))
        .collect(Collectors.toList());
    List<ChatroomDocument> documentList = chatroomDocumentRepository.saveAll(
        chatroomDocumentList);
    int originalSize = documentList.size() - 1;

    // when
    when(
        rabbitService.getQueueMessages(any()))
        .thenReturn(4);
    PageRequest pageRequest = PageRequest.of(0, 5);
    // 1, 2번째 채팅방에서 퇴장하겠다.
    chatroomService.leaveChatroom(guestInfo,
        createChatroomLeaveRequest(documentList.get(0).getId()));
    chatroomService.leaveChatroom(guestInfo,
        createChatroomLeaveRequest(documentList.get(1).getId()));

    //then
    ChatroomResponse response = chatroomService.getChatrooms(guestInfo, pageRequest);
    int size = response.getChatroomSpecResponses().getContent().size() - 1;
    assertThat(size).isEqualTo(2);
    for (int i = originalSize; i >= size; i--) {
      ChatroomSpecResponse each = response.getChatroomSpecResponses().getContent()
          .get(originalSize - i);
      assertThat(each).extracting(ChatroomSpecResponse::getChatroomId,
              ChatroomSpecResponse::getOrderId, ChatroomSpecResponse::getUncheckedMessageCount)
          .containsExactly(aes256Util.encrypt(documentList.get(i).getId()),
              documentList.get(i).getOrderId(), 4);
    }
  }

  @Test
  @DisplayName("채팅방 삭제(나가기) 테스트 - 실패 : 존재하지 않는 채팅방")
  public void leaveInvalidChatroomTest() {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    ChatroomLeaveRequest invalidChatroomRequest = createChatroomLeaveRequest("invalidChatroomId");
    //when & then
    assertThatThrownBy(() -> chatroomService.leaveChatroom(ownerInfo, invalidChatroomRequest))
        .isInstanceOf(ChatroomNotFoundException.class)
        .hasMessage("채팅방을 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("채팅방 삭제(나가기) 테스트 - 실패 : 채팅방의 구성원이 아님")
  public void leaveOthersChatroomTest() {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    MemberInfoDTO currentMemberInfo = createMemberInfoDTO(3L, "currentMember@gachon.ac.kr",
        "currentMember");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());
    ChatroomLeaveRequest chatroomLeaveRequest = createChatroomLeaveRequest(chatroom.getId());
    //when & then
    assertThatThrownBy(() -> chatroomService.leaveChatroom(currentMemberInfo, chatroomLeaveRequest))
        .isInstanceOf(NotChatroomMemberException.class)
        .hasMessage("사용자는 해당 채팅방의 멤버가 아닙니다.");
  }

  @Test
  @DisplayName("채팅방 삭제(나가기) 테스트 - 실패 : 이미 나간 채팅방")
  public void leaveAlreadyLeftChatroomTest() {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    chatroomService.leaveChatroom(ownerInfo, createChatroomLeaveRequest(chatroom.getId()));

    //then
    assertThatThrownBy(() -> chatroomService.leaveChatroom(ownerInfo,
        createChatroomLeaveRequest(chatroom.getId())))
        .isInstanceOf(ExitedChatroomException.class)
        .hasMessage("이미 사용자가 나간 채팅방입니다.");
  }

  @Test
  @DisplayName("메시지 전송 - 성공")
  public void sendMessageTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    MessageDTO messageDTO = createMessageDTO(chatroom.getId(), "테스트용 채팅 메시지");
    stompChatService.sendMessage(aes256Util.encrypt(chatroom.getId()), messageDTO, ownerInfo);
    PageRequest pageRequest = PageRequest.of(0, 10);

    //then
    ChatMessageResponse chatMessages = chatroomService.getChatMessages(ownerInfo, pageRequest,
        chatroom.getId());
    assertThat(chatMessages.getChatMessageSpecResponses().getContent().get(0)).extracting(
            ChatMessageSpecResponse::getMessage,
            ChatMessageSpecResponse::getSenderId)
        .containsExactly("테스트용 채팅 메시지", ownerInfo.getId());

    ChatroomResponse chatrooms = chatroomService.getChatrooms(ownerInfo, pageRequest);
    assertThat(chatrooms.getChatroomSpecResponses().getContent().get(0)).extracting(
            ChatroomSpecResponse::getLastMessage,
            ChatroomSpecResponse::getOpponentNickname)
        .containsExactly("테스트용 채팅 메시지", guestInfo.getNickname());
  }

  @Test
  @DisplayName("메시지 전송 - 실패 : 존재하지 않는 채팅방")
  public void sendInvalidChatroomMessageTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    MessageDTO messageDTO = createMessageDTO(chatroom.getId(), "테스트용 채팅 메시지");

    //then
    assertThatThrownBy(
        () -> stompChatService.sendMessage(aes256Util.encrypt("invalidChatroomId"), messageDTO,
            ownerInfo))
        .isInstanceOf(ChatroomNotFoundException.class)
        .hasMessage("채팅방을 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("메시지 전송 - 실패 : 채팅방의 구성원이 아님")
  public void sendMessageToOthersChatroomTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    MemberInfoDTO currentMemberInfo = createMemberInfoDTO(3L, "currentMember@gachon.ac.kr",
        "currentMember");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    MessageDTO messageDTO = createMessageDTO(chatroom.getId(), "테스트용 채팅 메시지");

    //then
    assertThatThrownBy(
        () -> stompChatService.sendMessage(aes256Util.encrypt(chatroom.getId()), messageDTO,
            currentMemberInfo))
        .isInstanceOf(NotChatroomMemberException.class)
        .hasMessage("사용자는 해당 채팅방의 멤버가 아닙니다.");
  }

  @Test
  @DisplayName("메시지 전송 - 실패 : 이미 나간 채팅방")
  public void sendMessageToAlreadyLeftChatroomTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    ChatroomLeaveRequest chatroomLeaveRequest = createChatroomLeaveRequest(chatroom.getId());
    chatroomService.leaveChatroom(ownerInfo, chatroomLeaveRequest);
    MessageDTO messageDTO = createMessageDTO(chatroom.getId(), "테스트용 채팅 메시지");

    //then
    assertThatThrownBy(
        () -> stompChatService.sendMessage(aes256Util.encrypt(chatroom.getId()), messageDTO,
            ownerInfo))
        .isInstanceOf(ExitedChatroomException.class)
        .hasMessage("이미 사용자가 나간 채팅방입니다.");
  }

  @Test
  @DisplayName("메시지 전송 - 실패 : 상대방이 나간 채팅방")
  public void sendSingleMessageTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    ChatroomLeaveRequest chatroomLeaveRequest = createChatroomLeaveRequest(chatroom.getId());
    chatroomService.leaveChatroom(guestInfo, chatroomLeaveRequest);
    MessageDTO messageDTO = createMessageDTO(chatroom.getId(), "테스트용 채팅 메시지");

    //then
    assertThatThrownBy(
        () -> stompChatService.sendMessage(aes256Util.encrypt(chatroom.getId()), messageDTO,
            ownerInfo))
        .isInstanceOf(SingleChattingTrialException.class)
        .hasMessage("혼자 있는 채팅방에서는 채팅을 보낼 수 없습니다.");
  }

  @Test
  @DisplayName("채팅방 메시지 조회 - 성공")
  public void getChatroomMessagesTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    String prefix = "테스트용 채팅 메시지";
    for (int i = 0; i < 10; i++) {
      MessageDTO messageDTO = createMessageDTO(chatroom.getId(), prefix + (i + 1));
      stompChatService.sendMessage(aes256Util.encrypt(chatroom.getId()), messageDTO,
          i % 2 == 0 ? ownerInfo : guestInfo);
    }
    PageRequest pageRequest = PageRequest.of(0, 10);

    //then
    ChatMessageResponse chatMessages = chatroomService.getChatMessages(ownerInfo, pageRequest,
        chatroom.getId());

    List<ChatMessageSpecResponse> messages = chatMessages.getChatMessageSpecResponses()
        .getContent();
    int size = chatMessages.getChatMessageSpecResponses().getContent().size() - 1;
    for (int i = size; i >= 0; i--) {
      assertThat(messages.get(size - i)).extracting(ChatMessageSpecResponse::getMessage,
              ChatMessageSpecResponse::getSenderId)
          .containsExactly(prefix + (i + 1), i % 2 == 0 ? ownerInfo.getId() : guestInfo.getId());
    }
  }

  @Test
  @DisplayName("채팅방 메시지 조회 - 성공 : 상대방이 나간 채팅방")
  public void getOpponentLeftChatroomMessagesTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    String prefix = "테스트용 채팅 메시지";
    for (int i = 0; i < 10; i++) {
      MessageDTO messageDTO = createMessageDTO(chatroom.getId(), prefix + (i + 1));
      stompChatService.sendMessage(aes256Util.encrypt(chatroom.getId()), messageDTO,
          i % 2 == 0 ? ownerInfo : guestInfo);
    }
    PageRequest pageRequest = PageRequest.of(0, 10);
    ChatroomLeaveRequest chatroomLeaveRequest = createChatroomLeaveRequest(chatroom.getId());
    chatroomService.leaveChatroom(guestInfo, chatroomLeaveRequest);

    //then
    ChatMessageResponse chatMessages = chatroomService.getChatMessages(ownerInfo, pageRequest,
        chatroom.getId());

    List<ChatMessageSpecResponse> messages = chatMessages.getChatMessageSpecResponses()
        .getContent();
    int size = chatMessages.getChatMessageSpecResponses().getContent().size() - 1;
    for (int i = size; i >= 0; i--) {
      assertThat(messages.get(size - i)).extracting(ChatMessageSpecResponse::getMessage,
              ChatMessageSpecResponse::getSenderId)
          .containsExactly(prefix + (i + 1), i % 2 == 0 ? ownerInfo.getId() : guestInfo.getId());
    }
  }


  @Test
  @DisplayName("채팅방 메시지 조회 - 실패 : 존재하지 않는 채팅방")
  public void getInvalidChatroomMessagesTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    PageRequest pageRequest = PageRequest.of(0, 10);

    //then
    assertThatThrownBy(
        () -> chatroomService.getChatMessages(ownerInfo, pageRequest, "invalidChatroomId"))
        .isInstanceOf(ChatroomNotFoundException.class)
        .hasMessage("채팅방을 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("채팅방 메시지 조회 - 실패 : 채팅방의 구성원이 아님")
  public void getOthersChatroomMessagesTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    MemberInfoDTO currentMemberInfo = createMemberInfoDTO(3L, "currentMember@gachon.ac.kr",
        "currentMember");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    PageRequest pageRequest = PageRequest.of(0, 10);

    //then
    assertThatThrownBy(() -> chatroomService.getChatMessages(currentMemberInfo, pageRequest,
        chatroom.getId()))
        .isInstanceOf(NotChatroomMemberException.class)
        .hasMessage("사용자는 해당 채팅방의 멤버가 아닙니다.");
  }

  @Test
  @DisplayName("채팅방 메시지 조회 - 실패 : 이미 나간 채팅방")
  public void getAlreadyLeftChatroomMessagesTest() throws Exception {
    //given
    MemberInfoDTO ownerInfo = createMemberInfoDTO(1L, "owner@gachon.ac.kr", "owner");
    MemberInfoDTO guestInfo = createMemberInfoDTO(2L, "guest@gachon.ac.kr", "guest");
    ChatroomDocument chatroom = createChatroom(1L, ownerInfo.getId(), guestInfo.getId(),
        ownerInfo.getEmail(), guestInfo.getEmail(),
        ownerInfo.getNickname(), guestInfo.getNickname());

    //when
    ChatroomLeaveRequest chatroomLeaveRequest = createChatroomLeaveRequest(chatroom.getId());
    chatroomService.leaveChatroom(ownerInfo, chatroomLeaveRequest);
    PageRequest pageRequest = PageRequest.of(0, 10);

    //then
    assertThatThrownBy(() -> chatroomService.getChatMessages(ownerInfo, pageRequest,
        chatroom.getId()))
        .isInstanceOf(ExitedChatroomException.class)
        .hasMessage("이미 사용자가 나간 채팅방입니다.");
  }
}
