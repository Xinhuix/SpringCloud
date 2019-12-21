package com.visionvera.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Administrator
 * @date 2018年11月23日 14:44
 */
public class Vphone implements Serializable {


    private String srcNo;
    private String srcMac;
    private String dstNo;
    private String dstMac;
    private Date time;
    private String userName;
    private int businessMode;
    private int businessType;
    private int businessState;

    public void setSrcNo(String srcNo) {
        this.srcNo = srcNo;
    }

    public String getSrcNo() {
        return srcNo;
    }

    public void setSrcMac(String srcMac) {
        this.srcMac = srcMac;
    }

    public String getSrcMac() {
        return srcMac;
    }

    public void setDstNo(String dstNo) {
        this.dstNo = dstNo;
    }

    public String getDstNo() {
        return dstNo;
    }

    public void setDstMac(String dstMac) {
        this.dstMac = dstMac;
    }

    public String getDstMac() {
        return dstMac;
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
