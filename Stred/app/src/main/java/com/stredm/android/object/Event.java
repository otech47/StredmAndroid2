package com.stredm.android.object;


public class Event {
	private String id;
	private String event;
    private String bio;
    private String facebookLink;
    private String twitterLink;
    private String iconImageUrl;
    private String mainImageUrl;
    private String startDate;
    private String endDate;
    private int days;
    private String venue;
    private double latitude;
    private double longitude;
    private String address;

    public Event() {}
    public Event(String id, String event, String bio,
                 String facebookLink, String twitterLink, String iconImageUrl,
                 String mainImageUrl, String startDate, String endDate,
                 int days, String venue, double latitude, double longitude,
                 String address) {
        this.id = id;
        this.event = event;
        this.bio = bio;
        this.facebookLink = facebookLink;
        this.twitterLink = twitterLink;
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