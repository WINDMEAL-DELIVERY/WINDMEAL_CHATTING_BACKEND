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

  private final FirebaseMessaging firebaseMessaging;

  public void sendNotification(FcmNotificationRequest request, String token) {

    try {
      Notification notification = Notification.builder()
          .setTitle(request.getTitle())
          .setBody(request.getBody())
          .build();

      Message message = Message.builder()
          .setToken(token)
          .setNotification(notification)
          .build();

      firebaseMessaging.send(message);
      log.info("알람 전송 성공 : " + request.getBody());
    } catch (Exception e) {
      log.error("알람 전송 실패 : ");
      e.printStackTrace();
    }
  }


}
