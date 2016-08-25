package com.webgateway;

import com.gateway.server.NioServer;
import com.gateway.server.Worker;
import com.gateway.socket.ConnectionProperties;
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
            int port = Integer.parseInt(properties.getProperty("gw_port"));
            String host = properties.getProperty("gw_address");
            InetAddress address = InetAddress.getByName(host);
            new Thread(new NioServer(address, port, worker)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SpringApplication.run(Application.class, args);
    }
}
