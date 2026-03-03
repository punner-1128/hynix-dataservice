package com.example.amos.service.dto;

public class ApikeyFilterRequest {

    private String service;
    private String from;
    private String to;

    public ApikeyFilterRequest() {
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
