package com.webgateway.controller;

import com.chat.util.entity.Message;
import com.chat.util.entity.User;
import com.webgateway.config.socket.zmq.RoomManagerSocketConfig;
import com.webgateway.config.socket.zmq.SocketConfig;
import com.webgateway.entity.CustomUser;
import com.webgateway.entity.MessageStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

@Controller
public class MainController {
    private static Thread receivingThread;
    private final SocketConfig<Message> messageSocket;
    private final RoomManagerSocketConfig roomManagerSocket;

    @Autowired
    public MainController(RoomManagerSocketConfig roomManagerSocket,
                          @Qualifier("messageSocketConfig") SocketConfig<Message> messageSocket) {
        this.roomManagerSocket = roomManagerSocket;
        this.messageSocket = messageSocket;
    }

    @RequestMapping(value = "/init")
    public ModelAndView init() throws Exception {
        startReceivingThread();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUser) authentication.getPrincipal()).toUser();
        roomManagerSocket.addClientToLobby(user);
        System.out.println(roomManagerSocket.getInfo(user));
        System.out.println(Arrays.toString(roomManagerSocket.getAllUsersInRoom(user, 15000L)));
        return new ModelAndView("redirect:/");
    }

    @RequestMapping("/out")
    public ModelAndView logout() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
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
