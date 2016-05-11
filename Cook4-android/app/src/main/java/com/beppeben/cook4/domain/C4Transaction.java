package com.beppeben.cook4.domain;

import com.beppeben.cook4.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private Date date;
    private Integer portions;
    private Double latitude;
    private Double longitude;
    private Long id;
    private Long itemId;
    private Float cookRating;
    private Float foodieRating;
    private Float foodRating;
    private Long twinTransId;
    private C4Transaction twinTransaction;
    private String currency;
    private String phone;
    private Float totalPrice;
    private String extDelInfo;
    private Long deliveryId;


    public C4Transaction() {
    }


    public static C4Transaction swapToTrans(C4SwapProposal swap, boolean wepropose) {
        C4Transaction trans = new C4Transaction();
        if (wepropose) {
            trans.setCookId(swap.getToCookId());
            trans.setCookName(swap.getToCookName());
            trans.setFoodieId(swap.getFromCookId());
            trans.setFoodieName(swap.getFromCookName());
            trans.setPortions(swap.getTargetDishPortions());
            trans.setDelivery(swap.getToCookDelivers());
            trans.setDishId(swap.getTargetDishId());
            trans.setDishName(swap.getTargetDishName());
        } else {
            trans.setCookId(swap.getFromCookId());
            trans.setCookName(swap.getFromCookName());
            trans.setFoodieId(swap.getToCookId());
            trans.setFoodieName(swap.getToCookName());
            trans.setPortions(swap.getRewardDishPortions());
            trans.setDelivery(!swap.getToCookDelivers());
            trans.setDishId(swap.getRewardDishId());
            trans.setDishName(swap.getRewardDishName());
        }
        trans.setDeliveryAddress(swap.getDeliveryAddress());
        trans.setAddressDetails(swap.getAddressDetails());
        trans.setDate(swap.getDate());
        trans.setItemId(swap.getItemId());
        trans.setLatitude(swap.getLatitude());
        trans.setLongitude(swap.getLongitude());
        trans.setPrice(0F);

        return trans;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getTwinTransId() {
        return twinTransId;
    }

    public void setTwinTransId(Long twinTransId) {
        this.twinTransId = twinTransId;
    }

    public double score(final DateTime refdate, final Double latitude, final Double longitude) {
        DateTime _date = new DateTime(date);
        long min = Math.abs(Seconds.secondsBetween(refdate, _date).getSeconds()) / 60;
        double score = min / 10;
        if (latitude != null && longitude != null) {
            score -= Utils.distFrom(latitude, longitude, getLatitude(), getLongitude());
        }
        return score;
    }

    public static Comparator<C4Transaction> compareByDatePosition
            (final DateTime refdate, final Double latitude, final Double longitude) {
        return new Comparator<C4Transaction>() {

            @Override
            public int compare(C4Transaction t1, C4Transaction t2) {
                boolean result = t1.score(refdate, latitude, longitude)
                        > t2.score(refdate, latitude, longitude);
                return result ? 1 : -1;
            }

        };
    }

    @JsonIgnore
    public C4Transaction getTwinTransaction() {
        return twinTransaction;
    }

    public void setTwinTransaction(C4Transaction twinTransaction) {
        this.twinTransaction = twinTransaction;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

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
}
