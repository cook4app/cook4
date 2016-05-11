package com.beppeben.cook4server.domain;

import com.beppeben.cook4server.utils.DateAdapter;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Entity
@Table(name = "past_transaction", schema = "APP")
@XmlRootElement
public class C4PastTransaction extends C4Entity implements Serializable {

    private String dishName;
    private Long dishId;
    private Long cookId;
    private Long foodieId;
    private String cookName;
    private String foodieName;
    private Boolean delivery;
    private String deliveryAddress;
    private String addressDetails;
    private Float price;
    private Float cookRating;
    private Float foodieRating;
    private Float foodRating;
    private Date date;
    private Integer portions;
    private Double latitude;
    private Double longitude;
    private Long itemId;
    private Long twinTransId;
    private String currency;
    private Float totalPrice;
    private String extDelInfo;
    private Long deliveryId;
    private Long cancelledBy;
    private Float commission;

    public C4PastTransaction() {
    }

    public static C4PastTransaction newInstance(C4Transaction trans) {
        C4PastTransaction past = new C4PastTransaction();
        past.id = trans.getId();
        past.addressDetails = trans.getAddressDetails();
        past.cookId = trans.getCook().getId();
        past.cookName = trans.getCook().getName();
        past.cookRating = trans.getCookRating();
        past.date = trans.getDate();
        past.delivery = trans.getDelivery();
        past.deliveryAddress = trans.getDeliveryAddress();
        past.dishId = trans.getDish().getId();
        past.dishName = trans.getDish().getName();
        past.foodRating = trans.getFoodRating();
        past.foodieId = trans.getFoodie().getId();
        past.foodieName = trans.getFoodie().getName();
        past.foodieRating = trans.getFoodieRating();
        past.itemId = trans.getItemId();
        past.latitude = trans.getLatitude();
        past.longitude = trans.getLongitude();
        past.portions = trans.getPortions();
        past.price = trans.getPrice();
        past.currency = trans.getCurrency();
        past.totalPrice = trans.getTotalPrice();
        past.extDelInfo = trans.getExtDelInfo();
        //past.payId = trans.getPayId();
        past.deliveryId = trans.getDeliveryId();
        past.commission = trans.getCommission();
        return past;
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Long getCookId() {
        return cookId;
    }

    public void setCookId(Long cookId) {
        this.cookId = cookId;
    }

    public Long getFoodieId() {
        return foodieId;
    }

    public void setFoodieId(Long foodieId) {
        this.foodieId = foodieId;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    @XmlJavaTypeAdapter(DateAdapter.class)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
    }

    public Integer getPortions() {
        return portions;
    }

    public void setPortions(Integer portions) {
        this.portions = portions;
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

    public String getCookName() {
        return cookName;
    }

    public void setCookName(String cookName) {
        this.cookName = cookName;
    }

    public String getFoodieName() {
        return foodieName;
    }

    public void setFoodieName(String foodieName) {
        this.foodieName = foodieName;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Float getCookRating() {
        return cookRating;
    }

    public void setCookRating(Float cookRating) {
        this.cookRating = cookRating;
    }

    public Float getFoodieRating() {
        return foodieRating;
    }

    public void setFoodieRating(Float foodieRating) {
        this.foodieRating = foodieRating;
    }

    public Float getFoodRating() {
        return foodRating;
    }

    public void setFoodRating(Float foodRating) {
        this.foodRating = foodRating;
    }

    public Long getTwinTransactionId() {
        return twinTransId;
    }

    public void setTwinTransactionId(Long twinTransactionId) {
        this.twinTransId = twinTransactionId;
    }

    public Long getTwinTransId() {
        return twinTransId;
    }

    public void setTwinTransId(Long twinTransId) {
        this.twinTransId = twinTransId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getExtDelInfo() {
        return extDelInfo;
    }

    public void setExtDelInfo(String extDelInfo) {
        this.extDelInfo = extDelInfo;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    /*
     public String getPayId() {
     return payId;
     }

     public void setPayId(String payId) {
     this.payId = payId;
     }
     */
    public Long getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(Long cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    @XmlTransient
    public Float getCommission() {
        return commission;
    }

    public void setCommission(Float commission) {
        this.commission = commission;
    }

}
