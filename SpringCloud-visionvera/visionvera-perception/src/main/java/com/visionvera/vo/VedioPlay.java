package com.visionvera.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Administrator
 * @date 2018年11月23日 15:03
 */
public class VedioPlay implements Serializable {

    private String uniqId;
    private String vedioName;
    private String devNo;
    private String devMac;
    private Date time;
    private String userName;
    private int businessMode;
    private int businessType;
    private int businessState;
    private int platformType;

    private String platformId;
    public void setUniqId(String uniqId) {
        this.uniqId = uniqId;
    }
    public String getUniqId() {
        return uniqId;
    }

    public void setVedioName(String vedioName) {
        this.vedioName = vedioName;
    }
    public String getVedioName() {
        return vedioName;
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

    public int getPlatformType() {
        return platformType;
    }

    public void setPlatformType(int platformType) {
        this.platformType = platformType;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }
}
