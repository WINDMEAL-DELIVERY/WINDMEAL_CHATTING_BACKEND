package com.windmealchat.chat.dto.request;


import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String chatRoomId;
    private MessageType type;
    private String message;

    public MessageDocument toEntity(MemberInfoDTO memberInfoDTO) {
        return new MessageDocument(this, memberInfoDTO);
    }

}
