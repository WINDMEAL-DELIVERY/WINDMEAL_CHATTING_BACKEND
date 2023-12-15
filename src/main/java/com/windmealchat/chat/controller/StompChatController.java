package com.windmealchat.chat.controller;

import static com.windmealchat.global.constants.RabbitConstants.CHAT_QUEUE_NAME;

import com.windmealchat.alarm.dto.FcmNotificationRequest;
import com.windmealchat.alarm.service.FcmNotificationService;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.service.StompChatService;
import com.windmealchat.global.token.service.TokenService;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

  private final FcmNotificationService fcmNotificationService;
  private final StompChatService stompChatService;
  private final TokenService tokenService;

//  @MessageMapping(value = "/chat/enter")
  @MessageMapping(value = "chat.enter.{chatRoomId}")
  public void enter(@DestinationVariable String chatRoomId, MessageDTO messageDTO, SimpMessageHeaderAccessor accessor) {
    // 채팅방에 처음 사용자가 참가할때, SYSTEM 타입의 메세지를 전송해주는 컨트롤러
      sendNotification(messageDTO, tokenService.resolveAlarmToken(accessor));
      stompChatService.enter(chatRoomId, messageDTO);
  }

//  @MessageMapping(value = "/chat/message")
  @MessageMapping(value = "chat.message.{chatRoomId}")
  public void sendMessage(@DestinationVariable String chatRoomId,  MessageDTO messageDTO, SimpMessageHeaderAccessor accessor) {
    // TODO 메세지 타입에 따른 처리 : 만약 이미지 타입의 메시지가 오게 된다면 그에 따른 처리를 해주어야 한다.
    Optional<MemberInfoDTO> memberInfoOptional = tokenService.resolveJwtToken(accessor);
    String alarmToken = tokenService.resolveAlarmToken(accessor);
    if (memberInfoOptional.isPresent()) {
      sendNotification(messageDTO, alarmToken);
      stompChatService.sendMessage(chatRoomId, messageDTO, memberInfoOptional.get());
    }
  }


  @RabbitListener(queues = CHAT_QUEUE_NAME)
  public void receive(ChatMessageSpecResponse chatMessageSpecResponse){

    System.out.println("received : " + chatMessageSpecResponse.getMessage());
  }

  private void sendNotification(MessageDTO messageDTO, String token) {
    fcmNotificationService.sendNotification(
        FcmNotificationRequest.of(messageDTO.getType().name(), messageDTO.getMessage()),
        token);
  }

}
