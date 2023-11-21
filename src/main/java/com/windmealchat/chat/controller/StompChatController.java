package com.windmealchat.chat.controller;

import com.windmealchat.chat.dto.request.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

    private final SimpMessageSendingOperations messageTemplate;

    @MessageMapping(value = "/chat/message")
    public void enter(MessageDTO messageDTO) {
        log.error(messageDTO.getChatRoomId() + " 번 채팅방의 메세지 : " + messageDTO.getMessage());
        // 스프링에서 제공하는 추상화된 메세지 큐이다. 이후에 메세지 브로커를 확정하고 이 부분을 완성해주어야 한다.
        // 메세지가 들어왔으면 채팅방의 다른 사용자들에게 전달해야 하므로, 채팅방 ID를 토픽으로 메세지를 전송한다.
        log.info("/sub/chat/room/" + messageDTO.getChatRoomId());
        messageTemplate.convertAndSend("/sub/chat/room/" + messageDTO.getChatRoomId(), messageDTO);
    }
}
