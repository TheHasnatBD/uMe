package com.infobox.hasnat.ume.ume.Models;

public class Message {
    private String message, type;
    private long time;
    private boolean seen;

    // default constructor

    public Message() {
    }


    // constructor

    public Message(String message, String type, long time, boolean seen) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }


    // getter & setter


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", time=" + time +
                ", seen=" + seen +
                '}';
    }
}
