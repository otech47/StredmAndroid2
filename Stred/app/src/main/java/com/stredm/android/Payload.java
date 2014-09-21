package com.stredm.android;

public class Payload<T> {

    public String modelName;
    public T model;

    public Payload(T data, String type) {
        this.modelName = type;
        this.model = data;
    }

    public T getModel() {
        return model;
    }

}
