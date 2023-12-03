package com.windmealchat.global.handler;

import static com.windmealchat.global.constants.TokenConstants.CODE;
import static com.windmealchat.global.constants.TokenConstants.CODE_A;
import static com.windmealchat.global.constants.TokenConstants.PREFIX_REFRESHTOKEN;
import static com.windmealchat.global.constants.TokenConstants.TOKEN;

import com.windmealchat.global.token.dao.RefreshTokenDAO;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.AES256Util;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@RequiredArgsConstructor
public class HttpHandShakeInterceptor implements HandshakeInterceptor {

    private final AES256Util aes256Util;
    private final TokenProvider tokenProvider;
    private final RefreshTokenDAO refreshTokenDAO;
    /*
        ServletHttpRequest에서 쿠키를 가져온 다음, attributes에 저장해준다.
        그리고 해당 값을 인바운드 채널 인터셉터에서 시큐리티의 필터처럼 검증해주면 된다.
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.error("소켓 연결 수신됨");
        if(request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
            String token = resolveToken(servletRequest, CODE);
            String alarmToken = resolveToken(servletRequest, CODE_A);
            Optional<MemberInfoDTO> memberInfoDTO = resolveMemberInfo(token);
            /*
                연결을 맺기 전에 토큰을 검증하는 단계
                토큰을 담고 있는 쿠키가 존재하는지, 존재한다면 해당 토큰이 담고 있는 아이디와 이메일로 존재하는 refreshToken이 존재하는지
                (refreshToken이 존재여부를 확인하는 이유는 그냥 아무 값이나 prefix만 일치하면 통과가 되는 현상을 막기 위해서이다.)
             */
            if(memberInfoDTO.isPresent()) {
                log.error("memberInfo 존재");
                log.error(memberInfoDTO.get().getEmail());
                String key = PREFIX_REFRESHTOKEN + memberInfoDTO.get().getId() + memberInfoDTO.get().getEmail();
                Optional<String> refreshToken = refreshTokenDAO.getRefreshToken(aes256Util.encrypt(key));
                // 이메일과 PK로 찾아온 리프레쉬 토큰이 존재함을 확인하면 연결을 맺을 수 있다.
                if(refreshToken.isPresent()) {
                    // clientInboundChannelHandler (메세지를 송신하기 전 콜백되는 핸들러)에서 사용할 수 있도록 세션에 토큰을 저장해준다.
                    attributes.put(TOKEN, token);
                    log.error("연결 성공");
                    return true;
                }
            }
        }
        // 토큰이 없거나 요청이 잘못되었다면 연결을 맺지 않는다.
        // 여기서 사용자에게 예외를 전달해줄 방법은 없고, 그냥 연결을 맺지 않는 정도가 최선인 듯 하다.
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 핸드쉐이크 이후의 콜백 메서드. 일단은 처리할 것이 없다고 판단되어 비워두겠다.
        log.info("Handshake 완료");
    }

    private String resolveToken (HttpServletRequest servletRequest, String type) throws Exception{
        String queryString = servletRequest.getParameter(type);
        return aes256Util.decrypt(queryString);
    }
    private Optional<MemberInfoDTO> resolveMemberInfo (String token) {
        Optional<MemberInfoDTO> memberInfoDTO = Optional.empty();
        if(token != null) {
            memberInfoDTO = tokenProvider.getMemberInfoFromToken(token);
        }
        return memberInfoDTO;
    }
}
