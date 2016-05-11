package com.beppeben.cook4server.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "cook4_pass", schema = "APP")
@XmlRootElement
public class C4Pass {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String password;
    private String newPassword;
    private Boolean confirmed;

    public C4Pass() {
    }

    public C4Pass(Long id, String password, Boolean confirmed) {
        this.id = id;
        this.password = password;
        this.confirmed = confirmed;
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @NotNull
    public Boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
