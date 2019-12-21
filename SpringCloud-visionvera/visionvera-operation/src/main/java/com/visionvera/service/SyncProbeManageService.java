package com.visionvera.service;

public interface SyncProbeManageService {
	
	
	 /**
     * 端口变更通知探针管理
     * @param JSONObject
     */
	 public void portInfoChange();
	 
    /**
     * 探针批量导入通知探针管理
     * @param JSONObject
     */
	 public  void addProbeBatch();

}
