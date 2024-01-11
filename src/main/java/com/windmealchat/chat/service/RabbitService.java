package com.windmealchat.chat.service;

import com.rabbitmq.client.AMQP;
import com.windmealchat.chat.dto.request.ChatInitialRequest;
import com.windmealchat.chat.dto.response.ChatMessageResponse.ChatMessageSpecResponse;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitService {
  private final RabbitTemplate rabbitTemplate;
  private final AmqpAdmin admin;

  public void createQueue(String chatroomId, ChatInitialRequest request) {
    String queueName =
        "room." + chatroomId + "." + request.getOppositeEmail()
            .split("@")[0];
    Queue queue = new Queue(queueName, true, false, false, null);
    admin.declareQueue(queue);
  }

  public void sendMessage(String chatroomId, String email, ChatMessageSpecResponse messageSpecResponse) {
    rabbitTemplate.convertAndSend(
        "room." + chatroomId + "." + email.split("@")[0],
        messageSpecResponse);
  }

  public int getQueueMessages(String queueName) {
    AMQP.Queue.DeclareOk dok = rabbitTemplate.execute(
        channel -> channel.queueDeclare(queueName, true, false, false, new HashMap<>()));
    return dok.getMessageCount();
  }

//  public void deleteQueue(String queueName) {
//    if(!admin.deleteQueue(queueName)) {
//      throw new
//    }
//  }
}
