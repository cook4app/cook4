package com.beppeben.cook4server.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class StringUtils {

    public static String formatTime(int hour, int minute) {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute);
    }

    public static String formatDate(int day, int month, int year) {
        return String.format("%02d", day) + "/" + String.format("%02d", month)
                + "/" + String.format("%02d", year);
    }

    public static String format(DateTime date, String timeZone) {
        if (timeZone != null) {
            date = date.withZone(DateTimeZone.forID(timeZone));
        }
        return formatDate(date.getDayOfMonth(), date.getMonthOfYear(), date.getYear())
                + ", " + formatTime(date.getHourOfDay(), date.getMinuteOfHour());
    }
}
