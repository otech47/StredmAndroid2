package com.stredm.android;

public class ApiResponse {
    public int version;
    public String status;
    public Payload payload;

    public ApiResponse(int version, String status, Payload payload) {
        this.version = version;
        this.status = status;
        this.payload = payload;
    }

    public Payload getPayload() {
        return payload;
    }

}
