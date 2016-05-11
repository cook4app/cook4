package com.beppeben.cook4.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
public class C4Report implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Long toDishId;
    private String message;


    public C4Report() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getToDishId() {
        return toDishId;
    }

    public void setToDishId(Long toDishId) {
        this.toDishId = toDishId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
