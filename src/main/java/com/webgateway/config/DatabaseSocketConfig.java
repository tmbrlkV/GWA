package com.webgateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

@Component("databaseSocketConfig")
public final class DatabaseSocketConfig extends SocketConfig<String> {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSocketConfig.class);
    private final ZMQ.Socket receiver;
    private final ZMQ.Socket sender;

    public DatabaseSocketConfig() {
        super();
        receiver = getReceiver();
        sender = getSender();
        receiver.subscribe("database".getBytes());
    }

    @Override
    public void send(String request) {
        sender.send(request);
    }

    @Override
    public String receive() {
        receiver.recvStr();
        return receiver.recvStr();
    }
}
