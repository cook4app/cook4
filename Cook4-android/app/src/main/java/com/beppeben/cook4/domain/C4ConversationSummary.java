package com.beppeben.cook4.domain;

import java.io.Serializable;
import java.util.Date;

public class C4ConversationSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private Date last;
    private boolean read;

    public C4ConversationSummary(Long id, String username, Date last, boolean read) {
        this.id = id;
        this.username = username;
        this.last = last;
        this.read = read;
    }

    public C4ConversationSummary() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLast() {
        return last;
    }

    public void setLast(Date last) {
        this.last = last;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }


}