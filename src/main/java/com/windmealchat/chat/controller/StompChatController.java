package com.windmealchat.chat.controller;

import static com.windmealchat.global.constants.StompConstants.SUB_CHAT_ROOM;
import static com.windmealchat.global.constants.StompConstants.SUB_PREFIX;
import static com.windmealchat.global.constants.StompConstants.SYSTEM_GREETING;

import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.MessageResponse;
import com.windmealchat.chat.dto.response.SystemResponse;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.HeaderAccessorUtil;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

    private final SimpMessageSendingOperations messageTemplate;
    private final TokenProvider tokenProvider;

    @MessageMapping(value = "/chat/enter")
    public void enter(MessageDTO messageDTO) {
        // 채팅방에 처음 사용자가 참가할때, SYSTEM 타입의 메세지를 전송해주는 컨트롤러
        SystemResponse systemResponse = SystemResponse.of(messageDTO, SYSTEM_GREETING);
        messageTemplate.convertAndSend(SUB_PREFIX + SUB_CHAT_ROOM + messageDTO.getChatRoomId(), systemResponse);
    }

    @MessageMapping(value = "/chat/message")
    public void sendMessage(MessageDTO messageDTO, SimpMessageHeaderAccessor accessor) {
        // 스프링에서 제공하는 추상화된 메세지 큐이다. 이후에 메세지 브로커를 확정하고 이 부분을 완성해주어야 한다.
        // 메세지가 들어왔으면 채팅방의 다른 사용자들에게 전달해야 하므로, 채팅방 ID를 토픽으로 메세지를 전송한다.
        // TODO 메세지 타입에 따른 처리 : 만약 이미지 타입의 메시지가 오게 된다면 그에 따른 처리를 해주어야 한다.
        MemberInfoDTO memberInfoDTO = HeaderAccessorUtil.convertToMemberInfo(accessor, tokenProvider);
        MessageResponse messageResponse = MessageResponse.of(messageDTO, memberInfoDTO);
        messageTemplate.convertAndSend(SUB_PREFIX + SUB_CHAT_ROOM + messageDTO.getChatRoomId(), messageResponse);
    }

}
