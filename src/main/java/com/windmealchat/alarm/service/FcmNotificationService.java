package com.windmealchat.alarm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.windmealchat.alarm.dto.FcmNotificationRequest;
import com.windmealchat.global.util.AES256Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationService {

  private final AES256Util aes256Util;
  private final FirebaseMessaging firebaseMessaging;

  public void sendNotification(FcmNotificationRequest request, String encryptedToken) {

    try {
      String token = aes256Util.decrypt(encryptedToken);
      Notification notification = Notification.builder()
          .setTitle(request.getTitle())
          .setBody(request.getBody())
          .build();

      Message message = Message.builder()
          .setToken(token)
          .setNotification(notification)
          .build();

      log.info("알람을 전송합니다 : ");
      firebaseMessaging.send(message);
      log.info("알람 전송 완료 : " + request.getBody());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
