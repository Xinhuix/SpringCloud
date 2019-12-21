package com.visionvera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SysConfig {
	@Value("${hg.protocol:http}")
	private String protocol;
	@Value("${sys_bit:64}")
	private String sysBit;
	
	public String getProtocal() {
		return protocol;
	}
	public String getSysBit() {
		return sysBit;
	}
}
