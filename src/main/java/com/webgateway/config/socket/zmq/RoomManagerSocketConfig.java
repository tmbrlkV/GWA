package com.webgateway.config.socket.zmq;

import com.chat.util.entity.User;
import com.chat.util.json.JsonProtocol;
import com.webgateway.entity.CustomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

@Component("roomManagerSocketConfig")
public final class RoomManagerSocketConfig extends SocketConfig<String> {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSocketConfig.class);
    private final ZMQ.Socket sender;
    private final ZMQ.Socket receiver;
    private String command;

    private RoomManagerSocketConfig() {
        super();
        receiver = getReceiver();
        sender = getSender();
        command = getCommand();
        receiver.subscribe("roomManager".getBytes());
    }

    public void setCommand(String command) {
        this.command = command;
        logger.debug("Command Set {}", command);
    }

    @Override
    public void send(String toRoom) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Principal {}", authentication.getAuthorities());
        User user = ((CustomUser) authentication.getPrincipal()).toUser();
        logger.debug("Command Send {}", command);
        JsonProtocol<User> protocol = new JsonProtocol<>(command, user);
        protocol.setFrom(String.valueOf(user.getId()));
        protocol.setTo("roomManager:" + toRoom);
        sender.send(protocol.toString());
    }

    @Override
    public String receive() {
        receiver.recvStr();
        return receiver.recvStr();
    }
}
