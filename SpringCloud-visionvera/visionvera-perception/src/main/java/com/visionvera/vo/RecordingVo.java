package com.visionvera.vo;

import java.util.Date;

/**
 * @author Administrator
 * @date 2018年12月06日 16:27
 */
public class RecordingVo {
    /**
     * 业务唯一ID
     */
    private String uniqId;
    /**
     * 视频名称
     */
    private String vedioName;
    /**
     * 虚拟终端号
     */
    private String virtualNo;
    /**
     * 虚拟终端号
     */
    private String virtualName;
    /**
     * 虚拟终端号
     */
    private String virtualAddress;
    /**
     * 所属服务器名称
     */
    private String svrname;
    /**
     * 上报业务的平台类型：301 唐古拉； 401 流媒体web平台； 501 内容管理平台
     */
    private Integer platformType;
    /**
     * 用户
     */
    private String userName;
    /**
     * 录制类型：0 会议； 1 监控
     */
    private Integer recordType;
    /**
     * 开始时间
     */
    private Date startTime;

    public String getUniqId() {
        return uniqId;
    }

    public void setUniqId(String uniqId) {
        this.uniqId = uniqId;
    }

    public String getVedioName() {
        return vedioName;
    }

    public void setVedioName(String vedioName) {
        this.vedioName = vedioName;
    }

    public String getVirtualNo() {
        return virtualNo;
    }

    public void setVirtualNo(String virtualNo) {
        this.virtualNo = virtualNo;
    }

    public String getVirtualName() {
        return virtualName;
    }

    public void setVirtualName(String virtualName) {
        this.virtualName = virtualName;
    }

    public String getVirtualAddress() {
        return virtualAddress;
    }

    public void setVirtualAddress(String virtualAddress) {
        this.virtualAddress = virtualAddress;
    }

    public String getSvrname() {
        return svrname;
    }

    public void setSvrname(String svrname) {
        this.svrname = svrname;
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

    public Integer getRecordType() {
        return recordType;
    }

    public void setRecordType(Integer recordType) {
        this.recordType = recordType;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}

