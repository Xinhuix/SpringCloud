package com.visionvera.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.cms.SmsVO;
import com.visionvera.dao.authentication.SmsDao;
import com.visionvera.service.SmsService;

@Service("smsService")
@Transactional(transactionManager = "transactionManager_authentication", rollbackFor = Exception.class)
public class SmsServiceImpl implements SmsService {
	@Autowired
	private SmsDao smsDao;

	@Override
	public SmsVO getSms() {
		return this.smsDao.getSms();
	}

	@Override
	public int updateSms(SmsVO sms) {
		return smsDao.updateSms(sms);
	}

}
