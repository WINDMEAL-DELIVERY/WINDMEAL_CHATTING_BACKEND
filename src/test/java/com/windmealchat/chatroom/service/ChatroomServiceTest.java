package com.windmealchat.chatroom.service;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.windmealchat.annotation.IntegrationTest;
import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.dto.request.ChatroomLeaveRequest;
import com.windmealchat.chat.dto.response.ChatroomResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse.ChatroomSpecResponse;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.chat.service.ChatroomService;
import com.windmealchat.chat.service.RabbitService;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;

@IntegrationTest
public class ChatroomServiceTest {

  @Autowired
  ChatroomDocumentRepository chatroomDocumentRepository;

  @Autowired
  ChatroomService chatroomService;

  @MockBean
  RabbitService rabbitService;

  @Autowired
  AES256Util aes256Util;

  @AfterEach
  void tearDown() {
    chatroomDocumentRepository.deleteAll();
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
        .ownerNickname(ownerNickname)
        .guestNickname(guestNickname)
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

  private String buildQueueName(ChatroomDocument chatroomDocument, MemberInfoDTO memberInfoDTO) {
    return "room." + chatroomDocument.getId() + "." + memberInfoDTO.getEmail().split("@")[0];
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
    chatroomService.leaveChatroom(guestInfo, createChatroomLeaveRequest(documentList.get(0).getId()));
    chatroomService.leaveChatroom(guestInfo, createChatroomLeaveRequest(documentList.get(1).getId()));

    //then
    ChatroomResponse response = chatroomService.getChatrooms(guestInfo, pageRequest);
    int size = response.getChatroomSpecResponses().getContent().size() - 1;
    assertThat(size).isEqualTo(2);
    for (int i = originalSize; i >= size; i--) {
      ChatroomSpecResponse each = response.getChatroomSpecResponses().getContent().get(originalSize - i);
      assertThat(each).extracting(ChatroomSpecResponse::getChatroomId,
              ChatroomSpecResponse::getOrderId, ChatroomSpecResponse::getUncheckedMessageCount)
          .containsExactly(aes256Util.encrypt(documentList.get(i).getId()),
              documentList.get(i).getOrderId(), 4);
    }
  }

  @Test
  @DisplayName("채팅방 삭제(나가기) 테스트 - 실패 : 존재하지 않는 채팅방")
  public void leaveInvalidChatroom() {
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

}
