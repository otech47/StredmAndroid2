package com.stredm.android;

public class Payload {

    public Model[] featured;

    public Payload(Model[] featured) {
        this.featured = featured;
    }

    public Model[] getFeatured() {
        return featured;
    }
}
