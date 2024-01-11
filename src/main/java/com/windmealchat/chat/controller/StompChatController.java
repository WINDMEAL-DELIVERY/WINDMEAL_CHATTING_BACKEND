package com.windmealchat.chat.controller;

import static com.windmealchat.global.constants.RabbitConstants.CHAT_QUEUE_NAME;

import com.windmealchat.alarm.dto.FcmNotificationRequest;
import com.windmealchat.alarm.service.FcmNotificationService;
import com.windmealchat.chat.dto.request.ChatInitialRequest;
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
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

  private final FcmNotificationService fcmNotificationService;
  private final StompChatService stompChatService;
  private final TokenService tokenService;

  @MessageMapping(value = "chat.enter.{chatRoomId}")
  public void enter(@DestinationVariable String chatRoomId, ChatInitialRequest chatInitialRequest,
      StompHeaderAccessor accessor) {
    Optional<MemberInfoDTO> memberInfoOptional = tokenService.resolveJwtToken(accessor);
    if (memberInfoOptional.isPresent()) {
      stompChatService.enter(chatRoomId, chatInitialRequest, memberInfoOptional.get());
    }
  }

  @MessageMapping(value = "chat.message.{chatRoomId}")
  public void sendMessage(@DestinationVariable String chatRoomId, MessageDTO messageDTO,
      StompHeaderAccessor accessor) {
    Optional<MemberInfoDTO> memberInfoOptional = tokenService.resolveJwtToken(accessor);
    String alarmToken = tokenService.resolveAlarmToken(accessor);
    if (memberInfoOptional.isPresent()) {
      sendNotification(messageDTO, alarmToken);
      stompChatService.sendMessage(chatRoomId, messageDTO, memberInfoOptional.get());
    }
  }


  @RabbitListener(queues = CHAT_QUEUE_NAME)
  public void receive(ChatMessageSpecResponse chatMessageSpecResponse) {
    System.out.println("received : " + chatMessageSpecResponse.getMessage());
  }

  private void sendNotification(MessageDTO messageDTO, String token) {
    fcmNotificationService.sendNotification(
        FcmNotificationRequest.of(messageDTO.getType().name(), messageDTO.getMessage()),
        token);
  }

}
