package com.visionvera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SysConfig {
	@Value("${hg.protocol:http}")
	private String protocol;
	@Value("${hg.ip}")
	private String hgIp;
	@Value("${hg.port}")
	private String hgPort;
	@Value("${content.ip}")
	private String contentIp;
	@Value("${content.port}")
	private String contentPort;
	public String getHgIp() {
		return hgIp;
	}
	public void setHgIp(String hgIp) {
		this.hgIp = hgIp;
	}
	public String getHgPort() {
		return hgPort;
	}
	public void setHgPort(String hgPort) {
		this.hgPort = hgPort;
	}
	public String getHgUrl() {
		return protocol+"://" + this.hgIp + ":" + this.hgPort;
	}
	
	public void setContentIp(String contentIp) {
		this.contentIp = contentIp;
	}
	public String getContentPort() {
		return contentPort;
	}
	public void setContentPort(String contentPort) {
		this.contentPort = contentPort;
	}
	public String getContentUrl() {
		return "http://" + this.contentIp + ":" + this.contentPort;
	}
}
