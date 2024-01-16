package com.windmealchat.chat.controller;

import static com.windmealchat.global.constants.RabbitConstants.CHAT_QUEUE_NAME;

import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import com.windmealchat.chat.service.StompChatService;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.GeneralException;
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

  private final StompChatService stompChatService;
  private final TokenService tokenService;

  @MessageMapping(value = "chat.message.{chatRoomId}")
  public void sendMessage(@DestinationVariable String chatRoomId, MessageDTO messageDTO,
      StompHeaderAccessor accessor) {
    Optional<MemberInfoDTO> memberInfoOptional = tokenService.resolveJwtToken(accessor);
    memberInfoOptional.ifPresent(
        memberInfoDTO -> stompChatService.sendMessage(chatRoomId, messageDTO, memberInfoDTO));
  }

  @RabbitListener(queues = CHAT_QUEUE_NAME)
  public void receive(ChatMessageSpecResponse chatMessageSpecResponse) {
    System.out.println("received : " + chatMessageSpecResponse.getMessage());
  }

}
