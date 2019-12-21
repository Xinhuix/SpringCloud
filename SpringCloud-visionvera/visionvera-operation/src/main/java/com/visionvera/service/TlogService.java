package com.visionvera.service;

import java.util.List;
import java.util.Map;

public interface TlogService {
	
	/**
	 * 查询日志列表
	 * @param log 
	 * @return
	 */
	public List<Map<String,Object>> getTlogListSelective(Map<String,Object> params);
	
	/**
	 * 查询日志类型列表
	 * @param log 
	 * @return
	 */
	public List<Map<String,Object>> getTlogTypeList();

}
