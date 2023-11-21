package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.domain.MessageType;
import com.windmealchat.chat.dto.request.MessageDTO;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SystemResponse {

    private final MessageDTO messageDTO;
    private final LocalDateTime localDateTime;

    private SystemResponse(MessageDTO messageDTO, String systemMessage) {
        this.messageDTO = new MessageDTO(messageDTO.getChatRoomId(), MessageType.SYSTEM, systemMessage);
        this.localDateTime = LocalDateTime.now();
    }

    public static SystemResponse of(MessageDTO messageDTO, String systemMessage) {
        return new SystemResponse(messageDTO, systemMessage);
    }

}
