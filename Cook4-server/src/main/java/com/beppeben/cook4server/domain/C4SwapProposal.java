package com.beppeben.cook4server.domain;

import com.beppeben.cook4server.utils.DateAdapter;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Entity
@Table(name = "cook4_swap_proposal", schema = "APP")
@XmlRootElement
public class C4SwapProposal extends C4Entity implements Serializable {

    private String fromCookName;
    private String toCookName;
    private C4User fromCook;
    private C4User toCook;
    private Long fromCookId;
    private Long toCookId;
    private String targetDishName;
    private String rewardDishName;
    private C4Dish targetDish;
    private C4Dish rewardDish;
    private Long targetDishId;
    private Long rewardDishId;
    private Boolean toCookDelivers;
    private String deliveryAddress;
    private String addressDetails;
    private Double latitude;
    private Double longitude;
    private Integer targetDishPortions;
    private Integer rewardDishPortions;
    private Date date;
    private Date validUntil;
    private Long itemId;
    private C4Item item;

    public C4SwapProposal() {

    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public String getFromCookName() {
        return fromCookName;
    }

    public void setFromCookName(String fromCookName) {
        this.fromCookName = fromCookName;
    }

    @Transient
    public String getToCookName() {
        return toCookName;
    }

    public void setToCookName(String toCookName) {
        this.toCookName = toCookName;
    }

    @Transient
    public Long getFromCookId() {
        return fromCookId;
    }

    public void setFromCookId(Long fromCookId) {
        this.fromCookId = fromCookId;
    }

    @Transient
    public Long getToCookId() {
        return toCookId;
    }

    public void setToCookId(Long toCookId) {
        this.toCookId = toCookId;
    }

    @Transient
    public String getTargetDishName() {
        return targetDishName;
    }

    public void setTargetDishName(String targetDishName) {
        this.targetDishName = targetDishName;
    }

    @Transient
    public String getRewardDishName() {
        return rewardDishName;
    }

    public void setRewardDishName(String rewardDishName) {
        this.rewardDishName = rewardDishName;
    }

    @Transient
    public Long getTargetDishId() {
        return targetDishId;
    }

    public void setTargetDishId(Long targetDishId) {
        this.targetDishId = targetDishId;
    }

    @Transient
    public Long getRewardDishId() {
        return rewardDishId;
    }

    public void setRewardDishId(Long rewardDishId) {
        this.rewardDishId = rewardDishId;
    }

    public Boolean getToCookDelivers() {
        return toCookDelivers;
    }

    public void setToCookDelivers(Boolean toCookDelivers) {
        this.toCookDelivers = toCookDelivers;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
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

    public Integer getTargetDishPortions() {
        return targetDishPortions;
    }

    public void setTargetDishPortions(Integer targetDishPortions) {
        this.targetDishPortions = targetDishPortions;
    }

    public Integer getRewardDishPortions() {
        return rewardDishPortions;
    }

    public void setRewardDishPortions(Integer rewardDishPortions) {
        this.rewardDishPortions = rewardDishPortions;
    }

    @XmlJavaTypeAdapter(DateAdapter.class)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @XmlJavaTypeAdapter(DateAdapter.class)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    @Transient
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @XmlTransient
    public C4User getFromCook() {
        return fromCook;
    }

    public void setFromCook(C4User fromCook) {
        this.fromCook = fromCook;
    }

    @XmlTransient
    public C4User getToCook() {
        return toCook;
    }

    public void setToCook(C4User toCook) {
        this.toCook = toCook;
    }

    @XmlTransient
    public C4Dish getTargetDish() {
        return targetDish;
    }

    public void setTargetDish(C4Dish targetDish) {
        this.targetDish = targetDish;
    }

    @XmlTransient
    public C4Dish getRewardDish() {
        return rewardDish;
    }

    public void setRewardDish(C4Dish rewardDish) {
        this.rewardDish = rewardDish;
    }

    @XmlTransient
    public C4Item getItem() {
        return item;
    }

    public void setItem(C4Item item) {
        this.item = item;
    }

}
