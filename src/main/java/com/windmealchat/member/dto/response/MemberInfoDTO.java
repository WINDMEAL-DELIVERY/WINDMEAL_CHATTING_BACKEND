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

    /*
        dto 레벨에서 필드에 할당된 null 값에 대처하기 위해 만들어진 메서드이다.
        필드 값 중 하나라도 null이라면 dto를 null로 할당한다.
        null로 할당될 경우 tcp 핸드쉐이크 과정에서 이를 감지하고 연결을 맺지 않게 된다.
     */
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
