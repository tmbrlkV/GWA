package com.webgateway.config.socket.zmq;

import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

import javax.annotation.PreDestroy;
import java.util.Properties;

@Component
public abstract class SocketConfig<T> {
    private final ZMQ.Socket sender;
    private final ZMQ.Socket receiver;
    private String command;

    protected SocketConfig() {
        Properties properties = ConnectionProperties.getProperties();
        ZMQ.Context context = ZmqContextHolder.getContext();
        sender = context.socket(ZMQ.PUSH);
        sender.connect(properties.getProperty("to_butler_address"));
        receiver = context.socket(ZMQ.SUB);
        receiver.connect(properties.getProperty("from_butler_address"));
    }

    public abstract void send(T json);

    public abstract String receive();

    protected ZMQ.Socket getReceiver() {
        return receiver;
    }

    protected ZMQ.Socket getSender() {
        return sender;
    }

    protected String getCommand() {
        return command;
    }

    public void setCommand(String command) {

    }

    @PreDestroy
    private void close() {
        sender.close();
        receiver.close();
    }
}
