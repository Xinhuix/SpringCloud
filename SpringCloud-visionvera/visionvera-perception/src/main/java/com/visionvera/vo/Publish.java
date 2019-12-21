package com.visionvera.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Administrator
 * @date 2018年11月23日 14:45
 */
public class Publish implements Serializable {
    private String id;

    private String uniqId;

    private String msgNo;

    private String devNo;

    private String devMac;

    private String devName;

    private String svrName;

    private Date time;

    private String userName;

    private int businessMode;

    private int businessType;

    private int businessState;

    private int subscribeCount;

    private int platformType;

    private String platformId;

    private String monitorId;
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUniqId() {
        return uniqId;
    }

    public void setUniqId(String uniqId) {
        this.uniqId = uniqId;
    }

    public String getId() {
        return this.id;
    }

    public void setMsgNo(String msgNo) {
        this.msgNo = msgNo;
    }

    public String getMsgNo() {
        return this.msgNo;
    }

    public void setDevNo(String devNo) {
        this.devNo = devNo;
    }

    public String getDevNo() {
        return this.devNo;
    }

    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }

    public String getDevMac() {
        return this.devMac;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevName() {
        return this.devName;
    }

    public void setSvrName(String svrName) {
        this.svrName = svrName;
    }

    public String getSvrName() {
        return this.svrName;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    public void setBusinessMode(int businessMode) {
        this.businessMode = businessMode;
    }

    public int getBusinessMode() {
        return this.businessMode;
    }

    public void setBusinessType(int businessType) {
        this.businessType = businessType;
    }

    public int getBusinessType() {
        return this.businessType;
    }

    public void setBusinessState(int businessState) {
        this.businessState = businessState;
    }

    public int getBusinessState() {
        return this.businessState;
    }

    public void setSubscribeCount(int subscribeCount) {
        this.subscribeCount = subscribeCount;
    }

    public int getSubscribeCount() {
        return this.subscribeCount;
    }

    public void setPlatformType(int platformType) {
        this.platformType = platformType;
    }

    public int getPlatformType() {
        return this.platformType;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getPlatformId() {
        return this.platformId;
    }
}
