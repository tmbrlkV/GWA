package com.webgateway.controller;

import com.chat.util.entity.Message;
import com.webgateway.config.socket.zmq.SocketConfig;
import com.webgateway.entity.MessageStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {
    private static Thread receivingThread;
    @Autowired
    @Qualifier("messageSocketConfig")
    private SocketConfig<Message> messageSocket;
    @Autowired
    @Qualifier("roomManagerSocketConfig")
    private SocketConfig<String> roomManagerSocket;

    @RequestMapping(value = "/init")
    public ModelAndView init() throws Exception {
        startReceivingThread();
//        roomManagerSocketConfig = RoomManagerSocketConfig.getInstance();
//        roomManagerSocketConfig.send();
//        System.out.println(roomManagerSocketConfig.receive());
        return new ModelAndView("redirect:/");
    }

    @RequestMapping("/login")
    public ModelAndView logout() throws Exception {
//        SecurityContextHolder.getContext().setAuthentication(null);
        return new ModelAndView("login");
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public MessageStub greeting(Message message) throws Exception {
        messageSocket.send(message);
        return new MessageStub("");
    }

    private void startReceivingThread() {
        if (receivingThread == null) {
            receivingThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
//                    try {
                    messageSocket.receive();
//                    } catch (IOException e) {
//                        receivingThread.interrupt();
//                        receivingThread = null;
//                    }
                }
            });
            receivingThread.start();
        }
    }
}
