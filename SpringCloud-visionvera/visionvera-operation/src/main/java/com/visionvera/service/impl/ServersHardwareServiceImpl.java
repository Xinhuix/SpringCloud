package com.visionvera.service.impl;

import com.visionvera.bean.slweoms.ServerHardwareRelation;
import com.visionvera.bean.slweoms.ServerHardwareVO;
import com.visionvera.dao.ywcore.ServerHardwareRelationDao;
import com.visionvera.dao.ywcore.ServersHardwareDao;
import com.visionvera.service.ServersHardwareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class ServersHardwareServiceImpl implements ServersHardwareService {
	
	private Logger logger = LoggerFactory.getLogger(ServersHardwareServiceImpl.class);
	
	@Autowired
	private ServersHardwareDao serversHardwareDao;
	
	@Autowired
	private ServerHardwareRelationDao relationDao;
	
	@Override
	public List<Long> getServersHardwareListBeforeDay(Integer reserverDays,String serverUnique) {
		List<Long> hardwareIds = serversHardwareDao.getServersHardwareListBeforeDay(reserverDays,serverUnique);
		return hardwareIds;
	}

	@Override
	public int deleteServersHardwareList(List<Long> serverHardwareIds) {
		if(serverHardwareIds != null && serverHardwareIds.size() > 0) {
			Integer num = serversHardwareDao.deleteServersHardwareList(serverHardwareIds);
			return num;
		}
		return 0;
	}

	@Override
	public int insertServerHardwareVO(ServerHardwareVO serverHardwareVO) {
		int num = serversHardwareDao.insertServersHardware(serverHardwareVO);
		ServerHardwareRelation relation = relationDao.getServerHardwareRelationByUnique(serverHardwareVO.getServerUnique());
        ServerHardwareRelation shRelation = new ServerHardwareRelation();
        shRelation.setServerUnique(serverHardwareVO.getServerUnique());
        shRelation.setHardwareId(serverHardwareVO.getId());
        if(relation == null) {
        	relationDao.insertServerHardwareRelation(shRelation);
        }else {
        	relationDao.updateServerHardwareRelation(shRelation);
        }
        return num;
	}
	
	@Override
	public List<Long> getServersHardwareListEarliest(String serverUnique, Integer size) {
		List<Long> hardwareIds = serversHardwareDao.getServersHardwareListEarlist(serverUnique,size);
		return hardwareIds;
	}

	
}
