package com.webgateway.config;

import com.chat.util.entity.User;
import com.chat.util.json.JsonProtocol;
import com.gateway.socket.ConnectionProperties;
import com.webgateway.entity.CustomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

public class RoomManagerSocketConfig {
    private Logger logger = LoggerFactory.getLogger(DatabaseSocketConfig.class);
    private Socket socket;
    private PrintWriter printWriter;
    private static RoomManagerSocketConfig instance;

    public static RoomManagerSocketConfig getInstance() {
        if (instance == null) {
            instance = new RoomManagerSocketConfig();
        }
        return instance;
    }

    private RoomManagerSocketConfig() {
        try {
            Properties properties = ConnectionProperties.getProperties();
            int port = Integer.parseInt(properties.getProperty("gw_port"));
            String address = properties.getProperty("gw_address");
            socket = new Socket(address, port);
            printWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void send() {
        User user = ((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toUser();
        JsonProtocol<User> protocol = new JsonProtocol<>("addUserToRoom", user);
        protocol.setFrom(String.valueOf(user.getId()));
        protocol.setTo("roomManager:15000");
        printWriter.println(protocol.toString());
        printWriter.flush();
    }

    public String receive() {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] message = new byte[8192];
            logger.debug("receive(room manager) before read");
            int read = inputStream.read(message);
            logger.debug("receive(room manager) after read");
            if (read > 0) {
                return new String(message).trim();
            }
        } catch (IOException e) {
            logger.error("Error {}", e);
        }
        return "";
    }

    public void close() {
        try {
            socket.close();
            instance = null;
        } catch (IOException e) {
            logger.error("Bad close", e);
        }
    }
}
