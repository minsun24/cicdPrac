package com.lgcns.pipeline.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketBroadcastController {
    private static int msgId = 0;

    // 메시지 전달
    @MessageMapping("/broadcast")   // message는 /app/broadcast로 보냄
    @SendTo("/topic/broadcast")
    public ChatMessage send(ChatMessage msg) throws Exception {
        System.out.println("msg 확인: " + msg);
        return new ChatMessage(++msgId, msg.from(), msg.text(), msg.type());
    }

    // 방 참가 or 퇴장
    @MessageMapping("/join-leave")
    @SendTo("/topic/join-leave")
    public ChatMessage joinLeave(ChatMessage msg) throws Exception {
        System.out.println("msg 확인: " + msg);
        return new ChatMessage(++msgId, "system", msg.from()
                + "'s " + msg.text(), msg.type());
    }
}
