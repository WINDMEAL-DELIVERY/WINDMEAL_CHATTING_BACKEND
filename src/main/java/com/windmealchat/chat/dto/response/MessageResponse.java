package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class MessageResponse {
    private final MessageDTO messageDTO;
    private final MemberInfoDTO memberInfoDTO;
    private final LocalDateTime localDateTime;

    private MessageResponse (MessageDTO messageDTO, MemberInfoDTO memberInfoDTO) {
        this.messageDTO = messageDTO;
        this.memberInfoDTO = memberInfoDTO;
        this.localDateTime = LocalDateTime.now();
    }

    public static MessageResponse of(MessageDTO messageDTO, MemberInfoDTO memberInfoDTO) {
        return new MessageResponse(messageDTO, memberInfoDTO);
    }
}
