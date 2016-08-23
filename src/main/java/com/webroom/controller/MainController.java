package com.webroom.controller;

import com.room.util.entity.Message;
import com.webroom.config.SocketConfig;
import com.webroom.entity.MessageStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class MainController {
    private static Thread thread;
    private static SocketConfig instance;


    @RequestMapping("/init")
    public ModelAndView init() throws Exception {
        instance = SocketConfig.getInstance();
        startReceivingThread();
        return new ModelAndView("redirect:/");
    }

    @RequestMapping("/login")
    public ModelAndView logout() throws Exception {
        instance = SocketConfig.getInstance();
        return new ModelAndView("login");
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public MessageStub greeting(Message message) throws Exception {
        instance.send(message);
        return new MessageStub("");
    }


    @Autowired
    private SimpMessagingTemplate template;

    private void startReceivingThread() {
        if (thread == null) {
            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        instance.receive(template);
                    } catch (IOException e) {
                        thread.interrupt();
                        thread = null;
                    }
                }
            });
            thread.start();
        }
    }
}
