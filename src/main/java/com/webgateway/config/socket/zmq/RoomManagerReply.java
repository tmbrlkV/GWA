package com.webgateway.config.socket.zmq;

import java.util.Arrays;

public class RoomManagerReply {
    private Long[] rooms;
    private Long[] users;

    public RoomManagerReply(Long[] rooms, Long[] users) {
        this.rooms = rooms;
        this.users = users;
    }

    public Long[] getRooms() {
        return rooms.clone();
    }

    public Long[] getUsers() {
        return users.clone();
    }

    @Override
    public String toString() {
        return "RoomManagerReply{" +
                "rooms=" + Arrays.toString(rooms) +
                ", users=" + Arrays.toString(users) +
                '}';
    }
}
