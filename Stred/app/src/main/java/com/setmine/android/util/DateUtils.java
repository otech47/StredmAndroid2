package com.setmine.android.util;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {


    public String convertDateToDaysAgo(String dateString) {
        DateTime date = new DateTime(dateString);
        DateTime today = new DateTime();

        int days = Days.daysBetween(new DateTime(date), new DateTime(today)).getDays();
        return Integer.toString(days) + "d ago";
    }

    public Date stringToDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        try {
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String formatDateText(String startDateString, String endDateString) {
        Date startDate = stringToDate(startDateString);
        Date endDate = stringToDate(endDateString);
        String formattedDateString;
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM' 'd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("M");
        SimpleDateFormat yearFormat = new SimpleDateFormat("y");
        SimpleDateFormat dayFormat = new SimpleDateFormat("d");
        String firstDayMonth = monthFormat.format(startDate);
        String lastDayMonth = monthFormat.format(endDate);
        String yearString = yearFormat.format(startDate);
        String firstDayString = monthDayFormat.format(startDate);
        String lastDayString = dayFormat.format(endDate);
        if(dayFormat.format(startDate).equals(lastDayString)) {
            formattedDateString = firstDayString + ", " + yearString;
        }
        else if(firstDayMonth.equals(lastDayMonth)) {
            formattedDateString = firstDayString + "-" + lastDayString + ", " + yearString;
        }
        else {
            lastDayString = monthDayFormat.format(endDate);
            formattedDateString = firstDayString + " - " + lastDayString + ", " + yearString;
        }
        return formattedDateString;
    }

    public String formatAddressForDetail(String address) {
        return address;
    }

    public String getCityStateFromAddress(String address) {
        try {
            int comma = address.lastIndexOf(",");
            String country = address.substring(comma+2);
            String cityState = address.substring(0, comma);
            comma = cityState.lastIndexOf(",");
            cityState = address.substring(0, comma);
            comma = cityState.lastIndexOf(",");
            if(comma == -1)
                cityState = address.substring(0, cityState.length()+4);
            else {
                cityState = address.substring(comma+2, cityState.length()+4);
            }
            return cityState;
        } catch(IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return "Location Not Available";
    }

    public String getDayFromDate(String date, Integer day) {
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("E");
        Date startDate = stringToDate(date);
        String dayOfWeek = dayOfWeekFormat.format(startDate);
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DAY_OF_MONTH, day - 1);
        return dayOfWeekFormat.format(c.getTime());

    }

}