package com.beppeben.cook4.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class C4Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    public String username;
    public ArrayList<Message> messages;

    public C4Conversation() {
    }

    public static class Message {

        public boolean me;
        public String message;
        public Date date;

        public Message() {
        }

        public Message(boolean me, String message, Date date) {
            this.me = me;
            this.message = message;
            this.date = date;
        }
    }
}
