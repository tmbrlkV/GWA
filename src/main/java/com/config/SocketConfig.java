package com.config;

import com.entity.Greeting;
import com.entity.Message;
import com.room.socket.ConnectionProperties;
import com.room.util.json.JsonMessage;
import com.room.util.json.JsonObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class SocketConfig {
    private Logger logger = LoggerFactory.getLogger(SocketConfig.class);
    private Socket socket;
    private PrintWriter printWriter;
    private Scanner scanner;
    private static SocketConfig instance;

    public static SocketConfig getInstance() {
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
            socket = new Socket(address, port);
            printWriter = new PrintWriter(socket.getOutputStream());
            scanner = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Bad init", e);
        }
    }

    public void send(Message message) {
        String command = "message";
        JsonMessage jsonMessage = new JsonMessage(command, message.getUser(), message.getMessage());
        jsonMessage.setFrom(ConnectionProperties.getProperties().getProperty("room_port"));
        String string = JsonObjectFactory.getJsonString(jsonMessage);
        printWriter.println(string);
        printWriter.flush();
    }

    public void receive(SimpMessagingTemplate template) {
        if (scanner.hasNextLine()) {
            logger.debug("receive(SMT) before read");
            String json = scanner.nextLine();
            logger.debug("receive(SMT) after read");

            JsonMessage objectFromJson = JsonObjectFactory.getObjectFromJson(json, JsonMessage.class);
            if (objectFromJson != null) {
                template.convertAndSend("/topic/greetings",
                        new Greeting(objectFromJson.getUsername() + ": " + objectFromJson.getContent()));
            }
        }
    }


    public void close() {
        try {
            socket.close();
            instance = null;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Bad close", e);
        }
    }
}
