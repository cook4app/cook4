package com.beppeben.cook4.domain;

import com.beppeben.cook4.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class C4Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private C4User cook;
    private C4Dish dish;
    private Float priceDel;
    private Float priceNoDel;
    private Double latitude;
    private Double longitude;
    private String address;
    private String addressDetails;
    private String city;
    private Date startDate;
    private Date endDate;
    private List<Boolean> daysAvailable;
    private Boolean oneoff;
    private Long id;
    private Integer portions;
    private Integer minNotice;
    private Integer maxDist;
    private List<String> portionsOrdered;
    private String delCurrency;
    private String noDelCurrency;
    private Date sDate;
    private Integer sPortions;
    private String timeZone;
    private List<C4DeliveryPoint> delpoints;


    public C4Item() {
    }

    public String getDelCurrency() {
        return delCurrency;
    }

    public void setDelCurrency(String delCurrency) {
        this.delCurrency = delCurrency;
    }

    public String getNoDelCurrency() {
        return noDelCurrency;
    }

    public void setNoDelCurrency(String noDelCurrency) {
        this.noDelCurrency = noDelCurrency;
    }

    public C4User getCook() {
        return cook;
    }

    public void setCook(C4User cook) {
        this.cook = cook;
    }

    public C4Dish getDish() {
        return dish;
    }

    public void setDish(C4Dish dish) {
        this.dish = dish;
    }

    public Float getPriceDel() {
        return priceDel;
    }

    public void setPriceDel(Float priceDel) {
        this.priceDel = priceDel;
    }

    public Float getPriceNoDel() {
        return priceNoDel;
    }

    public void setPriceNoDel(Float priceNoDel) {
        this.priceNoDel = priceNoDel;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Boolean> getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(List<Boolean> daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    public Boolean getOneoff() {
        return oneoff;
    }

    public void setOneoff(Boolean oneoff) {
        this.oneoff = oneoff;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPortions() {
        return portions;
    }

    public void setPortions(Integer portions) {
        this.portions = portions;
    }

    public Integer getMinNotice() {
        return minNotice;
    }

    public void setMinNotice(Integer minNotice) {
        this.minNotice = minNotice;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getMaxDist() {
        return maxDist;
    }

    public void setMaxDist(Integer maxDist) {
        this.maxDist = maxDist;
    }

    public Date getsDate() {
        return sDate;
    }

    public void setsDate(Date sDate) {
        this.sDate = sDate;
    }

    public Integer getsPortions() {
        return sPortions;
    }

    public void setsPortions(Integer sPortions) {
        this.sPortions = sPortions;
    }

    public List<String> getPortionsOrdered() {
        return portionsOrdered;
    }

    public void setPortionsOrdered(List<String> portionsOrdered) {
        this.portionsOrdered = portionsOrdered;
    }

    public List<C4DeliveryPoint> getDelpoints() {
        return delpoints;
    }

    public void setDelpoints(List<C4DeliveryPoint> delpoints) {
        this.delpoints = delpoints;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    private DateTime nDayofWeekwithTime(DateTime today, int dayOfWeek, int hour, int minute) {
        try {
            if (today.getDayOfWeek() > dayOfWeek) {
                return today.plusWeeks(1).withDayOfWeek(dayOfWeek).withHourOfDay(hour).withMinuteOfHour(minute);
            } else if (today.getDayOfWeek() == dayOfWeek) {
                if (!today.isBefore(today.withHourOfDay(hour).withMinuteOfHour(minute).plusMinutes(1)))
                    today = today.plusWeeks(1);
            }
            return today.withDayOfWeek(dayOfWeek).withHourOfDay(hour).withMinuteOfHour(minute).withSecondOfMinute(0).withMillisOfSecond(0);
        } catch (IllegalArgumentException e) {
            return nDayofWeekwithTime(today, dayOfWeek, hour + 1, minute);
        }
    }

    private DateTime pDayofWeekwithTime(DateTime today, int dayOfWeek, int hour, int minute) {
        try {
            if (today.getDayOfWeek() < dayOfWeek) {
                return today.minusWeeks(1).withDayOfWeek(dayOfWeek).withHourOfDay(hour).withMinuteOfHour(minute);
            } else if (today.getDayOfWeek() == dayOfWeek) {
                if (today.isBefore(today.withHourOfDay(hour).withMinuteOfHour(minute)))
                    today = today.minusWeeks(1);
            }
            return today.withDayOfWeek(dayOfWeek).withHourOfDay(hour).withMinuteOfHour(minute).withSecondOfMinute(0).withMillisOfSecond(0);
        } catch (IllegalArgumentException e) {
            return pDayofWeekwithTime(today, dayOfWeek, hour + 1, minute);
        }
    }


    private DateTime dayOfWeekWithTimeZone(DateTime today, int dayOfWeek, boolean next) {
        DateTime jstartDate = new DateTime(startDate);
        if (timeZone != null) {
            today = today.withZone(DateTimeZone.forID(timeZone));
            jstartDate = jstartDate.withZone(DateTimeZone.forID(timeZone));
        } else {
            //default to european time zone
            today = today.withZone(DateTimeZone.forID("Europe/Rome"));
            jstartDate = jstartDate.withZone(DateTimeZone.forID("Europe/Rome"));
        }
        int hour = jstartDate.getHourOfDay();
        int minute = jstartDate.getMinuteOfHour();
        DateTime result;
        if (next) result = nDayofWeekwithTime(jstartDate.isAfter(today) ?
                jstartDate : today, dayOfWeek, hour, minute);
        else result = pDayofWeekwithTime(today, dayOfWeek, hour, minute);
        return result.withZone(DateTimeZone.getDefault());
    }

    private DateTime nextDayofWeekwithTime(DateTime today, int dayOfWeek) {
        return dayOfWeekWithTimeZone(today, dayOfWeek, true);
    }

    private DateTime previousDayofWeekwithTime(DateTime today, int dayOfWeek) {
        return dayOfWeekWithTimeZone(today, dayOfWeek, false);
    }


    public DateTime nextOffer(DateTime date) {
        DateTime jstartDate = new DateTime(startDate);
        DateTime jendDate = new DateTime(endDate);
        if (oneoff) {
            return date.isBefore(jstartDate) ? jstartDate : null;
        }
        DateTime next = date.plusYears(10);
        int sum = 0;
        for (int i = 0; i < daysAvailable.size(); i++) {
            boolean avtoday = daysAvailable.get(i);
            if (avtoday) {
                DateTime temp = nextDayofWeekwithTime(date, i + 1);
                if (temp.isBefore(next)) {
                    next = temp;
                    sum++;
                }
            }
        }
        if (sum == 0) return null;
        if (next.isAfter(jendDate)) next = null;
        return next;
    }


    public DateTime previousOffer(DateTime date) {
        DateTime jstartDate = new DateTime(startDate);
        if (oneoff) {
            return date.isAfter(jstartDate) ? jstartDate : null;
        }
        DateTime previous = new DateTime();
        int sum = 0;
        for (int i = 0; i < daysAvailable.size(); i++) {
            boolean avtoday = daysAvailable.get(i);
            if (avtoday) {
                DateTime temp = previousDayofWeekwithTime(date, i + 1);
                if (temp.isAfter(previous)) {
                    previous = temp;
                    sum++;
                }
            }
        }
        if (sum == 0) return null;
        if (previous.isBefore(jstartDate)) previous = null;
        return previous;
    }


    public DateTime nextAvailable(Double lat, Double lng, DateTime date) {
        DateTime next = null;
        DateTime today = new DateTime();
        while ((next = nextOffer(date)) != null && portionsLeft(next) <= 0) {
            date = next.plusHours(1);
        }
        if (next != null && minNotice != null && next.minusMinutes(minNotice).isBefore(today))
            return nextAvailable(lat, lng, next.plusHours(1));

        if (lat != null && lng != null && next != null && !attainable(lat, lng, next))
            return nextAvailable(lat, lng, next.plusHours(1));

        return next;
    }

    public DateTime previousAvailable(Double lat, Double lng, DateTime date) {
        DateTime previous = null;
        DateTime today = new DateTime();
        while ((previous = previousOffer(date)) != null && portionsLeft(previous) <= 0) {
            date = previous.minusHours(1);
        }
        if (previous != null && minNotice != null && previous.minusMinutes(minNotice).isBefore(today))
            return null;

        if (lat != null && lng != null && previous != null && !attainable(lat, lng, previous))
            return previousAvailable(lat, lng, previous.minusHours(1));

        return previous;
    }

    public DateTime closestAvailable(Double lat, Double lng, DateTime date) {
        DateTime next = nextAvailable(lat, lng, date);
        DateTime previous = previousAvailable(lat, lng, date);
        if (previous != null && date.minusMinutes(30).isAfter(previous)) previous = null;
        if (next == null) return previous;
        if (previous == null) return next;
        if (Seconds.secondsBetween(previous, date).getSeconds()
                < Seconds.secondsBetween(date, next).getSeconds())
            return previous;
        else return next;
    }

    public int portionsLeft(DateTime date) {
        return portions - portionsOrdered(date);
    }

    public int portionsOrdered(DateTime date) {
        if (portionsOrdered == null) return 0;
        Integer port = 0;
        date = date.withZone(DateTimeZone.forID(timeZone));
        String key = "" + date.getDayOfMonth() + date.getMonthOfYear() + date.getYear();
        for (String pair : portionsOrdered) {
            String[] parts = pair.split("/");
            if (parts[0].equals(key)) {
                port = Integer.parseInt(parts[1]);
                break;
            }
        }
        return port;
    }


    public DeliveryQuote bestExternalDelivery(Double lat, Double lng, DateTime date, Integer portions) {
        DeliveryQuote result = new DeliveryQuote();
        Float price = null;
        Long id = null;
        if (date == null) return result;
        date = date.withZone(DateTimeZone.forID(timeZone));
        if (priceNoDel != null && delpoints != null) {
            for (int i = 0; i < delpoints.size(); i++) {
                C4DeliveryPoint point = delpoints.get(i);
                if (!point.getCurrency().equals(noDelCurrency)) continue;
                if (Utils.distFrom(point.getLatitude(), point.getLongitude(), lat, lng) < point.getRadius()
                        && Utils.distFrom(point.getLatitude(), point.getLongitude(), latitude, longitude) < point.getRadius()
                        && point.getDaysAvailable().get(date.getDayOfWeek() - 1)) {
                    boolean timesuccess = false;
                    for (C4DeliveryPoint.MyPeriod p : point.getPeriods()) {
                        DateTime from = date.withHourOfDay(p.getFromhour()).withMinuteOfHour(p.getFromminute());
                        DateTime to = date.withHourOfDay(p.getTohour()).withMinuteOfHour(p.getTominute());
                        if (date.isAfter(from) && date.isBefore(to)) {
                            timesuccess = true;
                            break;
                        }
                    }
                    if (!timesuccess) continue;
                    Float cand = priceNoDel * portions + point.getPrice();
                    if (price == null || cand < price) {
                        price = cand;
                        id = point.getId();
                    }
                }
            }
        }
        result.price = price;
        result.currency = noDelCurrency;
        result.id = id;
        return result;
    }

    public DeliveryQuote bestDelivery(Double lat, Double lng, DateTime date, Integer portions) {
        DeliveryQuote ext = bestExternalDelivery(lat, lng, date, portions);
        DeliveryQuote loc = new DeliveryQuote();
        if (priceDel != null && Utils.distFrom(latitude, longitude, lat, lng) < (float) maxDist / 1000) {
            loc.price = priceDel * portions;
            loc.currency = delCurrency;
        }
        if (loc.price == null) return ext;
        if (ext.price == null) return loc;
        if (loc.price < ext.price) return loc;
        else return ext;
    }

    public class DeliveryQuote {
        public Float price;
        public String currency;
        public Long id;

        public DeliveryQuote() {
        }

        public DeliveryQuote(Float price, String currency, Long id) {
            this.price = price;
            this.currency = currency;
            this.id = id;
        }
    }

    public boolean attainable(Double lat, Double lng, DateTime date) {
        if (priceNoDel != null) return true;
        if (priceDel != null &&
                Utils.distFrom(latitude, longitude, lat, lng) < (float) maxDist / 1000)
            return true;
        if (priceDel != null &&
                lat != null && lng != null && bestExternalDelivery(lat, lng, date, 1).price != null)
            return true;
        return false;
    }

}
