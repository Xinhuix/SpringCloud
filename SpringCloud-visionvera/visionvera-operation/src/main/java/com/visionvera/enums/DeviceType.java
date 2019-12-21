package com.visionvera.enums;

public enum DeviceType {
	
	aps("aps","应用服务器"),
	cap("cap","抓包机");
	
	DeviceType(String deviceType,String deviceTypeName) {
		this.deviceType = deviceType;
		this.deviceTypeName = deviceTypeName;
	}
	
	private String deviceType;
	private String deviceTypeName;
	
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	public String getDeviceTypeName() {
		return deviceTypeName;
	}
	public void setDeviceTypeName(String deviceTypeName) {
		this.deviceTypeName = deviceTypeName;
	}
	
}
