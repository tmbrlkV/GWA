package com.webgateway.config.socket.zmq;

import com.chat.util.entity.User;
import com.chat.util.json.JsonProtocol;
import com.webgateway.entity.CustomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

@Component("roomManagerSocketConfig")
public final class RoomManagerSocketConfig extends SocketConfig<String> {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSocketConfig.class);
    private final ZMQ.Socket sender;
    private final ZMQ.Socket receiver;

    private RoomManagerSocketConfig() {
        super();
        receiver = getReceiver();
        sender = getSender();
        receiver.subscribe("roomManager".getBytes());
    }


    @Override
    public void send(String toRoom) {
        User user = ((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toUser();
        JsonProtocol<User> protocol = new JsonProtocol<>("addUserToRoom", user);
        protocol.setFrom(String.valueOf(user.getId()));
        protocol.setTo(toRoom);
        sender.send(protocol.toString());
    }

    @Override
    public String receive() {
        receiver.recvStr();
        return receiver.recvStr();
    }
}
