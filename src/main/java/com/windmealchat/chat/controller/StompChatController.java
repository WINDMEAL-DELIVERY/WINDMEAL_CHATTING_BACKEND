package com.windmealchat.chat.controller;

import com.windmealchat.alarm.dto.FcmNotificationRequest;
import com.windmealchat.alarm.service.FcmNotificationService;
import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.service.ChatService;
import com.windmealchat.global.token.service.TokenService;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

  private final ChatService chatService;
  private final TokenService tokenService;
  private final FcmNotificationService fcmNotificationService;

  @MessageMapping(value = "/chat/enter")
  public void enter(MessageDTO messageDTO, SimpMessageHeaderAccessor accessor) {
    // 채팅방에 처음 사용자가 참가할때, SYSTEM 타입의 메세지를 전송해주는 컨트롤러
    Optional<String> tokenOptional = tokenService.resolveAlarmToken(accessor);
    // 상대방 토큰이 존재할때만 채팅 메세지 보냄
    if (tokenOptional.isPresent()) {
      sendNotification(messageDTO, tokenOptional.get());
      chatService.enter(messageDTO);
    }
  }

  @MessageMapping(value = "/chat/message")
  public void sendMessage(MessageDTO messageDTO, SimpMessageHeaderAccessor accessor) {
    // TODO 메세지 타입에 따른 처리 : 만약 이미지 타입의 메시지가 오게 된다면 그에 따른 처리를 해주어야 한다.
    Optional<MemberInfoDTO> memberInfoOptional = tokenService.resolveJwtToken(accessor);
    Optional<String> tokenOptional = tokenService.resolveAlarmToken(accessor);
    if (memberInfoOptional.isPresent() && tokenOptional.isPresent()) {
      sendNotification(messageDTO, tokenOptional.get());
      chatService.sendMessage(messageDTO, memberInfoOptional.get());
    }
  }

  private void sendNotification(MessageDTO messageDTO, String token) {
    fcmNotificationService.sendNotification(
        FcmNotificationRequest.of(messageDTO.getType().name(), messageDTO.getMessage()),
        token);
  }

}
