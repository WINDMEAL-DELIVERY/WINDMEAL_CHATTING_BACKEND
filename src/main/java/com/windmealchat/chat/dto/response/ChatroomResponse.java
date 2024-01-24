package com.windmealchat.chat.dto.response;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.domain.MessageDocument;
import com.windmealchat.chat.domain.MessageType;
import com.windmealchat.global.util.ChatroomValidator;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@Schema(title = "채팅방 페이지네이션 응답")
public class ChatroomResponse {

  private final Slice<ChatroomSpecResponse> chatroomSpecResponses;

  private ChatroomResponse(Slice<ChatroomSpecResponse> chatroomSpecResponses) {
    this.chatroomSpecResponses = chatroomSpecResponses;
  }

  public static ChatroomResponse of(Slice<ChatroomSpecResponse> chatroomSpecResponses) {
    return new ChatroomResponse(chatroomSpecResponses);
  }

  @Getter
  @Schema(title = "채팅방 세부 정보")
  public static class ChatroomSpecResponse implements Comparable<ChatroomSpecResponse> {

    @Schema(description = "채팅방 아이디", example = "65a8c06b58fdc8725d6b10b1")
    private String chatroomId;
    @Schema(description = "주문 아이디", example = "1")
    private Long orderId;
    @Schema(description = "마지막 메시지", example = "죄송합니다 오다가 실수로 피자를 다 먹어버렸어요;;")
    private String lastMessage;
    @Schema(description = "메시지 유형", example = "MESSAGE")
    private MessageType messageType;
    @Schema(description = "마지막 메시지 전송 시간", example = "2024-01-18T06:09:27.998")
    private LocalDateTime lastMessageTime;
    @Schema(description = "상대방의 닉네임", example = "뚜벅이")
    private String opponentNickname;
    @Schema(description = "읽지 않은 메시지 수", example = "3")
    private int uncheckedMessageCount;
    @Schema(description = "상대방의 알람 토큰(암호화 적용)", example = "p+Zvgw7s3QKfd0L5KRWcAr8lj+ojc8FCkgqz8puYgakB5p6CrfbY/okmXYimvekkDvFazLwNQCy8LVkKgR91bt0smoBNk24rJ9FTDqk7pBM//7+P2t1vrZ757oEkk/4DhbgHQ3uEUFCcoT5zgNzWk0JZFIrqtNU8ufqOUmmVirPquQo1nY7lEMGOMPNn2S0hnJeACWOWw8fVU/qFWiiglizxBE/J2B1WqAIVwU1i7/c=")
    private String opponentAlarmToken;

    private ChatroomSpecResponse(String chatroomId, Long orderId, String lastMessage, MessageType messageType,
        LocalDateTime lastMessageTime, String opponentNickname, int uncheckedMessageCount, String opponentAlarmToken) {
      this.orderId = orderId;
      this.chatroomId = chatroomId;
      this.lastMessage = lastMessage;
      this.messageType = messageType;
      this.lastMessageTime = lastMessageTime;
      this.opponentNickname = opponentNickname;
      this.opponentAlarmToken = opponentAlarmToken;
      this.uncheckedMessageCount = uncheckedMessageCount;
    }

    public static ChatroomSpecResponse of(ChatroomDocument chatroomDocument, String encrypt,
        MessageDocument messageDocument, int uncheckedMessageCount, MemberInfoDTO memberInfoDTO) {
      String lastMessage = messageDocument != null ? messageDocument.getMessage() : "";
      // 마지막으로 전송된 메시지가 없어서 전송 시간을 표시할 수 없는 경우는 채팅방이 생성된 시간을 대신 반환한다.
      LocalDateTime lastMessageTime = messageDocument != null ? messageDocument.getCreatedTime()
          : chatroomDocument.getCreatedTime();

      MessageType lastMessageType =
          messageDocument != null ? messageDocument.getMessageType() : MessageType.SYSTEM;
      boolean isOwner = ChatroomValidator.isOwner(memberInfoDTO, chatroomDocument);
      String opponentAlarmToken =
          isOwner ? chatroomDocument.getGuestAlarmToken() : chatroomDocument.getOwnerAlarmToken();
      String opponentNickname =
          isOwner ? chatroomDocument.getGuestNickname() : chatroomDocument.getOwnerNickname();

      return new ChatroomSpecResponse(encrypt, chatroomDocument.getOrderId(), lastMessage, lastMessageType,
          lastMessageTime, opponentNickname, uncheckedMessageCount, opponentAlarmToken);
    }

    @Override
    public int compareTo(ChatroomSpecResponse o) {
      return o.lastMessageTime.compareTo(this.lastMessageTime);
    }
  }
}
