package com.windmealchat.chat.controller;

import static com.windmealchat.global.constants.TokenConstants.AUTHORIZATION_HEADER;
import static com.windmealchat.global.constants.TokenConstants.BEARER_PREFIX;

import com.windmealchat.chat.dto.request.ChatroomLeaveRequest;
import com.windmealchat.chat.dto.response.ChatMessageResponse;
import com.windmealchat.chat.dto.response.ChatroomResponse;
import com.windmealchat.chat.service.ChatroomService;
import com.windmealchat.global.dto.ResultDataResponseDTO;
import com.windmealchat.global.exception.ExceptionResponseDTO;
import com.windmealchat.global.token.service.TokenService;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/chat")
@Tag(name = "채팅 API", description = "채팅 관련 REST API 입니다.")
public class ChatRestController {

  private final ChatroomService chatroomService;
  private final TokenService tokenService;

  @GetMapping("/{chatroomId}")
  @Operation(summary = "채팅방의 메시지 리스트 조회 요청", description = "특정 채팅방의 메시지 리스트를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "메시지 리스트 조회 성공"),
      @ApiResponse(responseCode = "400", description = "암호화 과정에서 오류가 발생하였습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "401", description = "요청에 인증 정보가 존재하지 않습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 엑세스 토큰입니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
  })
  public ResultDataResponseDTO<ChatMessageResponse> messageList(@PathVariable String chatroomId,
      Pageable pageable, HttpServletRequest request) {
    MemberInfoDTO memberInfoDTO = tokenService.resolveJwtTokenFromHeader(resolveToken(request));
    ChatMessageResponse chatMessages = chatroomService.getChatMessages(memberInfoDTO, pageable,
        chatroomId);
    return ResultDataResponseDTO.of(chatMessages);
  }

  @GetMapping("/chatroom")
  @Operation(summary = "채팅방의 리스트 조회 요청", description = "사용자가 속한 채팅방의 리스트를 조회합니다")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "채팅방 리스트 조회 성공"),
      @ApiResponse(responseCode = "400", description = "암호화 과정에서 오류가 발생하였습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "401", description = "요청에 인증 정보가 존재하지 않습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 엑세스 토큰입니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
  })
  public ResultDataResponseDTO<ChatroomResponse> chatroomList(
      Pageable pageable,
      HttpServletRequest request) {
    MemberInfoDTO memberInfoDTO = tokenService.resolveJwtTokenFromHeader(resolveToken(request));
    ChatroomResponse chatrooms = chatroomService.getChatrooms(memberInfoDTO, pageable);
    return ResultDataResponseDTO.of(chatrooms);
  }

  // TODO delete?
  @PostMapping("/chatroom")
  @Operation(summary = "채팅방 나가기 요청", description = "채팅방을 나갑니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "메시지 나가기 성공"),
      @ApiResponse(responseCode = "400", description = "암호화 과정에서 오류가 발생하였습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "401", description = "요청에 인증 정보가 존재하지 않습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 엑세스 토큰입니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
      @ApiResponse(responseCode = "600", description = "큐를 삭제하지 못했습니다.",
          content = @Content(schema = @Schema(implementation = ExceptionResponseDTO.class))),
  })
  public ResultDataResponseDTO leaveChatroom(@RequestBody ChatroomLeaveRequest chatroomLeaveRequest,
      HttpServletRequest request) {
    MemberInfoDTO memberInfoDTO = tokenService.resolveJwtTokenFromHeader(resolveToken(request));
    chatroomService.leaveChatroom(memberInfoDTO, chatroomLeaveRequest);
    return ResultDataResponseDTO.empty();
  }

  private Optional<String> resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    String result = null;
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      result = bearerToken.substring(7);
    }
    return Optional.ofNullable(result);
  }

}
