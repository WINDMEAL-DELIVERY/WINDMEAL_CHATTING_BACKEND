package com.windmealchat.global.token.service;

import java.util.Optional;

@FunctionalInterface
public interface TokenResolver<T> {
  Optional<T> resolve(String token) throws Exception;
}
