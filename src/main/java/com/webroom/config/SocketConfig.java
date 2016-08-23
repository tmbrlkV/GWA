package com.webroom.config;

import com.room.socket.ConnectionProperties;
import com.room.util.entity.Message;
import com.room.util.json.JsonObjectFactory;
import com.room.util.json.JsonProtocol;
import com.webroom.entity.MessageStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;

public class SocketConfig {
    private static Logger logger = LoggerFactory.getLogger(SocketConfig.class);
    private SocketChannel socket;
    private static SocketConfig instance;
    private ByteBuffer bufferReceive = ByteBuffer.allocate(1024);
    private ByteBuffer bufferSend = ByteBuffer.allocate(1024);

    public static SocketConfig getInstance() {
        if (instance != null) {
            logger.debug(String.valueOf(instance.socket));
        }
        if (instance == null) {
            instance = new SocketConfig();
        }
        return instance;
    }

    private SocketConfig() {
        try {
            Properties properties = ConnectionProperties.getProperties();
            int port = Integer.parseInt(properties.getProperty("room_port"));
            String address = properties.getProperty("room_address");
            socket = SocketChannel.open(new InetSocketAddress(address, port));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Bad init", e);
        }
    }

    public void send(Message message) {
        String command = "message";
        JsonProtocol<Message> jsonMessage = new JsonProtocol<>(command, message);
        jsonMessage.setFrom(ConnectionProperties.getProperties().getProperty("room_port"));
        String string = JsonObjectFactory.getJsonString(jsonMessage);
        bufferSend.put(string.getBytes());
        bufferSend.flip();
        try {
            int written = socket.write(bufferSend);
            logger.debug("Written {} bytes.", written);
            bufferSend.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive(SimpMessagingTemplate template) throws IOException {
        logger.debug("receive(SMT) before read");
        int read = socket.read(bufferReceive);
        if (read < 0) {
            close();
            throw new IOException();
        }

        logger.debug("receive(SMT) after read");

        String json = new String(bufferReceive.array()).trim();
        bufferReceive.clear();

        JsonProtocol<Message> objectFromJson = JsonObjectFactory.getObjectFromJson(json, JsonProtocol.class);
        if (objectFromJson != null) {
            template.convertAndSend("/topic/greetings",
                    new MessageStub(objectFromJson.getAttachment().getLogin() + ": "
                            + objectFromJson.getAttachment().getContent()));
        }
    }


    private void close() {
        try {
            socket.close();
            instance = null;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Bad close", e);
        }
    }
}
