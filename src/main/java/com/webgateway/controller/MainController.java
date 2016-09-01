package com.webgateway.controller;

import com.chat.util.entity.Message;
import com.webgateway.config.socket.zmq.SocketConfig;
import com.webgateway.entity.MessageStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {
    private static Thread receivingThread;
    private final SocketConfig<Message> messageSocket;
    private final SocketConfig<String> roomManagerSocket;

    @Autowired
    public MainController(@Qualifier("roomManagerSocketConfig") SocketConfig<String> roomManagerSocket,
                          @Qualifier("messageSocketConfig") SocketConfig<Message> messageSocket) {
        this.roomManagerSocket = roomManagerSocket;
        this.messageSocket = messageSocket;
    }

    @RequestMapping(value = "/init")
    public ModelAndView init() throws Exception {
        startReceivingThread();
        roomManagerSocket.setCommand("getAllRooms");
        roomManagerSocket.send("15000");
        System.out.println("getAllRooms: " + roomManagerSocket.receive());
        roomManagerSocket.setCommand("getAllUsers");
        roomManagerSocket.send("15000");
        System.out.println("getAllUsers: " + roomManagerSocket.receive());
        roomManagerSocket.setCommand("getAllUsersInRoom");
        roomManagerSocket.send("15000");
        System.out.println("getAllUsersInRoom" + roomManagerSocket.receive());
        return new ModelAndView("redirect:/");
    }

    @RequestMapping("/out")
    public ModelAndView logout() throws Exception {
        return new ModelAndView("redirect:/login");
    }

    @MessageMapping("/hello/{id}")
    @SendTo("/topic/greetings/{id}")
    public MessageStub greeting(Message message, @DestinationVariable String id) throws Exception {
        messageSocket.setCommand(id);
        messageSocket.send(message);
        return new MessageStub("");
    }

    private void startReceivingThread() {
        if (receivingThread == null) {
            receivingThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    messageSocket.receive();
                }
            });
            receivingThread.start();
        }
    }
}
