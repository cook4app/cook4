package com.beppeben.cook4.domain;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Float rating;
    private Integer orders;
    private byte[] imgArray;
    private String description;
    private Long id;
    private C4User user;
    private List<Long> picIds;
    private Long coverId;
    private List<C4Tag> dishtags;
    private Bitmap imgBmp;


    public C4Dish(String name, Float rating, Integer orders) {
        super();
        this.name = name;
        this.rating = rating;
        this.orders = orders;
    }


    public C4Dish() {
    }

    public C4Dish(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((orders == null) ? 0 : orders.hashCode());
        result = prime * result + ((rating == null) ? 0 : rating.hashCode());
        return result;
    }

    public byte[] getImgArray() {
        return imgArray;
    }

    public void setImgArray(byte[] imgArray) {
        this.imgArray = imgArray;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public C4User getUser() {
        return user;
    }

    public void setUser(C4User user) {
        this.user = user;
    }

    public List<Long> getPicIds() {
        List<Long> result = new ArrayList<Long>();
        if (coverId != null) result.add(coverId);
        if (picIds != null) {
            for (Long id : picIds) {
                if (!id.equals(coverId)) result.add(id);
            }
        }
        if (result.size() == 0) return null;
        return result;
    }

    public void setPicIds(List<Long> picIds) {
        this.picIds = picIds;
    }

    public Long getCoverId() {
        return coverId;
    }

    public void setCoverId(Long coverId) {
        this.coverId = coverId;
    }

    public List<C4Tag> getDishtags() {
        return dishtags;
    }

    public void setDishtags(List<C4Tag> dishtags) {
        this.dishtags = dishtags;
    }

    public List<String> obtainStringTags() {
        List<String> ctags = new ArrayList<String>();
        for (C4Tag tag : dishtags) {
            ctags.add(tag.getTag());
        }
        return ctags;
    }

    @JsonIgnore
    public Bitmap getImgBmp() {
        return imgBmp;
    }

    public void setImgBmp(Bitmap imgBmp) {
        this.imgBmp = imgBmp;
    }

}
