package com.beppeben.cook4.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4SwapProposal implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String fromCookName;
    private String toCookName;
    private Long fromCookId;
    private Long toCookId;
    private String targetDishName;
    private String rewardDishName;
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


    public C4SwapProposal() {
    }

    public static C4SwapProposal transToSwap(C4Transaction trans) {
        C4SwapProposal swap = new C4SwapProposal();
        swap.setFromCookId(trans.getFoodieId());
        swap.setFromCookName(trans.getCookName());
        swap.setToCookId(trans.getCookId());
        swap.setToCookName(trans.getCookName());
        swap.setToCookDelivers(trans.getDelivery());
        swap.setTargetDishId(trans.getDishId());
        swap.setTargetDishName(trans.getDishName());
        swap.setDeliveryAddress(trans.getDeliveryAddress());
        swap.setAddressDetails(trans.getAddressDetails());
        swap.setLatitude(trans.getLatitude());
        swap.setLongitude(trans.getLongitude());
        swap.setTargetDishPortions(trans.getPortions());
        swap.setDate(trans.getDate());
        swap.setItemId(trans.getItemId());
        return swap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromCookName() {
        return fromCookName;
    }

    public void setFromCookName(String fromCookName) {
        this.fromCookName = fromCookName;
    }

    public String getToCookName() {
        return toCookName;
    }

    public void setToCookName(String toCookName) {
        this.toCookName = toCookName;
    }

    public Long getFromCookId() {
        return fromCookId;
    }

    public void setFromCookId(Long fromCookId) {
        this.fromCookId = fromCookId;
    }

    public Long getToCookId() {
        return toCookId;
    }

    public void setToCookId(Long toCookId) {
        this.toCookId = toCookId;
    }

    public String getTargetDishName() {
        return targetDishName;
    }

    public void setTargetDishName(String targetDishName) {
        this.targetDishName = targetDishName;
    }

    public String getRewardDishName() {
        return rewardDishName;
    }

    public void setRewardDishName(String rewardDishName) {
        this.rewardDishName = rewardDishName;
    }

    public Long getTargetDishId() {
        return targetDishId;
    }

    public void setTargetDishId(Long targetDishId) {
        this.targetDishId = targetDishId;
    }

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

}
