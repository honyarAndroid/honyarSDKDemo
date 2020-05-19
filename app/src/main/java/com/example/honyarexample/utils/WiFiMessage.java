package com.example.honyarexample.utils;

import java.io.Serializable;

/**
 * Created by kiss on 2017/11/11.
 */

public class WiFiMessage implements Serializable{
    private String WIFI_SSID;
    private String WIFI_PassWD;

    public String getWIFI_SSID() {
        return WIFI_SSID;
    }

    public void setWIFI_SSID(String WIFI_SSID) {
        this.WIFI_SSID = WIFI_SSID;
    }

    public String getWIFI_PassWD() {
        return WIFI_PassWD;
    }

    public void setWIFI_PassWD(String WIFI_PassWD) {
        this.WIFI_PassWD = WIFI_PassWD;
    }
}
