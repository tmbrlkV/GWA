package com.room.util.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.room.util.json.JsonObjectFactory;

public class Message<T> {
    private String login;
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
    private T content;

    public Message() {
    }

    public Message(String login, T content) {
        this.login = login;
        this.content = content;
    }

    public String getLogin() {
        return login;
    }

    public T getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message<?> message = (Message<?>) o;

        if (!login.equals(message.login)) return false;
        return content.equals(message.content);

    }

    @Override
    public int hashCode() {
        int result = login.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return JsonObjectFactory.getJsonString(this);
    }
}
