package com.visionvera.config.base;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SysConfig {
	@Value("${hg.ip}")
	private String hgIp;
	@Value("${hg.protocol:http}")
	private String protocol;
	@Value("${hg.port}")
	private String hgPort;
	@Value("${tgl_rest.ip}")
	private String tglRestIp;
	@Value("${tgl_rest.port}")
	private String tglRestPort;
	@Value("${tgl_ws.ip}")
	private String tglWsIp;
	@Value("${tgl_ws.port}")
	private String tglWsPort;
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
	public String getTglRestIp() {
		return tglRestIp;
	}
	public void setTglRestIp(String tglRestIp) {
		this.tglRestIp = tglRestIp;
	}
	public String getTglRestPort() {
		return tglRestPort;
	}
	public void setTglRestPort(String tglRestPort) {
		this.tglRestPort = tglRestPort;
	}
	public String getTglWsIp() {
		return tglWsIp;
	}
	public void setTglWsIp(String tglWsIp) {
		this.tglWsIp = tglWsIp;
	}
	public String getTglWsPort() {
		return tglWsPort;
	}
	public void setTglWsPort(String tglWsPort) {
		this.tglWsPort = tglWsPort;
	}
	
	public String getHgUrl() {
		return protocol+"://" + this.hgIp + ":" + this.hgPort;
	}
	public String getTglWsUrl() {
		return "http://" + this.tglWsIp + ":" + this.tglWsPort;
	}
	public String getTglRestUrl() {
		return "http://" + this.tglRestIp + ":" + this.tglRestPort;
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
