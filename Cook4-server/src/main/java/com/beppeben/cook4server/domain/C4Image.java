package com.beppeben.cook4server.domain;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "cook4_image", schema = "APP")
@XmlRootElement
public class C4Image extends C4Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    private byte[] smallImage;
    private byte[] bigImage;
    private C4Dish dish;
    private C4User user;

    public C4Image() {
    }

    public C4Image(byte[] smallImage, byte[] bigImage) {
        this.smallImage = smallImage;
        this.bigImage = bigImage;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getSmallImage() {
        return smallImage;
    }

    public byte[] getBigImage() {
        return bigImage;
    }

    public void setSmallImage(byte[] smallImage) {
        this.smallImage = smallImage;
    }

    public void setBigImage(byte[] bigImage) {
        this.bigImage = bigImage;
    }

    @XmlTransient
    @ManyToOne
    public C4Dish getDish() {
        return dish;
    }

    public void setDish(C4Dish dish) {
        this.dish = dish;
    }

    @XmlTransient
    @OneToOne
    public C4User getUser() {
        return user;
    }

    public void setUser(C4User user) {
        this.user = user;
    }

}
