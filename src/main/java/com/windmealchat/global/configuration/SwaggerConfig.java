package com.windmealchat.global.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openAPI() {
    Info info = new Info()
        .title("WINDMEAL CHATTING API")
        .version("1.0")
        .description("바람개비 딜리버리 채팅서버 API 입니다.")
        .termsOfService("http://swagger.io/terms/")
        .license(new License()
            .name("Apache License Version 2.0")
            .url("http://www.apache.org/licenses/LICENSE-2.0")
        );

    return new OpenAPI()
        .components(new Components())
        .info(info);
  }
}
