package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.visionvera.bean.cms.LogTypeVO;
import com.visionvera.bean.cms.LogVO;
import com.visionvera.bean.cms.MeetRecordVO;


/**
 * 日志操作相关的DAO
 *
 */
public interface LogDao {
	/**
	 * 通过条件查询日志
	 * @param log 查询条件
	 * @return
	 */
	public List<LogVO> selectLogByCondition(LogVO log);
	
	/**
	 * 查询所有日志类型
	 * @return
	 */
	public List<LogTypeVO> selectLogType();
	/**
	 * 
	 * @Title: getLogList
	 * @Description: 分页获取日志列表
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	List<LogVO> getLogList(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 
	 * @Title: getLogListCount
	 * @Description: 获取日志总数
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	int getLogListCount(Map<String, Object> map);
	/**
	 * 
	 * @Title: getRecordList
	 * @Description: 根据会议ID查询会议纪要列表
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	List<MeetRecordVO> getRecordList(Map<String, Object> map,
			RowBounds rowBounds);
}