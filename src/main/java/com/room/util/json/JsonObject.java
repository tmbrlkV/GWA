package com.room.util.json;


import com.room.util.entity.User;

public class JsonObject {
    private String command;
    private User user;
    private String to;
    private String from;

    public JsonObject() {
    }


    public JsonObject(String command, User user) {
        this.command = command;
        this.user = user;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getCommand() {
        return command;
    }

    public User getUser() {
        return user;
    }
}
