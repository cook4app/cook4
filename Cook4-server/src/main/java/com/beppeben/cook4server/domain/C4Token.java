package com.beppeben.cook4server.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "cook4_token", schema = "APP")
@XmlRootElement
public class C4Token {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String token;

    public C4Token() {
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(length = 1000)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
