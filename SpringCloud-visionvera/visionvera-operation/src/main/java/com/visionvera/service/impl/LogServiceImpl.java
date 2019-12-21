package com.visionvera.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.cms.LogTypeVO;
import com.visionvera.bean.cms.LogVO;
import com.visionvera.dao.operation.LogDao;
import com.visionvera.service.LogService;
import com.visionvera.util.StringUtil;

@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class LogServiceImpl implements LogService {
	@Autowired
	private LogDao logDao;
	
	/**
	 * 通过条件查询日志列表
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param log 查询条件
	 * @return
	 */
	@Override
	public PageInfo<LogVO> getLogList(boolean isPage, Integer pageNum, Integer pageSize, LogVO log) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		if (StringUtil.isNotNull(log.getTypes())) {
			log.setTypesArr(log.getTypes().split(","));
		}
		
		List<LogVO> logList = this.logDao.selectLogByCondition(log);
		PageInfo<LogVO> logInfo = new PageInfo<LogVO>(logList);
		return logInfo;
	}
	
	/**
	 * 获取日志类型
	 */
	@Override
	public List<LogTypeVO> getLogTypeList() {
		return this.logDao.selectLogType();
	}

}
