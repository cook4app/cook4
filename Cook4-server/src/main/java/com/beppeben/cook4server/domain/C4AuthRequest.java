package com.beppeben.cook4server.domain;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class C4AuthRequest implements Serializable {

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

    @NotNull
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @NotNull
    public Boolean getDiploma() {
        return diploma;
    }

    public void setDiploma(Boolean diploma) {
        this.diploma = diploma;
    }

}
