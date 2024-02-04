package com.windmealchat.member.domain;

import com.windmealchat.member.Authority;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class MemberBase {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  private String email;

  @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
  private boolean isDeleted;
  @Enumerated(value = EnumType.STRING)
  private Authority authority;

  @LastModifiedDate //변경시간
  private LocalDateTime modifiedDate;

  public MemberBase(String email, Authority authority) {
    this.email = email;
    this.authority = authority;
  }

  public MemberBase(Long id, String email, Authority authority) {
    this.id = id;
    this.email = email;
    this.authority = authority;
  }

  public MemberBase(Long id) {
    this.id = id;
  }
}

