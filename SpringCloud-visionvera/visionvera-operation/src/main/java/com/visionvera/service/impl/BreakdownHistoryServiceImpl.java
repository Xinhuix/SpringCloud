package com.visionvera.service.impl;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.visionvera.basecrud.CrudService;
import com.visionvera.bean.operation.BreakdownHistory;
import com.visionvera.bean.operation.BreakdownInfo;
import com.visionvera.dao.operation.BreakdownHistoryDao;
import com.visionvera.service.BreakdownHistoryService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
@Service("breakdownHistoryService")
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class BreakdownHistoryServiceImpl extends CrudService<BreakdownHistoryDao, BreakdownHistory> implements BreakdownHistoryService {
	private static final Logger logger = Logger.getLogger(BreakdownHistoryServiceImpl.class) ;
	@Resource
	private BreakdownHistoryDao breakdownHistoryDao ;
	
	@Override
	public List<BreakdownHistory> getBreakdownHistoryList(BreakdownHistory history,HashMap<String, Object> paramsMap){
		
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			List<BreakdownHistory> breakdownHistoryList = breakdownHistoryDao.queryList(history) ;
			return breakdownHistoryList;
		}
		PageHelper.startPage((Integer)paramsMap.get("pageNum"), (Integer)paramsMap.get("pageSize")) ;
		List<BreakdownHistory> breakdownHistoryList = breakdownHistoryDao.queryList(history) ;
		return breakdownHistoryList;
		
	}
}
