package com.windmealchat.chat.controller;

import com.windmealchat.chat.dto.request.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

    @MessageMapping(value = "/chat/wait")
    public void enter(MessageDTO messageDTO) {
        log.error(messageDTO.getMessage());
    }
}
