package com.visionvera.vo;

import java.util.Date;

/**
 * @author Administrator
 * @date 2018年12月06日 11:00
 */
public class VphoneReportVo {
    /**
     * 业务唯一ID
     */
    private String uniqId;
    /**
     * 终端名称
     */
    private String devName;
    /**
     * 终端位置
     */
    private String devAddress;
    /**
     * 服务器名称
     */
    private String svrName;
    /**
     * 源终端号码
     */
    private String devNo;

    /**
     * 上报业务的平台类型：301 唐古拉； 401 流媒体web平台； 501 内容管理平台
     */
    private Integer platformType;
    /**
     * 上报业务的平台ID（唯一标志）
     */
    private String platformId;
    /**
     * 拨打可视电话的用户
     */
    private String userName;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 业务模式： 0 主动发起；1 被动发起（只适用终端）
     */
    private Integer businessMode;

    public Integer getBusinessMode() {
        return businessMode;
    }

    public void setBusinessMode(Integer businessMode) {
        this.businessMode = businessMode;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevAddress() {
        return devAddress;
    }

    public void setDevAddress(String devAddress) {
        this.devAddress = devAddress;
    }

    public String getSvrName() {
        return svrName;
    }

    public void setSvrName(String svrName) {
        this.svrName = svrName;
    }

    public String getDevNo() {
        return devNo;
    }

    public void setDevNo(String devNo) {
        this.devNo = devNo;
    }

    public Integer getPlatformType() {
        return platformType;
    }

    public void setPlatformType(Integer platformType) {
        this.platformType = platformType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getUniqId() {
        return uniqId;
    }

    public void setUniqId(String uniqId) {
        this.uniqId = uniqId;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }
}
