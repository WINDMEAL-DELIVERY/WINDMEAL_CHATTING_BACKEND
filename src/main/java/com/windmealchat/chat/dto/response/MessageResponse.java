package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.dto.request.MessageDTO;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 웹소켓 커넥션이 맺어진 상태에서, 상대에게 채팅을 보낼때 이렇게 보내준다.
 *
 */
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
