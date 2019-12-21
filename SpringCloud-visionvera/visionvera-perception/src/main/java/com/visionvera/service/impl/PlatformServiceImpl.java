package com.visionvera.service.impl;

import com.visionvera.dao.ywcore.PlatformDao;
import com.visionvera.service.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class PlatformServiceImpl implements PlatformService {

	@Autowired
	private PlatformDao platformDao;

	@Override
	public int getProcessExceptionCount(String registerid) {
		return platformDao.getProcessExceptionCount(registerid);
	}

}
