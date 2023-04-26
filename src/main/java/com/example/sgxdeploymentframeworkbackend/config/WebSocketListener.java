package com.example.sgxdeploymentframeworkbackend.config;

import com.example.sgxdeploymentframeworkbackend.dto.WebSocketDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketListener {

    @Autowired
    private SimpMessagingTemplate webSocket;

    public void pushSystemStatusToWebSocket (WebSocketDto webSocketDto) {
        webSocket.convertAndSend("/azure/device-code-provider", webSocketDto);
    }
}