package com.visionvera.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.visionvera.dao.datacore.TlogDao;
import com.visionvera.service.TlogService;

@Service
@Transactional(value = "transactionManager_dataCore", rollbackFor = Exception.class)
public class TlogServiceImpl implements TlogService {

	@Autowired
	private TlogDao tlogDao;

	@Override
	public List<Map<String,Object>> getTlogListSelective(Map<String,Object> params) {
		Integer pageNum = (Integer) params.get("pageNum");
		Integer pageSize = (Integer) params.get("pageSize");
		if (pageNum != -1) {
			PageHelper.startPage(pageNum, pageSize);
		}
		List<Map<String,Object>> list = tlogDao.getTlogListSelective(params);
		
		return list;
	}

	@Override
	public List<Map<String, Object>> getTlogTypeList() {
		
		return tlogDao.getTlogTypeList();
	}
	
	

}
