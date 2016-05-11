package com.beppeben.cook4server.domain;

import com.beppeben.cook4server.utils.DateAdapter;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Entity
@Table(name = "temp_transaction", schema = "APP")
@XmlRootElement
public class C4TempTransaction extends C4Entity implements Serializable {

    private C4Dish dish;
    private C4User cook;
    private C4User foodie;
    private Boolean delivery;
    private String deliveryAddress;
    private String addressDetails;
    private String pickupAddress;
    private String pickupAddressDetails;
    private Float price;
    private Date date;
    private Integer portions;
    private Double latitude;
    private Double longitude;
    private Long itemId;
    private String currency;
    private String phone;
    private Float totalPrice;
    private String extDelInfo;
    private Long deliveryId;
    private String payKey;
    private String timeZone;
    private Float commission;

    public C4TempTransaction() {
    }

    public C4TempTransaction(C4Transaction trans) {
        dish = trans.getDish();
        cook = trans.getCook();
        foodie = trans.getFoodie();
        delivery = trans.getDelivery();
        deliveryAddress = trans.getDeliveryAddress();
        addressDetails = trans.getAddressDetails();
        price = trans.getPrice();
        date = trans.getDate();
        portions = trans.getPortions();
        latitude = trans.getLatitude();
        longitude = trans.getLongitude();
        itemId = trans.getItemId();
        currency = trans.getCurrency();
        phone = trans.getPhone();
        totalPrice = trans.getTotalPrice();
        extDelInfo = trans.getExtDelInfo();
        deliveryId = trans.getDeliveryId();
        commission = trans.getCommission();
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @XmlTransient
    public C4User getCook() {
        return cook;
    }

    public void setCook(C4User cook) {
        this.cook = cook;
    }

    @XmlTransient
    public C4User getFoodie() {
        return foodie;
    }

    public void setFoodie(C4User foodie) {
        this.foodie = foodie;
    }

    @XmlTransient
    @ManyToOne
    public C4Dish getDish() {
        return dish;
    }

    public void setDish(C4Dish dish) {
        this.dish = dish;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Transient
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getPickupAddressDetails() {
        return pickupAddressDetails;
    }

    public void setPickupAddressDetails(String pickupAddressDetails) {
        this.pickupAddressDetails = pickupAddressDetails;
    }

    public String getPayKey() {
        return payKey;
    }

    public void setPayKey(String payKey) {
        this.payKey = payKey;
    }
    /*
     public String getPayId() {
     return payId;
     }

     public void setPayId(String payId) {
     this.payId = payId;
     }
     */

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @XmlTransient
    public Float getCommission() {
        return commission;
    }

    public void setCommission(Float commission) {
        this.commission = commission;
    }

}
