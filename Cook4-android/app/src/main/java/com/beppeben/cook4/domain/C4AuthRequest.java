package com.beppeben.cook4.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
public class C4AuthRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String message;
    private Boolean diploma = false;

    public C4AuthRequest() {
    }

    public C4AuthRequest(Long userId, String message, Boolean diploma) {
        this.userId = userId;
        this.message = message;
        this.diploma = diploma;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getDiploma() {
        return diploma;
    }

    public void setDiploma(Boolean diploma) {
        this.diploma = diploma;
    }
}
