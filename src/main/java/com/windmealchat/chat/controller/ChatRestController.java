package com.windmealchat.chat.controller;

import static com.windmealchat.global.constants.TokenConstants.AUTHORIZATION_HEADER;
import static com.windmealchat.global.constants.TokenConstants.BEARER_PREFIX;

import com.windmealchat.chat.dto.response.ChatMessageResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse;
import com.windmealchat.chat.service.ChatroomService;
import com.windmealchat.global.dto.ResultDataResponseDTO;
import com.windmealchat.global.token.service.TokenService;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/chat")
public class ChatRestController {

  private final ChatroomService chatroomService;
  private final TokenService tokenService;

  @GetMapping("/{chatroomId}")
  public ResultDataResponseDTO<ChatMessageResponse> messageList(@PathVariable String chatroomId,
      Pageable pageable, HttpServletRequest request) {
    MemberInfoDTO memberInfoDTO = tokenService.resolveJwtTokenFromHeader(resolveToken(request));
    ChatMessageResponse chatMessages = chatroomService.getChatMessages(memberInfoDTO, pageable,
        chatroomId);
    return ResultDataResponseDTO.of(chatMessages);
  }

  @GetMapping("/chatroom")
  public ResultDataResponseDTO<ChatroomResponse> chatroomList(Pageable pageable, HttpServletRequest request) {
    MemberInfoDTO memberInfoDTO = tokenService.resolveJwtTokenFromHeader(resolveToken(request));
    ChatroomResponse chatrooms = chatroomService.getChatrooms(memberInfoDTO, pageable);
    return ResultDataResponseDTO.of(chatrooms);
  }

  private Optional<String> resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    String result = null;
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      result =  bearerToken.substring(7);
    }
    return Optional.ofNullable(result);
  }

}
