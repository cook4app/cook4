package com.beppeben.cook4server.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "cook4_pendingconfirm", schema = "APP")
@XmlRootElement
public class C4PendingConfirm {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String token;

    public C4PendingConfirm() {
    }

    public C4PendingConfirm(Long id, String token) {
        this.id = id;
        this.token = token;
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
