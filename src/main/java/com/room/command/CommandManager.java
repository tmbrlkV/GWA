package com.room.command;


import com.chat.util.entity.User;
import com.chat.util.json.JsonObjectFactory;
import com.chat.util.json.JsonProtocol;
import com.room.server.ServerDataEvent;
import com.room.socket.ConnectionProperties;
import com.room.socket.SenderSocketHandler;
import com.room.socket.ZmqContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {
    private static Logger logger = LoggerFactory.getLogger(CommandManager.class);
    private SenderSocketHandler sender = new SenderSocketHandler();
    private String DEFAULT_REPLY = "";

    private Map<String, Command> commandMap = new ConcurrentHashMap<String, Command>() {{
        Command databaseCommand = request -> {
            try (ZMQ.Socket handler = ZmqContextHolder.getContext().socket(ZMQ.SUB)) {
                Properties properties = ConnectionProperties.getProperties();
                handler.connect(properties.getProperty("from_butler_address"));
                handler.subscribe("0".getBytes());

                sender.send(request);
                handler.recvStr();

                String reply = handler.recvStr();
                logger.debug(reply);
                User user = JsonObjectFactory.getObjectFromJson(reply, User.class);
                handler.unsubscribe("0".getBytes());
                return JsonObjectFactory.getJsonString(Optional.ofNullable(user).orElse(new User()));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Bad database command", e);
            }
            return DEFAULT_REPLY;
        };
        put(Command.GET_USER_BY_LOGIN_PASSWORD, databaseCommand);
        put(Command.GET_USER_BY_LOGIN, databaseCommand);
        put(Command.NEW_USER, databaseCommand);
        put(Command.MESSAGE, request -> {
            sender.send(request);
            return DEFAULT_REPLY;
        });
    }};

    public String execute(ServerDataEvent dataEvent) {
        String json = new String(dataEvent.getData());
        JsonProtocol request = JsonObjectFactory.getObjectFromJson(json, JsonProtocol.class);
        Optional<JsonProtocol> protocolOptional = Optional.ofNullable(request);
        String commandName = protocolOptional.map(JsonProtocol::getCommand).orElse("");
        Command command = commandMap.getOrDefault(commandName, r -> Command.NO_COMMAND);
        json = protocolOptional.map(JsonObjectFactory::getJsonString).orElse("");

        return command.execute(json);
    }
}
