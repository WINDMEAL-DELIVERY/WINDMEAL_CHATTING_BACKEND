package com.windmealchat.global.token.dao;

import com.windmealchat.member.dto.response.MemberInfoDTO;

import java.util.Optional;

public interface RefreshTokenDAO {
    Optional<String> getRefreshToken(MemberInfoDTO memberInfoDTO);
}
