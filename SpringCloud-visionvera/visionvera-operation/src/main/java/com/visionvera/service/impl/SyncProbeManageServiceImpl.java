package com.visionvera.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.visionvera.service.SyncProbeManageService;
import com.visionvera.util.ProbeManagerMsgUtil;

@Service
public class SyncProbeManageServiceImpl implements SyncProbeManageService {

	@Override
	@Async
	public void portInfoChange() {
		
		 ProbeManagerMsgUtil.portInfoChange();
	}

	@Override
	@Async
	public void addProbeBatch() {
		
		 ProbeManagerMsgUtil.addProbeBatch();
	}

}
