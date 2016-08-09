package com;

import com.room.server.NioServer;
import com.room.server.Worker;
import com.room.socket.ConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        try {
            Worker worker = new Worker();
            new Thread(worker).start();
            Properties properties = ConnectionProperties.getProperties();
            int port = Integer.parseInt(properties.getProperty("room_port"));
            String host = properties.getProperty("room_address");
            InetAddress address = InetAddress.getByName(host);
            new Thread(new NioServer(address, port, worker)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SpringApplication.run(Application.class, args);
    }
}
