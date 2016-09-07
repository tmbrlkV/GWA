package com.webgateway.config.socket.zmq;

import com.chat.util.entity.User;
import com.chat.util.json.JsonProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

@Component("databaseSocketConfig")
public final class DatabaseSocketConfig extends SocketConfig<String> {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSocketConfig.class);
    private static final String BAD_REPLY = new JsonProtocol<>("", new User()).toString();
    private final ZMQ.Socket receiver;
    private final ZMQ.Socket sender;
    private final ZMQ.Poller poller;

    public DatabaseSocketConfig() {
        super();
        receiver = getReceiver();
        sender = getSender();
        receiver.subscribe("database".getBytes());
        poller = new ZMQ.Poller(0);
        poller.register(receiver, ZMQ.Poller.POLLIN);
    }

    @Override
    public void send(String request) {
        sender.send(request);
    }

    @Override
    public String receive() {
        if (poller.poll(150) > 0) {
            receiver.recvStr();
            return receiver.recvStr();
        }
        return BAD_REPLY;
    }
}
