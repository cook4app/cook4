package com.beppeben.cook4server.domain;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class C4SignUpRequest implements Serializable {

    private String username;
    private String password;
    private String email;
    private String regid;
    private String language;

    public C4SignUpRequest() {
    }

    public C4SignUpRequest(String username, String password, String email, String regid) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.regid = regid;
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
