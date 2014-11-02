package com.setmine.android.object;


import org.json.JSONException;
import org.json.JSONObject;

public class Event {
	public String id;
	public String event;
    public String bio;
    public String facebookLink;
    public String twitterLink;
    public String webLink;
    public String ticketLink;
    public String iconImageUrl;
    public String mainImageUrl;
    public String startDate;
    public String endDate;
    public int paid;
    public int days;
    public String venue;
    public double latitude;
    public double longitude;
    public String address;

    public Event() {}
    public Event(String id, String event, String bio,
                 String facebookLink, String twitterLink, String webLink, String iconImageUrl,
                 String mainImageUrl, String startDate, String endDate,
                 int days, String venue, double latitude, double longitude,
                 String address) {
        this.id = id;
        this.event = event;
        this.bio = bio;
        this.facebookLink = facebookLink;
        this.twitterLink = twitterLink;
        this.webLink = webLink;

        this.iconImageUrl = iconImageUrl;
        this.mainImageUrl = mainImageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.venue = venue;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Event(JSONObject json) {
        try {
            setId(json.getString("id"));
            setEvent(json.getString("event"));
            setBio(json.getString("bio"));
            setFacebookLink(json.getString("fb_link"));
            setTwitterLink(json.getString("twitter_link"));
            setIconImageUrl(json.getString("imageURL"));
            setWebLink(json.getString("web_link"));
            setMainImageUrl(json.getString("main_imageURL"));
            setStartDate(json.getString("start_date"));
            setEndDate(json.getString("end_date"));
            setDays(json.getInt("days"));
            setVenue(json.getString("venue"));
            setLatitude(json.getDouble("latitude"));
            setLongitude(json.getDouble("longitude"));
            setAddress(json.getString("address"));
            setPaid(json.getInt("paid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTicketLink() {
        return ticketLink;
    }

    public void setTicketLink(String ticketLink) {
        this.ticketLink = ticketLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFacebookLink() {
        return facebookLink;
    }

    public void setFacebookLink(String facebookLink) {
        this.facebookLink = facebookLink;
    }

    public String getTwitterLink() {
        return twitterLink;
    }

    public void setTwitterLink(String twitterLink) {
        this.twitterLink = twitterLink;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public String getIconImageUrl() {
        return iconImageUrl;
    }

    public void setIconImageUrl(String iconImageUrl) {
        this.iconImageUrl = iconImageUrl;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}