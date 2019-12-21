package com.visionvera.util;

import java.util.UUID;

import org.activiti.engine.impl.cfg.IdGenerator;

public class UUIDGenerator implements IdGenerator {

	public String getNextId() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	 /**
     * 生成UUID
     * @return
     */
    public static String getUuid()
    {
    	UUID uuid = UUID.randomUUID();
    	return uuid.toString().replaceAll("\\-", "");
    }

}
