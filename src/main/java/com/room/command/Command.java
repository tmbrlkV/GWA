package com.room.command;

interface Command {
    String CHAT = "chat";
    String NO_COMMAND = "";
    String DATABASE = "database";

    String execute(String request);
}
