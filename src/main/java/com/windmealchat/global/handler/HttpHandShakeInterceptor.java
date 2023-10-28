package com.windmealchat.global.handler;

import com.windmealchat.global.util.CookieUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

import static com.windmealchat.global.constants.TokenConstants.TOKEN;

/*
    웹소켓 연겯로 결국엔 HTTP의 확장, 즉 3-way handshake의 과정에 upgrade 헤더가 추가된 것이다.
    우리는 HTTP 핸드쉐이크의 과정 중간에서 쿠키에 접근하여 토큰을 가져와야 한다.
    그렇기에 HandShakeInterceptor를 구현한다. 이 과정에서
 */
public class HttpHandShakeInterceptor implements HandshakeInterceptor {

    /*
        ServletHttpRequest에서 쿠키를 가져온 다음, attributes에 저장해준다.
        그리고 해당 값을 인바운드 채널 인터셉터에서 시큐리티의 필터처럼 검증해주면 된다.
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if(request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
            Cookie cookie = CookieUtil.getCookie(servletRequest, TOKEN).orElse(null);
            attributes.put(TOKEN, cookie.getValue());
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 핸드쉐이크 이후의 콜백 메서드. 일단은 처리할 것이 없다고 판단되어 비워두겠다.
    }
}
