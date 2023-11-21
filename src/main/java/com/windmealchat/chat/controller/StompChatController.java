package com.windmealchat.chat.controller;

import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.chat.dto.response.MessageResponse;
import com.windmealchat.chat.dto.response.SystemResponse;
import com.windmealchat.chat.exception.AuthorizationException;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.Map;

import static com.windmealchat.global.constants.StompConstants.SUB;
import static com.windmealchat.global.constants.StompConstants.SYSTEM_GREETING;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

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
        messageTemplate.convertAndSend(SUB+messageDTO.getChatRoomId(), systemResponse);
    }

    @MessageMapping(value = "/chat/message")
    public void sendMessage(MessageDTO messageDTO, SimpMessageHeaderAccessor accessor) {
        // 스프링에서 제공하는 추상화된 메세지 큐이다. 이후에 메세지 브로커를 확정하고 이 부분을 완성해주어야 한다.
        // 메세지가 들어왔으면 채팅방의 다른 사용자들에게 전달해야 하므로, 채팅방 ID를 토픽으로 메세지를 전송한다.
        MessageResponse messageResponse = convertToMessageResponse(messageDTO, accessor);
        messageTemplate.convertAndSend(SUB + messageDTO.getChatRoomId(), messageResponse);
    }

    private MessageResponse convertToMessageResponse(MessageDTO messageDTO, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> session = accessor.getSessionAttributes();
        String accessToken = (String) session.get(TOKEN);
        MemberInfoDTO memberInfoDTO = tokenProvider.getMemberInfoFromToken(accessToken).orElseThrow(
                () -> new AuthorizationException(ErrorCode.UNAUTHORIZED, "사용자의 인증 정보가 유효하지 않습니다."));
        return MessageResponse.of(messageDTO, memberInfoDTO);
    }
}
