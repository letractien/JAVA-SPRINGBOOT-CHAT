package com.example.hcm25_cpl_ks_java_01_lms.chat;

public class WebRTCSignal {
    private String type;
    private String data;

    public WebRTCSignal() {}

    public WebRTCSignal(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
