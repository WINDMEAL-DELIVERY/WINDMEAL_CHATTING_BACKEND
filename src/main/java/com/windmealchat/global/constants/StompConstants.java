package com.windmealchat.global.constants;

public class StompConstants {

    public static final String QUEUE = "/queue";
    public static final String TOPIC = "/topic";
    // 모든 구독 상황에서 사용되는 prefix이다.
    public static final String SUB_PREFIX = "/sub";
    public static final String PUB_PREFIX = "/pub";
    public static final String APP = "/app";

    // 채팅방 구독 경로이다.
    public static final String SUB_CHAT_ROOM = "/chat/room/";

    // 알람 구독 경로이다.
    public static final String SUB_ALARM = "/alarm";
    public static final String SYSTEM_GREETING = "환영합니다! 채팅 가이드라인을 준수해주세요.";
}
