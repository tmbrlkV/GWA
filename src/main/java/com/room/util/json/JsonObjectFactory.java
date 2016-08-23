package com.room.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.room.util.entity.User;

import java.io.IOException;

public final class JsonObjectFactory {
    private JsonObjectFactory() {
    }

    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> String getJsonString(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static <T> T getObjectFromJson(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            return null;
        }
    }
}
