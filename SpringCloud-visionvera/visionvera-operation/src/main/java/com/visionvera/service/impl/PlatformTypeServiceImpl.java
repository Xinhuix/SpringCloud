package com.visionvera.service.impl;

import com.visionvera.bean.slweoms.PlatformTypeVO;
import com.visionvera.dao.ywcore.PlatformTypeDao;
import com.visionvera.service.PlatformTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 平台类型Service
 * @author dql
 *
 */
@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class PlatformTypeServiceImpl implements PlatformTypeService {
	
	@Autowired
	private PlatformTypeDao platformTypeDao;

	@Override
	public List<PlatformTypeVO> getAllPlatformType() {
		List<PlatformTypeVO> platformTypeList = platformTypeDao.getAllPlatformType();
		return platformTypeList;
	}

	@Override
	public PlatformTypeVO getPlatformTypeByTypeId(Integer tposPlatformType) {
		PlatformTypeVO platformTypeVO = platformTypeDao.getPlatformTypeByTypeId(tposPlatformType);
		return platformTypeVO;
	}

}
