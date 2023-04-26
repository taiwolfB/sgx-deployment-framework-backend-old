package com.example.sgxdeploymentframeworkbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/azure");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("device-code-provider")
                .setAllowedOrigins("http://localhost:8083")
                .withSockJS();
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(10)
                .maxPoolSize(20);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setSendTimeLimit( 15 * 1000)
                .setSendBufferSizeLimit( 512 * 1024)
                .setMessageSizeLimit( 128 * 1024);
    }
}
