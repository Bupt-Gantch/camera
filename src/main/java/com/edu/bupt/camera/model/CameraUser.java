package com.edu.bupt.camera.model;

import java.util.Date;

public class CameraUser {
    private Integer customerId;

    private String appkey;

    private String appsecret;

    private String accesstoken;

    private Date timestamp;

//    private Integer authority;

    public CameraUser(Integer customerId, String appkey,
                      String appsecret, String accesstoken, Date timestamp) {
        this.customerId = customerId;
        this.appkey = appkey;
        this.appsecret = appsecret;
        this.accesstoken = accesstoken;
        this.timestamp = timestamp;

    }

    public CameraUser() {
        super();
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public String getAppsecret() {
        return appsecret;
    }

    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret == null ? null : appsecret.trim();
    }

    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken == null ? null : accesstoken.trim();
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp){
        this.timestamp = timestamp;
    }

}