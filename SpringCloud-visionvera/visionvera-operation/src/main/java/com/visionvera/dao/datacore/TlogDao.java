package com.visionvera.dao.datacore;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.TLog;

/**
 * 操作日志的Dao
 *
 */
public interface TlogDao {
	/**
	 * 添加一条日志记录
	 * @param log 日志信息
	 * @return
	 */
	public int insertLog(TLog log);
	
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
