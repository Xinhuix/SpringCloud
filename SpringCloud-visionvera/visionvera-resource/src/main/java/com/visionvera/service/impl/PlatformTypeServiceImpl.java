package com.visionvera.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.datacore.TPlatformTypeVO;
import com.visionvera.dao.resource.PlatformTypeDao;
import com.visionvera.service.PlatformTypeService;

@Service
@Transactional(value = "transactionManager_resource", rollbackFor = Exception.class)
public class PlatformTypeServiceImpl implements PlatformTypeService {
	
	@Autowired
	private PlatformTypeDao platformTypeDao;

	@Override
	public List<TPlatformTypeVO> getPlatformTypeList(Map<String,Object> params) {
		List<TPlatformTypeVO> platformTypeVOList = platformTypeDao.getPlatformTypeList(params);
		return platformTypeVOList;
	}

}
