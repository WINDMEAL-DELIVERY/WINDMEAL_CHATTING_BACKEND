package com.windmealchat.chat.dto.request;


import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "채팅 메시지")
public class MessageDTO {

    @Schema(description = "상대방의 이메일 (큐를 생성하기 위함)", example = "windmealDelivery@gachon.ac.kr")
    private String oppositeEmail;
    @Schema(description = "채팅방 아이디", example = "65a8c06b58fdc8725d6b10b1")
    private String chatRoomId;
    @Schema(description = "메시지의 유형", example = "TEXT")
    private MessageType type;
    @Schema(description = "메시지 본문", example = "죄송해요 오다가 실수로 피자를 다 먹어버렸네요;")
    private String message;

    public MessageDocument toDocument(MemberInfoDTO memberInfoDTO) {
        return new MessageDocument(this, memberInfoDTO);

    }

    public MessageDocument toDocument(String message) {
        return MessageDocument.builder()
            .chatroomId(this.chatRoomId)
            .messageType(this.type)
            .message(message)
            .build();
    }


}
