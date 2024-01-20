package com.windmealchat.global.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${domain.url.local}")
  private String local;
  @Value("${domain.url.chat_host}")
  private String chat_host;
  @Value("${domain.url.chat1}")
  private String chat1;
  @Value("${domain.url.chat2}")
  private String chat2;
  @Value("${domain.url.ws_host}")
  private String ws_host;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        // 요청을 허용할 출처
        .allowedOrigins(local, chat_host, chat1, chat2, ws_host)
        .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowCredentials(true)
        .exposedHeaders("Authorization")
        .allowedHeaders("*")
        .maxAge(3000);
  }
}
