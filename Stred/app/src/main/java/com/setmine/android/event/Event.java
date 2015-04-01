package com.setmine.android.event;


import android.os.Parcel;
import android.os.Parcelable;

import com.setmine.android.api.JSONModel;
import com.setmine.android.Constants;
import com.setmine.android.util.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Event extends JSONModel implements Parcelable {

	public String id;
	public String event;
    public String bio;
    public String facebookLink;
    public String twitterLink;
    public String webLink;
    public String ticketLink = "No Ticket Link";
    public String iconImageUrl;
    public String mainImageUrl;
    public String startDate;
    public String endDate;
    public String dateFormatted;
    public int paid;
    public int days;
    public String venue;
    public double latitude;
    public double longitude;
    public String address;

    // Parcelable Implementation

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    private Event(Parcel in) {
        id = in.readString();
        event = in.readString();
        bio = in.readString();
        facebookLink = in.readString();
        twitterLink = in.readString();
        webLink = in.readString();
        ticketLink = in.readString();
        iconImageUrl = in.readString();
        mainImageUrl = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        dateFormatted = in.readString();
        paid = in.readInt();
        days = in.readInt();
        venue = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
    }


    @Override
    public int describeContents() {
        return Integer.parseInt(getId());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(event);
        out.writeString(bio);
        out.writeString(facebookLink);
        out.writeString(twitterLink);
        out.writeString(webLink);
        out.writeString(ticketLink);
        out.writeString(iconImageUrl);
        out.writeString(mainImageUrl);
        out.writeString(startDate);
        out.writeString(endDate);
        out.writeString(dateFormatted);
        out.writeInt(paid);
        out.writeInt(days);
        out.writeString(venue);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeString(address);

    }

    // Object Creation and Method Definitions

    public Event() {}

    public Event(JSONObject json) {
        jsonModelString = json.toString();

        try {
            setId(json.getString("id"));
            setEvent(json.getString("event"));
            setBio(json.getString("bio"));
            setFacebookLink(json.getString("fb_link"));
            setTwitterLink(json.getString("twitter_link"));
            setIconImageUrl(Constants.S3_ROOT_URL + json.getString("imageURL"));
            setWebLink(json.getString("web_link"));
            setMainImageUrl(Constants.S3_ROOT_URL + json.getString("main_imageURL"));
            setStartDate(json.getString("start_date"));
            setEndDate(json.getString("end_date"));
            setDateFormatted();
            setDays(json.getInt("days"));
            setVenue(json.getString("venue"));
            setLatitude(json.getDouble("latitude"));
            setLongitude(json.getDouble("longitude"));
            setAddress(json.getString("address"));
            setPaid(json.getInt("paid"));
            if(json.has("ticket_link")) {
                setTicketLink(json.getString("ticket_link"));
            }
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

    public String getDateFormatted() {
        return dateFormatted;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setDateFormatted() {
        DateUtils dateUtils = new DateUtils();
        this.dateFormatted = dateUtils.formatDateText(this.startDate, this.endDate);
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