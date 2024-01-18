package com.windmealchat.chat.domain;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "채팅 메시지 타입", enumAsRef = true)
public enum MessageType {

    TEXT, IMAGE, EMOJI, SYSTEM, ERROR
}
