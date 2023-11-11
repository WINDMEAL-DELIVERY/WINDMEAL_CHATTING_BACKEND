package com.windmealchat.global.handler;

import com.windmealchat.global.token.dao.RefreshTokenDAO;
import com.windmealchat.global.token.impl.TokenProvider;
import com.windmealchat.global.util.CookieUtil;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

import static com.windmealchat.global.constants.TokenConstants.TOKEN;

/*
    웹소켓 연결도 결국엔 HTTP의 확장, 즉 3-way handshake의 과정에 upgrade 헤더가 추가된 것이다.
    우리는 HTTP 핸드쉐이크의 과정 중간에서 쿠키에 접근하여 토큰을 가져와야 한다.
    그렇기에 HandShakeInterceptor를 구현한다. 이 과정에서
 */
@Slf4j
@RequiredArgsConstructor
public class HttpHandShakeInterceptor implements HandshakeInterceptor {

    private final TokenProvider tokenProvider;
    private final RefreshTokenDAO refreshTokenDAO;
    /*
        ServletHttpRequest에서 쿠키를 가져온 다음, attributes에 저장해준다.
        그리고 해당 값을 인바운드 채널 인터셉터에서 시큐리티의 필터처럼 검증해주면 된다.
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.error("handshakeInterceptor");
        if(request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
            String token = resolveToken(servletRequest);
            Optional<MemberInfoDTO> memberInfoDTO = resolveMemberInfo(token);
            /*
                연결을 맺기 전에 토큰을 검증하는 단계
                토큰을 담고 있는 쿠키가 존재하는지, 존재한다면 해당 토큰이 담고 있는 아이디와 이메일로 존재하는 refreshToken이 존재하는지
                (refreshToken이 존재여부를 확인하는 이유는 그냥 아무 값이나 prefix만 일치하면 통과가 되는 현상을 막기 위해서이다.)
             */
            if(memberInfoDTO.isPresent()) {
                Optional<String> refreshToken = refreshTokenDAO.getRefreshToken(memberInfoDTO.get());
                // 이메일과 PK로 찾아온 리프레쉬 토큰이 존재함을 확인하면 연결을 맺을 수 있다.
                if(refreshToken.isPresent()) {
                    // clientInboundChannelHandler (메세지를 송신하기 전 콜백되는 핸들러)에서 사용할 수 있도록 세션에 토큰을 저장해준다.
                    attributes.put(TOKEN, token);
                    return true;
                }
            }
        }
        // 토큰이 없거나 요청이 잘못되었다면 연결을 맺지 않는다.
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 핸드쉐이크 이후의 콜백 메서드. 일단은 처리할 것이 없다고 판단되어 비워두겠다.
        log.info("Handshake 완료");
    }

    private String resolveToken (HttpServletRequest servletRequest) {
        Cookie cookie = CookieUtil.getCookie(servletRequest, TOKEN).orElse(null);
        if(cookie != null)
            return cookie.getValue();
        return null;
    }
    private Optional<MemberInfoDTO> resolveMemberInfo (String token) {
        Optional<MemberInfoDTO> memberInfoDTO = Optional.empty();
        if(token != null) {
            memberInfoDTO = tokenProvider.getMemberInfoFromToken(token);
        }
        return memberInfoDTO;
    }
}
