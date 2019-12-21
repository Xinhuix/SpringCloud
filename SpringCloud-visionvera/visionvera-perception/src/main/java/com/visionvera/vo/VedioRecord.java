package com.visionvera.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Administrator
 * @date 2018年11月23日 15:00
 */
public class VedioRecord implements Serializable {
    private String id;

    private String uniqId;

    private String virtualNo;

    private String svrName;

    private String svrAddr;

    private Date time;

    private String userName;

    private String vedioName;

    private int recordType;

    private String recordContent;

    private int businessMode;

    private int businessType;

    private int businessState;

    private int platformType;

    private String platformId;


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setUniqId(String uniqId) {
        this.uniqId = uniqId;
    }

    public String getUniqId() {
        return this.uniqId;
    }

    public void setVirtualNo(String virtualNo) {
        this.virtualNo = virtualNo;
    }

    public String getVirtualNo() {
        return this.virtualNo;
    }

    public void setSvrName(String svrName) {
        this.svrName = svrName;
    }

    public String getSvrName() {
        return this.svrName;
    }

    public void setSvrAddr(String svrAddr) {
        this.svrAddr = svrAddr;
    }

    public String getSvrAddr() {
        return this.svrAddr;
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

    public void setVedioName(String vedioName) {
        this.vedioName = vedioName;
    }

    public String getVedioName() {
        return this.vedioName;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }

    public int getRecordType() {
        return this.recordType;
    }

    public void setRecordContent(String recordContent) {
        this.recordContent = recordContent;
    }

    public String getRecordContent() {
        return this.recordContent;
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
