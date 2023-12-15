package com.windmealchat.global.auth;

import java.security.Principal;
import javax.security.auth.Subject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleUserPrincipal implements Principal {

  private Long id;
  private String email;
  private String username;

  public SimpleUserPrincipal(Long id, String email, String username) {
    this.id = id;
    this.email = email;
    this.username = username;
  }
  @Override
  public String getName() {
    return username;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }
}
