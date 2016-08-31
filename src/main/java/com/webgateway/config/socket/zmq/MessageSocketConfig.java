package com.webgateway.config.socket.zmq;

import com.chat.util.entity.Message;
import com.chat.util.json.JsonObjectFactory;
import com.chat.util.json.JsonProtocol;
import com.webgateway.entity.MessageStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("messageSocketConfig")
public final class MessageSocketConfig extends SocketConfig<Message> {
    private static final Pattern chatMessageProtocolPattern = Pattern.compile("chat:?(\\d*)");
    private static Logger logger = LoggerFactory.getLogger(MessageSocketConfig.class);
    private ZMQ.Socket receiver;
    private ZMQ.Socket sender;
    private String to;

    private final SimpMessagingTemplate template;

    @Autowired
    private MessageSocketConfig(SimpMessagingTemplate template) {
        super();
        sender = getSender();
        receiver = getReceiver();
        this.template = template;
        receiver.subscribe("chat:".getBytes());
    }

    public void setCommand(String to) {
        this.to = to;
    }

    @Override
    public void send(Message message) {
        String command = "message";
        JsonProtocol<Message> jsonMessage = new JsonProtocol<>(command, message);
        jsonMessage.setFrom(String.valueOf(message.getLogin()));
        jsonMessage.setTo("chat:" + to);
        String string = JsonObjectFactory.getJsonString(jsonMessage);
        sender.send(string);
    }

    @Override
    public String receive() {
        receive(template);
        return "";
    }


    private void receive(SimpMessagingTemplate template) {
        logger.debug("receive(SMT) before read");
        receiver.recvStr();
        String json = receiver.recvStr();

        JsonProtocol<Message> objectFromJson = JsonObjectFactory.getObjectFromJson(json, JsonProtocol.class);
        logger.debug("Receive {}", objectFromJson);
        if (objectFromJson != null) {
            Matcher matcher = chatMessageProtocolPattern.matcher(objectFromJson.getTo());
            String group = "chat:";
            if (matcher.matches()) {
                group = matcher.group(1);
            }
            logger.debug("Group {}", group);

            template.convertAndSend("/topic/greetings/" + group,
                    new MessageStub(objectFromJson.getAttachment().getLogin() + ": "
                            + objectFromJson.getAttachment().getContent()));
        }
    }
}
