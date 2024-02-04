package com.windmealchat.member.domain;

import com.windmealchat.member.Authority;
import javax.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends MemberBase{


  private String token;
  private String name;
  private String nickname;
  private String department;
  private String profileImage;

  @Builder
  public Member(Long id, String email, Authority authority, String name, String nickname, String department, String profileImage) {
    super(id, email, authority);
    this.name = name;
    this.nickname = nickname;
    this.department = department;
    this.profileImage = profileImage;
  }

  @Builder
  public Member(Long id) {
    super(id);
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void updateToken(String token) {
    this.token = token;
  }

  public void updateProfileImage(String profileImage) {
    this.profileImage = profileImage;
  }
}
