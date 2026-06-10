package com.lgcns.pipeline.websocket;

public record ChatMessage(
        Integer id,
        String from,
        String text,
        String type
) {
}
