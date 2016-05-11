package com.beppeben.cook4server.domain;

import com.beppeben.cook4server.utils.DateAdapter;
import com.beppeben.cook4server.utils.StringUtils;
import java.io.Serializable;
import java.util.Date;
import static javax.persistence.CascadeType.ALL;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.joda.time.DateTime;

@Entity
@Table(name = "cook4_transaction", schema = "APP")
@XmlRootElement
public class C4Transaction extends C4Entity implements Serializable {

    private String dishName;
    private Long dishId;
    private C4Dish dish;
    private Long cookId;
    private Long foodieId;
    private C4User cook;
    private C4User foodie;
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
    private C4Transaction twinTransaction;
    private Long twinTransId;
    private String currency;
    private String phone;
    private Float totalPrice;
    private String extDelInfo;
    private Long deliveryId;
    private C4DelayedPayment delayedPayment;
    private String payKey;
    private String timeZone;
    private Float commission;

    public C4Transaction() {
    }

    public C4Transaction(C4TempTransaction temp) {
        dishName = temp.getDish().getName();
        dishId = temp.getDish().getId();
        dish = temp.getDish();
        cookId = temp.getCook().getId();
        foodieId = temp.getFoodie().getId();
        cook = temp.getCook();
        foodie = temp.getFoodie();
        cookName = temp.getCook().getName();
        foodieName = temp.getFoodie().getName();
        delivery = temp.getDelivery();
        deliveryAddress = temp.getDeliveryAddress();
        addressDetails = temp.getAddressDetails();
        price = temp.getPrice();
        date = temp.getDate();
        portions = temp.getPortions();
        latitude = temp.getLatitude();
        longitude = temp.getLongitude();
        itemId = temp.getItemId();
        currency = temp.getCurrency();
        phone = temp.getPhone();
        totalPrice = temp.getTotalPrice();
        extDelInfo = temp.getExtDelInfo();
        deliveryId = temp.getDeliveryId();
        payKey = temp.getPayKey();
        //payId = temp.getPayId();
        timeZone = temp.getTimeZone();
        commission = temp.getCommission();
    }

    @Id
    @GeneratedValue
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

    @Transient
    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    @Transient
    public Long getCookId() {
        return cookId;
    }

    public void setCookId(Long cookId) {
        this.cookId = cookId;
    }

    @Transient
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

    @XmlTransient
    @ManyToOne
    public C4User getCook() {
        return cook;
    }

    public void setCook(C4User cook) {
        this.cook = cook;
    }

    @XmlTransient
    @ManyToOne
    public C4User getFoodie() {
        return foodie;
    }

    public void setFoodie(C4User foodie) {
        this.foodie = foodie;
    }

    @XmlTransient
    public C4Dish getDish() {
        return dish;
    }

    public void setDish(C4Dish dish) {
        this.dish = dish;
    }

    @XmlTransient
    public Float getCookRating() {
        return cookRating;
    }

    public void setCookRating(Float cookRating) {
        this.cookRating = cookRating;
    }

    @XmlTransient
    public Float getFoodieRating() {
        return foodieRating;
    }

    public void setFoodieRating(Float foodieRating) {
        this.foodieRating = foodieRating;
    }

    @XmlTransient
    public Float getFoodRating() {
        return foodRating;
    }

    public void setFoodRating(Float foodRating) {
        this.foodRating = foodRating;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = ALL)
    @XmlTransient
    public C4Transaction getTwinTransaction() {
        return twinTransaction;
    }

    public void setTwinTransaction(C4Transaction twinTransaction) {
        this.twinTransaction = twinTransaction;
    }

    @Transient
    public Long getTwinTransId() {
        return twinTransId;
    }

    public void setTwinTransId(Long twinTransactionId) {
        this.twinTransId = twinTransactionId;
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
    @OneToOne
    public C4DelayedPayment getDelayedPayment() {
        return delayedPayment;
    }

    public void setDelayedPayment(C4DelayedPayment delayedPayment) {
        this.delayedPayment = delayedPayment;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String formatDate() {
        return StringUtils.format(new DateTime(date), timeZone);
    }

    @XmlTransient
    public Float getCommission() {
        return commission;
    }

    public void setCommission(Float commission) {
        this.commission = commission;
    }
}
