package com.webgateway.config;

import com.chat.util.entity.Message;
import com.chat.util.json.JsonObjectFactory;
import com.chat.util.json.JsonProtocol;
import com.gateway.socket.ConnectionProperties;
import com.webgateway.entity.MessageStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("messageSocketConfig")
public final class MessageSocketConfig extends SocketConfig<Message> {
    private static Logger logger = LoggerFactory.getLogger(MessageSocketConfig.class);
    @Autowired
    private SessionHandler sessionHandler;
    private List<String> subscriptions = new ArrayList<>();
    private ZMQ.Socket receiver;
    private ZMQ.Socket sender;

    private MessageSocketConfig() {
        super();
        sender = getSender();
        receiver = getReceiver();
        String sub = "chat:15000";
        subscribe(sub);
    }

    public void subscribe(String sub) {
        if (!subscriptions.contains(sub)) {
            subscriptions.add(sub);
            receiver.subscribe(sub.getBytes());
        }
    }

    public void unsubscribe(String unSub) {
        subscriptions.remove(unSub);
        receiver.unsubscribe(unSub.getBytes());
    }

    @Override
    public void send(Message message) {
        String command = "message";
        JsonProtocol<Message> jsonMessage = new JsonProtocol<>(command, message);
//        User user = ((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toUser();
        jsonMessage.setFrom(String.valueOf(message.getLogin()));
        jsonMessage.setTo("chat:" + ConnectionProperties.getProperties().getProperty("gw_port"));
        String string = JsonObjectFactory.getJsonString(jsonMessage);
        sender.send(string);
    }


    @Autowired
    private SimpMessagingTemplate template;

    @Override
    public String receive() throws IOException {
        receive(template);
        return "";
    }


    private void receive(SimpMessagingTemplate template) throws IOException {
        logger.debug("receive(SMT) before read");
        receiver.recvStr();
        String json = receiver.recvStr();

        JsonProtocol<Message> objectFromJson = JsonObjectFactory.getObjectFromJson(json, JsonProtocol.class);
        if (objectFromJson != null) {
            template.convertAndSend("/topic/greetings",
                    new MessageStub(objectFromJson.getAttachment().getLogin() + ": "
                            + objectFromJson.getAttachment().getContent()));
        }
    }
}
