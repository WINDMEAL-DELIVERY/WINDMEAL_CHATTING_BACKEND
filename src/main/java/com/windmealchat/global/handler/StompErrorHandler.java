package com.windmealchat.global.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.Assert;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Slf4j
@RequiredArgsConstructor
public class StompErrorHandler extends StompSubProtocolErrorHandler {

  /*
      클라이언트 메세지 처리 도중 발생한 오류를 처리한다.
      STOMP 프로토콜에 따르면, 서버는 이 경우 ERROR 프레임을 헤더에 달아서 전달 후 연결을 닫는데,
      이 프레임을 전달하고 싶지 않으면 null을 반환하도록 설계하면 된다고 한다.
      그 후 메세지 브로커를 통해서 사용자의 입맛대로 처리할 수 있다고 한다.
   */
  @Override
  @Nullable
  public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage,
      Throwable ex) {
    log.error(ex.getMessage());
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
    accessor.setMessage(ex.getMessage());
    accessor.setLeaveMutable(true);
    StompHeaderAccessor clientHeaderAccessor = null;
    if (clientMessage != null) {
      clientHeaderAccessor = MessageHeaderAccessor.getAccessor(clientMessage,
          StompHeaderAccessor.class);
      if (clientHeaderAccessor != null) {
        String receiptId = clientHeaderAccessor.getReceipt();
        if (receiptId != null) {
          accessor.setReceiptId(receiptId);
        }
      }
    }
    byte[] payload = ex.getMessage().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    return handleInternal(accessor, payload, ex, clientHeaderAccessor);
  }

  /*
      연결이 끊겼거나, 외부 메세지 브로커가 에러를 반환했거나, 기타 등등의 문제로 인해서
      서버가 클라이언트에게 에러를 전달해야 할 경우 아래의 메서드가 호출된다.
   */
  @Override
  @Nullable
  public Message<byte[]> handleErrorMessageToClient(Message<byte[]> errorMessage) {

    log.error("예외발생 : handleErrorMessageToClient");
    log.error("페이로드 : " + new String((byte[])errorMessage.getPayload()));

    // TODO STOMP Method를 ERROR로 만들어서 보내주는게 가능한가?
//    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(errorMessage,
//        StompHeaderAccessor.class);
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(errorMessage);
    Assert.notNull(accessor, "No StompHeaderAccessor");
    if (!accessor.isMutable()) {
      accessor = StompHeaderAccessor.wrap(errorMessage);
    }
    return handleInternal(accessor, errorMessage.getPayload(), null, null);
  }

  protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor,
      byte[] errorPayload,
      @Nullable Throwable cause, @Nullable StompHeaderAccessor clientHeaderAccessor) {

    return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());
  }
}
