package com.beppeben.cook4.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4SignUpRequest implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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
