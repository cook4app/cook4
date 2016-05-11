package com.beppeben.cook4.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4DishComment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private C4Dish dish;
    private Long dishId;
    private String dishName;
    private String cookName;
    private Long authorId;
    private String authorName;
    private C4User author;
    private String message;
    private Float rating;
    private Date ratingDate;


    public C4DishComment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getCookName() {
        return cookName;
    }

    public void setCookName(String cookName) {
        this.cookName = cookName;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
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

    public Date getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(Date ratingDate) {
        this.ratingDate = ratingDate;
    }

    public C4Dish getDish() {
        return dish;
    }

    public void setDish(C4Dish dish) {
        this.dish = dish;
    }

    public C4User getAuthor() {
        return author;
    }

    public void setAuthor(C4User author) {
        this.author = author;
    }

}
