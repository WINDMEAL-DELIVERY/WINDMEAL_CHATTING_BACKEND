package com.windmealchat.member.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Schema(title = "사용자 정보 응답")
public class MemberInfoDTO {

    @Schema(description = "사용자 아이디", example = "1")
    private Long id;
    @Schema(description = "사용자 이메일", example = "windmealDelivery@gachon.ac.kr")
    private String email;
    @Schema(description = "사용자 닉네임", example = "배달학과24학번최배달")
    private String nickname;

    public static MemberInfoDTO of(Long memberId, String email, String nickname) {
        return MemberInfoDTO.builder()
                .id(memberId)
                .email(email)
                .nickname(nickname)
                .build();
    }

    /*
        dto 레벨에서 필드에 할당된 null 값에 대처하기 위해 만들어진 메서드이다.
        필드 값 중 하나라도 null이라면 dto를 null로 할당한다.
        null로 할당될 경우 tcp 핸드쉐이크 과정에서 이를 감지하고 연결을 맺지 않게 된다.
     */
    public static Optional<MemberInfoDTO> ofNullable(Long memberId, String email, String nickname) {
        MemberInfoDTO response;
        if(memberId == null || email == null || nickname == null)
            response = null;
        else {
            response = MemberInfoDTO.builder()
                    .id(memberId)
                    .email(email)
                    .nickname(nickname)
                    .build();
        }
        return Optional.ofNullable(response);
    }
}
