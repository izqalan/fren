package com.izqalan.messenger;

public class Conversation {

    public long timestamp;

    public Conversation(){}

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



    public Conversation(long timestamp)
    { this.timestamp = timestamp; }

}
