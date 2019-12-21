package com.visionvera.enums;

/**
 * 应用服务器告警类型
 * @author dql
 *
 */
public enum LocalAlarmTypes {
	
	cpu("device","cpu","应用服务器/cpu","minor"),
	memory("device","memory","应用服务器/memory","minor"),
	disk("device","disk","应用服务器/disk","minor"),
	network("device","network","应用服务器/network","minor"),
	offline("device","offline","应用服务器/离线","critical"),
	softabnormal("platform","softabnormal","应用软件/运行异常","critical"),
	softconfig("platform","softconfig","应用软件/配置","minor");
	
	LocalAlarmTypes(String alarmType,String subType,String subTypeName,String levelId) {
		this.alarmType = alarmType;
		this.subType = subType;
		this.subTypeName = subTypeName;
		this.levelId = levelId;
	}
	
	private String alarmType;
	private String subType;
	private String subTypeName;
	private String levelId;
	
	public String getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	
	public String getSubTypeName() {
		return subTypeName;
	}
	public void setSubTypeName(String subTypeName) {
		this.subTypeName = subTypeName;
	}
	
	public String getLevelId() {
		return levelId;
	}
	public void setLevelId(String levelId) {
		this.levelId = levelId;
	}
	
}
