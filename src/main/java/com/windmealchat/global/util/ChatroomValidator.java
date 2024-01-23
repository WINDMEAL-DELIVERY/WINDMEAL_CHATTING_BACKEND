package com.windmealchat.global.util;

import com.windmealchat.chat.domain.ChatroomDocument;
import com.windmealchat.chat.exception.ChatroomNotFoundException;
import com.windmealchat.chat.exception.ExitedChatroomException;
import com.windmealchat.chat.exception.NotChatroomMemberException;
import com.windmealchat.chat.repository.ChatroomDocumentRepository;
import com.windmealchat.global.exception.ErrorCode;
import com.windmealchat.member.dto.response.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatroomValidator {

  private final ChatroomDocumentRepository chatroomDocumentRepository;

  public static boolean isOwner(MemberInfoDTO memberInfoDTO, ChatroomDocument chatroomDocument) {
    return memberInfoDTO.getId().equals(chatroomDocument.getOwnerId());
  }

  public void checkChatroom(String chatroomId, MemberInfoDTO memberInfoDTO) {
    ChatroomDocument chatroomDocument = chatroomDocumentRepository.findById(chatroomId)
        .orElseThrow(() -> new ChatroomNotFoundException(ErrorCode.NOT_FOUND));
    if (!chatroomDocument.getOwnerId().equals(memberInfoDTO.getId())
        && !chatroomDocument.getGuestId().equals(memberInfoDTO.getId())) {
      throw new NotChatroomMemberException(ErrorCode.VALIDATION_ERROR);
    }
    boolean isOwner = isOwner(memberInfoDTO, chatroomDocument);
    if ((isOwner && chatroomDocument.isDeletedByOwner())
        || (!isOwner && chatroomDocument.isDeletedByGuest())) {
      throw new ExitedChatroomException(ErrorCode.BAD_REQUEST);
    }
  }
}
