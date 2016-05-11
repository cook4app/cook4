package com.beppeben.cook4.utils;

import android.content.res.Resources;

import com.beppeben.cook4.R;

import org.joda.time.DateTime;
import org.joda.time.Period;


public class StringUtils {

    static final int NAMESIZE = 13;

    public static String formatName(String name) {
        return formatName(name, NAMESIZE);
    }

    public static String formatName(String name, int size) {
        if (name.toCharArray().length <= size) return name;
        return name.substring(0, size - 1);
    }

    public static String getLabel(float score, float[] levels, String[] labels) {
        String result = labels[0];
        int counter = 0;
        while (score >= levels[counter++]) {
            result = labels[counter];
            if (counter == levels.length) break;
        }
        return result;
    }


    public static String periodString(Resources ctx, Period period, boolean sum) {
        Integer months = period.getMonths();
        Integer days = period.getDays();
        Integer hours = period.getHours();
        Integer minutes = period.getMinutes();

        String smonths = "";
        if (months != 0) {
            if (months > 1) {
                smonths = months + " " + ctx.getString(R.string.months);
            } else {
                smonths = months + " " + ctx.getString(R.string.month);
            }
            if (sum) return smonths;
            smonths += ", ";
        }

        String sdays = "";
        if (days != 0) {
            if (days > 1) {
                sdays = days + " " + ctx.getString(R.string.days);
            } else {
                sdays = days + " " + ctx.getString(R.string.day);
            }
            if (sum) return sdays;
            sdays += ", ";
        }

        String shours = "";
        if (hours != 0) {
            if (hours > 1) {
                shours = hours + " " + ctx.getString(R.string.hours);
            } else {
                shours = hours + " " + ctx.getString(R.string.hour);
            }
            if (sum) return shours;
            shours += ", ";
        }

        String sminutes = "";
        if (minutes != 0) {
            if (minutes > 1) {
                sminutes = minutes + " " + ctx.getString(R.string.minutes);
            } else {
                sminutes = minutes + " " + ctx.getString(R.string.minute);
            }
            if (sum) return sminutes;
            sminutes += " ";
        }

        return smonths + sdays + shours + sminutes;
    }

    public static String formatTime(int hour, int minute) {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute);
    }

    public static String formatDate(Resources ctx, int day, int month, int year, boolean writeyear) {
        DateTime today = new DateTime();
        DateTime tomorrow = today.plusDays(1);
        DateTime yesterday = today.minusDays(1);
        if (today.getDayOfMonth() == day && today.getMonthOfYear() == month && today.getYear() == year)
            return ctx.getString(R.string.today);
        if (tomorrow.getDayOfMonth() == day && tomorrow.getMonthOfYear() == month && tomorrow.getYear() == year)
            return ctx.getString(R.string.tomorrow);
        if (yesterday.getDayOfMonth() == day && yesterday.getMonthOfYear() == month && yesterday.getYear() == year)
            return ctx.getString(R.string.yesterday);
        String resultString = String.format("%02d", day) + "/" + String.format("%02d", month);
        if (writeyear) resultString += "/" + String.format("%02d", year);
        return resultString;
    }

    public static String formatDate(int day, int month) {
        return String.format("%02d", day) + "/" + String.format("%02d", month);
    }

    public static String format(Resources ctx, DateTime date) {
        return formatDate(ctx, date.getDayOfMonth(), date.getMonthOfYear(), date.getYear(), false)
                + ", " + formatTime(date.getHourOfDay(), date.getMinuteOfHour());
    }

    public static String formatWithPrep(Resources ctx, DateTime date) {
        String dateString = formatDate(ctx, date.getDayOfMonth(), date.getMonthOfYear(), date.getYear(), false);
        String prep = ctx.getString(R.string.on) + " ";
        String today = ctx.getString(R.string.today);
        String yesterday = ctx.getString(R.string.yesterday);
        String tomorrow = ctx.getString(R.string.tomorrow);
        if (dateString.equals(today) || dateString.equals(yesterday) || dateString.equals(tomorrow))
            prep = "";
        return prep + dateString
                + " " + ctx.getString(R.string.at_hour) + " " + formatTime(date.getHourOfDay(), date.getMinuteOfHour());
    }

    public static String formatFloat(Float f) {
        return String.format("%.2f", f).replaceAll("(\\.|\\,)?0*$", "");
    }

}
