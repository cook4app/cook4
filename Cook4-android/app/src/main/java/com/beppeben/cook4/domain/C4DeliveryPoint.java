package com.beppeben.cook4.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4DeliveryPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Double latitude;
    private Double longitude;
    private Float radius;
    private Float price;
    private String currency;
    private List<MyPeriod> periods;
    private List<Boolean> daysAvailable;

    public C4DeliveryPoint() {
        periods = new ArrayList<>();
        daysAvailable = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            daysAvailable.add(true);
        }
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
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


    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    public List<MyPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<MyPeriod> periods) {
        this.periods = periods;
    }

    public List<Boolean> getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(List<Boolean> daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public static class MyPeriod implements Serializable {

        private Integer fromhour;
        private Integer fromminute;
        private Integer tohour;
        private Integer tominute;


        public MyPeriod() {
        }

        public MyPeriod(int fromhour, int fromminute, int tohour, int tominute) {
            this.fromhour = fromhour;
            this.fromminute = fromminute;
            this.tohour = tohour;
            this.tominute = tominute;
        }

        @Override
        public String toString() {
            return "" + fromhour + ":" + fromminute + "-" + tohour + ":" + tominute;
        }

        public int getFromhour() {
            return fromhour;
        }

        public void setFromhour(int fromhour) {
            this.fromhour = fromhour;
        }

        public int getFromminute() {
            return fromminute;
        }

        public void setFromminute(int fromminute) {
            this.fromminute = fromminute;
        }

        public int getTohour() {
            return tohour;
        }

        public void setTohour(int tohour) {
            this.tohour = tohour;
        }

        public int getTominute() {
            return tominute;
        }

        public void setTominute(int tominute) {
            this.tominute = tominute;
        }

    }

}
