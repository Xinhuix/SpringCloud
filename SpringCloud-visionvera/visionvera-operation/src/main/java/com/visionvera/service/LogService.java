package com.visionvera.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.cms.LogTypeVO;
import com.visionvera.bean.cms.LogVO;

/**
 * 日志操作业务接口
 *
 */
public interface LogService {
	/**
	 * 通过条件查询日志列表
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param log 查询条件
	 * @return
	 */
	public PageInfo<LogVO> getLogList(boolean isPage, Integer pageNum, Integer pageSize, LogVO log);
	
	/**
	 * 获取日志类型
	 */
	public List<LogTypeVO> getLogTypeList();
}
