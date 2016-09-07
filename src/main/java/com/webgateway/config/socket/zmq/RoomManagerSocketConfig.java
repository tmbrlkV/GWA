package com.webgateway.config.socket.zmq;

import com.chat.util.entity.User;
import com.chat.util.json.JsonObjectFactory;
import com.chat.util.json.JsonProtocol;
import com.webgateway.entity.CustomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

import java.util.Optional;

@Component("roomManagerSocketConfig")
public final class RoomManagerSocketConfig extends SocketConfig<String> {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSocketConfig.class);
    private static final String BAD_REPLY = new JsonProtocol<>("", new Long[0]).toString();
    private final ZMQ.Socket sender;
    private final ZMQ.Socket receiver;
    private String command;
    private ZMQ.Poller poller;

    private RoomManagerSocketConfig() {
        super();
        receiver = getReceiver();
        sender = getSender();
        command = getCommand();
        receiver.subscribe("roomManager".getBytes());
        poller = new ZMQ.Poller(0);
        poller.register(receiver, ZMQ.Poller.POLLIN);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public void send(String toRoom) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUser) authentication.getPrincipal()).toUser();
        JsonProtocol<User> protocol = new JsonProtocol<>(command, user);
        protocol.setFrom(String.valueOf(user.getId()));
        protocol.setTo("roomManager:" + toRoom);
        sender.send(protocol.toString());
    }

    @Override
    public String receive() {
        logger.debug("RECEIVING STARTED");
        int poll = poller.poll(100);
        logger.debug("Poller count: {}", poll);
        if (poll == 0) {
            logger.debug("RECEIVING FINISHED empty");
            return BAD_REPLY;
        }
        logger.debug("KEK " + receiver.recvStr());
        logger.debug("RECEIVING FINISHED from butler");
        return receiver.recvStr();
    }

    public void deleteUserFromAllRooms(User user) {
        communicate("removeUserFromAllRooms", user);
    }

    public void addClientToLobby(User user) {
        communicate("addUserToRoom", user);
    }

    private String communicate(String command, User user) {
        JsonProtocol<User> protocol = new JsonProtocol<>(command, user);
        protocol.setFrom(String.valueOf(user.getId()));
        protocol.setTo("roomManager:15000");
        sender.send(protocol.toString());
        return receive();
    }

    public RoomManagerReply getInfo(User user) {
        String getAllRooms = communicate("getAllRooms", user);
        String getAllUsers = communicate("getAllUsers", user);
        JsonProtocol<Long[]> rooms = Optional.ofNullable(JsonObjectFactory
                .getObjectFromJson(getAllRooms, JsonProtocol.class)).orElseGet(JsonProtocol::new);
        JsonProtocol<Long[]> users = Optional.ofNullable(JsonObjectFactory
                .getObjectFromJson(getAllUsers, JsonProtocol.class)).orElseGet(JsonProtocol::new);
        return new RoomManagerReply(rooms.getAttachment(), users.getAttachment());
    }

    public Long[] getAllUsersInRoom(User user, Long idRoom) {
        String usersInRoom = communicate("getAllUsersInRoom", user, idRoom);
        JsonProtocol<Long[]> protocol = Optional.ofNullable(JsonObjectFactory
                .getObjectFromJson(usersInRoom, JsonProtocol.class)).orElseGet(JsonProtocol::new);
        return protocol.getAttachment();
    }

    private String communicate(String command, User user, Long idRoom) {
        JsonProtocol<User> protocol = new JsonProtocol<>(command, user);
        protocol.setFrom(String.valueOf(user.getId()));
        protocol.setTo("roomManager:" + idRoom);
        sender.send(protocol.toString());
        return receive();
    }
}
