package com.windmealchat.global.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windmealchat.chat.exception.AuthorizationException;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.global.exception.ExceptionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebFluxExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("핸드쉐이크 에러 발생" + ex.getMessage());
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ErrorCode errorCode = ex.getClass() == AuthorizationException.class ? ErrorCode.UNAUTHORIZED : ErrorCode.BAD_REQUEST;
        response.setStatusCode(errorCode.getHttpStatus());
        ExceptionResponseDTO exceptionResponseDTO = ExceptionResponseDTO.of(errorCode, ex.getMessage());
        try {
            String responseBody = objectMapper.writeValueAsString(exceptionResponseDTO);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBody.getBytes())));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
