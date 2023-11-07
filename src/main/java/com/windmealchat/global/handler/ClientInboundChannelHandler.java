package com.windmealchat.global.handler;

import com.windmealchat.global.token.dao.RefreshTokenDAO;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static com.windmealchat.global.constants.TokenConstants.TOKEN;

@Component
@RequiredArgsConstructor
public class ClientInboundChannelHandler implements ChannelInterceptor {
    private final TokenProvider tokenProvider;
    private final RefreshTokenDAO refreshTokenDAO;
    /*
     * 웹소켓 연결을 맺은 클라이언트가 메세지를 보내기 전에, 권한이 있는지 체크하는 과정이다.
     * 아래 코드를 보면 accessor.getSessionAttributes(); 부분이 나오는데, 이 부분은 Map 형식의 세션으로부터 토큰을 얻어오는 부분이다.
     * 그리고 이 토큰은 HttpHandshakeInterceptor에서 핸드쉐이크 과정 중간에 가져온 쿠키의 토큰값이다.
     * 공식문서에 따르면, 해당 메서드가 null을 반환할 시 실제 메세지 전송은 일어나지 않는다고 한다.
     * 따라서 인증 정보가 유효하지 않다면 null을 반환하도록 하자.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // TODO 반환값이 null일때 사료용자에게 재로그인을 요청하거나 reissue를 요청해야 한다.
        Optional<MemberInfoDTO> memberInfoDTO = TokenProcessing(message);


    }

    private Optional<MemberInfoDTO> TokenProcessing(Message<?> message) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 이 세션은 HandshakeInterceptor 혹은 이를 상속받은 클래스에서 핸드쉐이크 과정 중간에 난입하여 얻은 토큰 정보 등을 저장해둔 곳이다.
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            String accessToken = (String) sessionAttributes.get(TOKEN);
            /*
                아래 코드에서 accessToken을 검증하고, 유효하다면 정보를 넣어서 반환해준다.
                토큰 만료, 잘못된 토큰 등 유효하지 않은 토큰이라고 판단되면 null이 반환됨.
                같은 과정을 핸드쉐이크 인터셉터에서도 수행해주는데, 그 결과인 memberInfo를 세션에 저장하지 않은 이유는
                토큰 검증 과정에서 만료된 토큰을 찾기 위함이다.
             */
            Optional<MemberInfoDTO> memberInfoDTOOptional = tokenProvider.getMemberInfoFromToken(accessToken);
            return memberInfoDTOOptional;
        }
    }
}
