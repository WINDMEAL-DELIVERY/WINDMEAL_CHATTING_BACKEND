package com.windmealchat.member.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class MemberInfoDTO {
    private Long id;
    private String email;

    public static MemberInfoDTO of(Long memberId, String email) {
        return MemberInfoDTO.builder()
                .id(memberId)
                .email(email)
                .build();
    }

    public static Optional<MemberInfoDTO> ofNullable(Long memberId, String email) {
        MemberInfoDTO response;
        if(memberId == null || email == null)
            response = null;
        else {
            response = MemberInfoDTO.builder()
                    .id(memberId)
                    .email(email)
                    .build();
        }
        return Optional.ofNullable(response);
    }
}
