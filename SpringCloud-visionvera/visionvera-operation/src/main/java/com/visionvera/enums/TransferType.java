package com.visionvera.enums;

public enum TransferType {
	
	IP("IP","互联网"),
	V2V("V2V","视联网");
	
	private String transferType;
	private String desc;
	
	TransferType(String transferType,String desc) {
		this.transferType = transferType;
		this.desc = desc;
	}

	public String getTransferType() {
		return transferType;
	}

	public void setTransferType(String transferType) {
		this.transferType = transferType;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
