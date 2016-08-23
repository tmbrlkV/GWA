package com.webroom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer implements WebSocketConfigurer {
    private final SessionHandler sessionHandler;

    @Autowired
    public WebSocketConfig(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/hello").withSockJS();
    }

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @EventListener
    public void onSocketConnected(SessionConnectedEvent event) {
        WebSocketSession session = (WebSocketSession) event.getSource();
        logger.debug("Session {}", session);
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        System.out.println("[Connected] " + sha.getSessionId());
    }

    @EventListener
    public void onSocketDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        System.out.println("[Disconnected] " + sha.getSessionId());
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sessionHandler, "/hello").withSockJS();
    }
}
