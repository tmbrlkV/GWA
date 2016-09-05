package com.webgateway.config.socket.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service("sessionHandler")
public class SessionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);
    private final Map<WebSocketSession, Instant> sessionMap = new ConcurrentHashMap<>();

    public SessionHandler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> sessionMap.keySet().forEach(key -> {
            try {
                Instant instant = sessionMap.get(key);
                if (Duration.between(instant, Instant.now()).getSeconds() >= 15) {
                    sessionMap.remove(key);
                    LOGGER.debug("Key {}", key.getPrincipal());
                    key.close();
                }
            } catch (IOException e) {
                LOGGER.error("Error while closing websocket session: {}", e);
            }
        }), 10, 15, TimeUnit.SECONDS);
    }

    void register(WebSocketSession session) {
        sessionMap.put(session, Instant.now());
    }
}
