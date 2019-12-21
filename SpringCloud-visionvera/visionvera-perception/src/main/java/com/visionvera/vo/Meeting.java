package com.visionvera.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Administrator
 * @date 2018年11月23日 15:04
 */
public class Meeting implements Serializable {

    private String meetName;
    private String devNo;
    private String devMac;
    private String devs;
    private Date time;
    private String userName;
    private int businessMode;
    private int businessType;
    private int businessState;
    public void setMeetName(String meetName) {
        this.meetName = meetName;
    }
    public String getMeetName() {
        return meetName;
    }

    public void setDevNo(String devNo) {
        this.devNo = devNo;
    }
    public String getDevNo() {
        return devNo;
    }

    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }
    public String getDevMac() {
        return devMac;
    }

    public void setDevs(String devs) {
        this.devs = devs;
    }
    public String getDevs() {
        return devs;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    public Date getTime() {
        return time;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserName() {
        return userName;
    }

    public void setBusinessMode(int businessMode) {
        this.businessMode = businessMode;
    }
    public int getBusinessMode() {
        return businessMode;
    }

    public void setBusinessType(int businessType) {
        this.businessType = businessType;
    }
    public int getBusinessType() {
        return businessType;
    }

    public void setBusinessState(int businessState) {
        this.businessState = businessState;
    }
    public int getBusinessState() {
        return businessState;
    }
}
