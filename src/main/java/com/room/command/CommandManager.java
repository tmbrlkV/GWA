package com.room.command;


import com.room.server.ServerDataEvent;
import com.room.socket.ConnectionProperties;
import com.room.socket.SenderSocketHandler;
import com.room.socket.ZmqContextHolder;
import com.room.util.entity.User;
import com.room.util.json.JsonMessage;
import com.room.util.json.JsonObject;
import com.room.util.json.JsonObjectFactory;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {
    private SenderSocketHandler sender = new SenderSocketHandler();
    private String DEFAULT_REPLY = "";

    private Map<String, Command> commandMap = new ConcurrentHashMap<String, Command>() {{
        Command databaseCommand = request -> {
            try (ZMQ.Socket handler = ZmqContextHolder.getContext().socket(ZMQ.SUB)){
                Properties properties = ConnectionProperties.getProperties();
                handler.connect(properties.getProperty("from_butler_address"));
                handler.subscribe("1".getBytes());

                sender.send(request);
                handler.recvStr();

                String reply = handler.recvStr();
                User user = JsonObjectFactory.getObjectFromJson(reply, User.class);
                handler.unsubscribe("1".getBytes());
                return JsonObjectFactory.getJsonString(Optional.ofNullable(user).orElse(new User()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return DEFAULT_REPLY;
        };
        put(Command.GET_USER_BY_LOGIN_PASSWORD, databaseCommand);
        put(Command.GET_USER_BY_LOGIN, databaseCommand);
        put(Command.NEW_USER, databaseCommand);
        put(Command.MESSAGE, request -> {
            System.out.println(request + " SEND");
            sender.send(request);
            return DEFAULT_REPLY;
        });
    }};

    public String execute(ServerDataEvent dataEvent) {
        String json = new String(dataEvent.getData());
        JsonObject databaseRequest = JsonObjectFactory.getObjectFromJson(json, JsonObject.class);
        JsonMessage message = JsonObjectFactory.getObjectFromJson(json, JsonMessage.class);

        Optional<JsonMessage> messageOptional = Optional.ofNullable(message);
        Optional<JsonObject> databaseRequestOptional = Optional.ofNullable(databaseRequest);

        String stringCommand = databaseRequestOptional.map(JsonObject::getCommand)
                .orElseGet(() -> messageOptional.map(JsonMessage::getCommand).orElse(Command.NO_COMMAND));

        Command command = commandMap.getOrDefault(stringCommand, request -> Command.NO_COMMAND);
        String jsonString = databaseRequestOptional.map(JsonObjectFactory::getJsonString)
                .orElseGet(() -> messageOptional.map(JsonObjectFactory::getJsonString).orElse(Command.NO_COMMAND));
        return command.execute(jsonString);
    }
}
