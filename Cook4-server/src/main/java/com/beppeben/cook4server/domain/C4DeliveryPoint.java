package com.beppeben.cook4server.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "delivery_point", schema = "APP")
@XmlRootElement
public class C4DeliveryPoint extends C4Entity implements Serializable {

    private Long id;
    private String name;
    private String email;
    private Double latitude;
    private Double longitude;
    private Float radius;
    private Float price;
    private String currency;
    private List<MyPeriod> periods;
    private List<Boolean> daysAvailable;
    private Boolean active = true;
    private String timeZone;

    private static final long serialVersionUID = 1L;

    public C4DeliveryPoint() {
        periods = new ArrayList<>();
        daysAvailable = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            daysAvailable.add(true);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + id);
        sb.append(", name: " + name);
        sb.append(", email; " + email);
        sb.append(", latitude: " + latitude);
        sb.append(", longitude: " + longitude);
        sb.append(", radius: " + radius);
        sb.append(", price: " + price);
        sb.append(", currency: " + currency);
        sb.append(", days available: ");
        if (daysAvailable.get(0)) {
            sb.append("Mon, ");
        }
        if (daysAvailable.get(1)) {
            sb.append("Tue, ");
        }
        if (daysAvailable.get(2)) {
            sb.append("Wed, ");
        }
        if (daysAvailable.get(3)) {
            sb.append("Thu, ");
        }
        if (daysAvailable.get(4)) {
            sb.append("Fri, ");
        }
        if (daysAvailable.get(5)) {
            sb.append("Sat, ");
        }
        if (daysAvailable.get(6)) {
            sb.append("Sun, ");
        }
        sb.append("periods: ");
        for (int i = 0; i < periods.size(); i++) {
            sb.append(periods.get(i).toString());
            sb.append(", ");
        }
        sb.append("active: " + active);
        return sb.toString();
    }

    public static String toString(List<C4DeliveryPoint> l) {
        String result = "";
        for (C4DeliveryPoint d : l) {
            result += d.toString() + "\n";
        }
        return result;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    @XmlTransient
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @XmlTransient
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotNull
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @NotNull
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @NotNull
    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    @NotNull
    public List<MyPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<MyPeriod> periods) {
        this.periods = periods;
    }

    @NotNull
    public List<Boolean> getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(List<Boolean> daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    @NotNull
    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    @NotNull
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @XmlTransient
    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @XmlTransient
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Embeddable
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

    public String daysFromInt(Integer days) {
        List<Boolean> daysAvailable = new ArrayList<>();
        while (daysAvailable.size() < 7) {
            daysAvailable.add(false);
        }
        for (int i = 0; i < 7; i++) {
            daysAvailable.set(6 - i, (days % 10) == 1);
            days /= 10;
        }
        setDaysAvailable(daysAvailable);
        return null;
    }

    public String periodsFromString(String periods) {
        List<MyPeriod> pds = new ArrayList<>();
        String[] intervals = periods.split(",");
        for (String interval : intervals) {
            String[] temp = interval.split("-");
            if (temp.length != 2) {
                return "bad period def";
            }
            String from = temp[0];
            String to = temp[1];

            temp = from.split(":");
            if (temp.length != 2) {
                return "bad time def";
            }
            int fromhour = Integer.parseInt(temp[0]);
            int fromminute = Integer.parseInt(temp[1]);

            temp = to.split(":");
            if (temp.length != 2) {
                return "bad time def";
            }
            int tohour = Integer.parseInt(temp[0]);
            int tominute = Integer.parseInt(temp[1]);

            MyPeriod per = new MyPeriod(fromhour, fromminute, tohour, tominute);
            pds.add(per);
        }
        setPeriods(pds);
        return null;
    }

}
