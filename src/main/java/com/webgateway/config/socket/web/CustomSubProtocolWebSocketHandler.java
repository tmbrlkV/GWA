package com.webgateway.config.socket.web;

import com.chat.util.entity.User;
import com.webgateway.config.socket.zmq.RoomManagerSocketConfig;
import com.webgateway.entity.CustomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

public class CustomSubProtocolWebSocketHandler extends SubProtocolWebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomSubProtocolWebSocketHandler.class);
    private SessionHandler sessionHandler;
    private RoomManagerSocketConfig roomManagerSocket;

    @Autowired
    public void setRoomManagerSocket(RoomManagerSocketConfig roomManagerSocket) {
        this.roomManagerSocket = roomManagerSocket;
    }

    @Autowired
    public void setSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    @Autowired
    public CustomSubProtocolWebSocketHandler(MessageChannel clientInboundChannel, SubscribableChannel clientOutboundChannel) {
        super(clientInboundChannel, clientOutboundChannel);
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LOGGER.info("New websocket connection was established");
        sessionHandler.register(session);
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) session.getPrincipal();
        User user = ((CustomUser) token.getPrincipal()).toUser();
        roomManagerSocket.addClientToLobby(user);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
        sessionHandler.register(session);
        LOGGER.debug("Session {}, message {}", session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) session.getPrincipal();
        User user = ((CustomUser) token.getPrincipal()).toUser();
        roomManagerSocket.deleteUserFromAllRooms(user);
    }
}
