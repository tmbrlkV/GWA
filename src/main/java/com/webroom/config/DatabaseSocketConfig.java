package com.webroom.config;

import com.room.socket.ConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

public class DatabaseSocketConfig {
    private Logger logger = LoggerFactory.getLogger(DatabaseSocketConfig.class);
    private Socket socket;
    private PrintWriter printWriter;
    private static DatabaseSocketConfig instance;

    public static DatabaseSocketConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseSocketConfig();
        }
        return instance;
    }

    private DatabaseSocketConfig() {
        try {
            Properties properties = ConnectionProperties.getProperties();
            int port = Integer.parseInt(properties.getProperty("room_port"));
            String address = properties.getProperty("room_address");
            socket = new Socket(address, port);
            printWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void send(String json) {
        printWriter.println(json);
        printWriter.flush();
    }

    public String receive() {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] message = new byte[8192];
            logger.debug("receive() before read");
            int read = inputStream.read(message);
            logger.debug("receive() after read");
            if (read > 0) {
                return new String(message).trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void close() {
        try {
            socket.close();
            instance = null;
        } catch (IOException e) {
            logger.error("Bad close", e);
            e.printStackTrace();
        }
    }
}
