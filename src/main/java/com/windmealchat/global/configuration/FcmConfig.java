package com.windmealchat.global.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FcmConfig {

  @Value("${fcm.admin.resource.path}")
  private String path;

  @Bean
  FirebaseMessaging firebaseMessaging() throws IOException {
    // 절대경롤 작성할 경우 인스턴스가 위치를 찾을 수 없기 때문에, ClassPathResource를 활용한다.
    ClassPathResource resourceClassPath = new ClassPathResource(path);

    // 받아온 resource를 inputStream으로 변경해준다.
    InputStream resource = resourceClassPath.getInputStream();

    FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(resource))
        .build();

    FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}
