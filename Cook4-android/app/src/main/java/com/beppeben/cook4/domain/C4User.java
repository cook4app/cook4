package com.beppeben.cook4.domain;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String email;
    private Float foodRating;
    private Integer sellExperience;
    private Float generalRating;
    private Integer generalExperience;
    private Double latitude;
    private Double longitude;
    private String city;
    private String address;
    private String country;
    private List<C4Dish> dishes;
    private List<C4Item> offers;
    private Long id;
    private String regid;
    private Long photoId;
    private Integer separation;
    private String reccomendedBy;
    private String visitKey;
    private boolean modData = false;
    private boolean unlocated = false;
    private Double modLatitude;
    private Double modLongitude;
    private String modCity;
    private String modAddress;
    private String modCountry;
    private String message;
    private String password;
    private String loginMethod = "not_set";
    private String totalEarned;
    private String totalSpent;
    private Boolean refreshTags;
    private String versionCode;
    private String description;
    private Bitmap imgBmp;
    private boolean passConfirmed = false;
    private String privilege;
    private String payEmail;
    private String language;


    public C4User() {
    }

    @JsonIgnore
    public boolean isPassConfirmed() {
        return passConfirmed;
    }

    public void setPassConfirmed(boolean passConfirmed) {
        this.passConfirmed = passConfirmed;
    }

    public C4User(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getFoodRating() {
        return foodRating;
    }

    public void setFoodRating(Float foodRating) {
        this.foodRating = foodRating;
    }

    public Float getGeneralRating() {
        return generalRating;
    }

    public void setGeneralRating(Float generalRating) {
        this.generalRating = generalRating;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<C4Dish> getDishes() {
        if (dishes != null)
            return dishes;
        else return new ArrayList<C4Dish>();
    }

    public void setDishes(List<C4Dish> dishes) {
        this.dishes = dishes;
    }

    public List<C4Item> getOffers() {
        return offers;
    }

    public void setOffers(List<C4Item> offers) {
        this.offers = offers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGeneralExperience() {
        return generalExperience;
    }

    public void setGeneralExperience(Integer generalExperience) {
        this.generalExperience = generalExperience;
    }

    public Integer getSellExperience() {
        return sellExperience;
    }

    public void setSellExperience(Integer sellExperience) {
        this.sellExperience = sellExperience;
    }

    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }

    public Float score() {
        final float FOOD_WEIGHT = 0.7F;
        return FOOD_WEIGHT * foodScore() + (1 - FOOD_WEIGHT) * genScore();
    }

    public Float foodScore() {
        final float THRESHOLD = 2F;
        float food_part = 0;
        if (foodRating != null) food_part = (float) sellExperience * (foodRating - THRESHOLD);
        return food_part;
    }

    public Float genScore() {
        final float THRESHOLD = 2F;
        float general_part = 0;
        if (generalRating != null)
            general_part = (float) generalExperience * (generalRating - THRESHOLD);
        return general_part;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public Integer getSeparation() {
        return separation;
    }

    public void setSeparation(Integer separation) {
        this.separation = separation;
    }

    public String getReccomendedBy() {
        return reccomendedBy;
    }

    public void setReccomendedBy(String reccomendedBy) {
        this.reccomendedBy = reccomendedBy;
    }

    public String getVisitKey() {
        return visitKey;
    }

    public void setVisitKey(String visitKey) {
        this.visitKey = visitKey;
    }

    @JsonIgnore
    public boolean isModData() {
        return modData;
    }

    public void setModData(boolean modData) {
        this.modData = modData;
    }

    public Double getModLatitude() {
        return modLatitude;
    }

    public void setModLatitude(Double modLatitude) {
        this.modLatitude = modLatitude;
    }

    public Double getModLongitude() {
        return modLongitude;
    }

    public void setModLongitude(Double modLongitude) {
        this.modLongitude = modLongitude;
    }

    public String getModCity() {
        return modCity;
    }

    public void setModCity(String modCity) {
        this.modCity = modCity;
    }

    public String getModAddress() {
        return modAddress;
    }

    public void setModAddress(String modAddress) {
        this.modAddress = modAddress;
    }

    public String getModCountry() {
        return modCountry;
    }

    public void setModCountry(String modCountry) {
        this.modCountry = modCountry;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginMethod() {
        return loginMethod;
    }

    public void setLoginMethod(String loginMethod) {
        this.loginMethod = loginMethod;
    }

    public String getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(String totalEarned) {
        this.totalEarned = totalEarned;
    }

    public String getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(String totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Boolean getRefreshTags() {
        return refreshTags;
    }

    public void setRefreshTags(Boolean refreshTags) {
        this.refreshTags = refreshTags;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public Bitmap getImgBmp() {
        return imgBmp;
    }

    public void setImgBmp(Bitmap imgBmp) {
        this.imgBmp = imgBmp;
    }

    @JsonIgnore
    public boolean isUnlocated() {
        return unlocated;
    }

    public void setUnlocated(boolean unlocated) {
        this.unlocated = unlocated;
    }

    @JsonIgnore
    public Double getAppLatitude() {
        if (!isModData()) {
            return latitude;
        } else {
            return modLatitude;
        }
    }

    @JsonIgnore
    public String getAppCity() {
        if (!isModData()) {
            return city;
        } else {
            return modCity;
        }
    }

    @JsonIgnore
    public Double getAppLongitude() {
        if (!isModData()) {
            return longitude;
        } else {
            return modLongitude;
        }
    }

    @JsonIgnore
    public String getAppAddress() {
        if (!isModData()) {
            return address;
        } else {
            return modAddress;
        }
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public String getPayEmail() {
        return payEmail;
    }

    public void setPayEmail(String payEmail) {
        this.payEmail = payEmail;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
