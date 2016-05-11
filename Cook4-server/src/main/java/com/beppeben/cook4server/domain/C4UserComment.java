package com.beppeben.cook4server.domain;

import com.beppeben.cook4server.utils.DateAdapter;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Entity
@Table(name = "user_comments", schema = "APP")
@XmlRootElement
public class C4UserComment extends C4Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long toUserId;
    private String userName;
    private Long fromUserId;
    private String authorName;
    private C4User toUser;
    private C4User fromUser;
    private String message;
    private Float rating;
    private Date ratingDate;

    public C4UserComment() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    @NotNull
    @XmlJavaTypeAdapter(DateAdapter.class)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(Date ratingDate) {
        this.ratingDate = ratingDate;
    }

    @Transient
    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    @Transient
    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    @XmlTransient
    @ManyToOne
    public C4User getToUser() {
        return toUser;
    }

    public void setToUser(C4User toUser) {
        this.toUser = toUser;
    }

    @XmlTransient
    @ManyToOne
    public C4User getFromUser() {
        return fromUser;
    }

    public void setFromUser(C4User fromUser) {
        this.fromUser = fromUser;
    }

    @Transient
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Transient
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

}
