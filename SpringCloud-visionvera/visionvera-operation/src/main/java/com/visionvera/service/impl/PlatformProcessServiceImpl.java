package com.visionvera.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.ywcore.PlatformDao;
import com.visionvera.dao.ywcore.PlatformProcessDao;
import com.visionvera.dao.ywcore.ServerBasicsDao;
import com.visionvera.dao.ywcore.TposProcessRelationDao;
import com.visionvera.enums.TransferType;
import com.visionvera.service.PlatformProcessService;
import com.visionvera.util.ProbeManagerMsgUtil;

@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class PlatformProcessServiceImpl implements PlatformProcessService {
	
	private Logger logger = LoggerFactory.getLogger(PlatformProcessServiceImpl.class);
	
	@Autowired
	private PlatformProcessDao platformProcessDao;
	@Autowired
	private TposProcessRelationDao tpRelationDao;
	@Autowired
	private ServerBasicsDao serverBasicsDao;
	@Autowired
	private PlatformDao platformDao;
	
	//private RestTemplate restTemplate = new RestTemplate();
	
	@Override
	public List<PlatformProcess> getAllProcesses(String serverUnique) {
		List<PlatformProcess> processList = platformProcessDao.getAllProcesses(serverUnique);
		return processList;
	}

	@Override
	public void insertPlatformProcess(PlatformProcess platformProcess, String userId) throws Exception{
		platformProcessDao.insertPlatformProcess(platformProcess);
		//维护进程与平台的关系表
	/*	String registerIds = platformProcess.getRegisterIds();
		String[] registerIdArr = registerIds.split(",");
		
		List<TposProcessRelation> tpRelationList = new ArrayList<TposProcessRelation>();
		for (String registerId : registerIdArr) {
			TposProcessRelation tpRelation = new TposProcessRelation();
			tpRelation.setTposProcessId(platformProcess.getId());
			tpRelation.setTposRegisterid(registerId);
			tpRelationList.add(tpRelation);
		}
		tpRelationDao.insertTposProcessRelationBatch(tpRelationList);
		
		if(registerIdArr.length > 0) {
			String registerid = registerIdArr[0];
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsByRegisterid(registerid);
			//String transferType = serverBasics.getTransferType();
			if(TransferType.IP.getTransferType().equals(transferType)) {
				String manageIp = serverBasics.getServerManageIp();
				Integer port = serverBasics.getPort();
				//String url = "http://"+manageIp+":"+port+"/serverwatch/process/addProcess";
				String url = String.format(GlobalConstants.ADD_PROCESS_URL,manageIp,port);
				Map<String,Object> resultStr = RestTemplateUtil.postForObject(url,platformProcess,Map.class);
				if(resultStr.get("result") == null || !(Boolean)resultStr.get("result")) {
					throw new Exception("同步进程信息到探针失败");
				}
			}else {
			jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.ADD_PROC
					+":::"+platformProcess.getId()+":"+userId,"", WebSocketPushMessage.EXPIRE_SECONDS);
			boolean retFlag = ProbeManagerMsgUtil.sendProcMsg(serverBasics,platformProcess,
					ProbeManagerMsgUtil.ADD_PROC,userId);
			if(!retFlag) {
				jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.ADD_PROC
						+":::"+platformProcess.getId()+":"+userId);
				logger.error("同步进程信息到监测探针失败");
				throw new Exception("同步进程信息到监测探针失败");
			}
			//}
		}*/
	}

	@Override
	public Integer updatePlatformProcess(PlatformProcess platformProcess) throws Exception{
		Integer i = platformProcessDao.updatePlatformProcess(platformProcess);
		//更新平台进程的关系
		tpRelationDao.deleteTposProcessRelationByProcessId(platformProcess.getId());
		
		/*String registerIds = platformProcess.getRegisterIds();
		String[] registerIdArr = registerIds.split(",");
		List<TposProcessRelation> tpRelationList = new ArrayList<TposProcessRelation>();
		for (String registerId : registerIdArr) {
			TposProcessRelation tpRelation = new TposProcessRelation();
			tpRelation.setTposProcessId(platformProcess.getId());
			tpRelation.setTposRegisterid(registerId);
			tpRelationList.add(tpRelation);
		}		
		tpRelationDao.insertTposProcessRelationBatch(tpRelationList);
		
		logger.info("平台数量====="+registerIdArr.length);*/
		/*if(registerIdArr.length > 0) {
			String registerid = registerIdArr[0];
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsByRegisterid(registerid);
			String transferType = serverBasics.getTransferType();
			logger.info("传输协议："+transferType);
			if(TransferType.IP.getTransferType().equals(transferType)) {
				String manageIp = serverBasics.getServerManageIp();
				Integer port = serverBasics.getPort();
				//String url = "http://"+manageIp+":"+port+"/serverwatch/process/modifyProcess";
				String url = String.format(GlobalConstants.MODIFY_PROCESS_URL,manageIp,port);
				Map<String,Object> resultStr = RestTemplateUtil.postForObject(url,platformProcess,Map.class);
				if(resultStr.get("result") == null || !(Boolean)resultStr.get("result")) {
					throw new Exception("同步进程信息到探针失败");
				}
			}
		}*/
		
		return i;
	}
	/**
	 * 修改平台和进程
	 */
	@Override
	public ReturnData updatePlatformAndProcess(PlatformVO platformVO){
		BaseReturn baseReturn = new BaseReturn();
		try {
			PlatformProcess processParams = new PlatformProcess();
			processParams.setId(platformVO.getProcessId());
			processParams.setShowName(platformVO.getShowName());
			processParams.setProcessIp(platformVO.getProcessIp());
			processParams.setProcessPort(platformVO.getProcessPort());
			//版本检测开关不一致 上报给探针管理
			PlatformVO platform = platformDao.getPlatformByTposRegisterid(platformVO.getTposRegisterid());
			if(platform==null){
				return baseReturn.returnError("平台信息不存在，修改失败");
			}
			Boolean result=true;
			if(platform.getVersionCheck()!=platformVO.getVersionCheck()){
				platformVO.setAbbreviation(platform.getAbbreviation());
				platformVO.setServerUnique(platform.getServerUnique());
				result =ProbeManagerMsgUtil.checkPlatformVersionCheck(platformVO);
			}
			if(!result){
				return baseReturn.returnError("与探针管理同步失败，修改失败");
			}
			int platCount = platformDao.updatePlatform(platformVO);
			int procCount = platformProcessDao.updatePlatformProcess(processParams);
			if(platCount>0&&procCount>0){
				return baseReturn.returnSuccess("修改成功");
			}
			
		} catch (Exception e) {
			logger.error("PlatformProcessServiceImpl--updatePlatformProcess",e);
		}
		return baseReturn.returnError("修改失败");
	}

	@Override
	public Integer deleteProcessById(Integer id) throws Exception {
		//删除进程信息同步到探针管理服务
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsByProcessId(id);
		
		//PlatformProcess process = platformProcessDao.getProcessById(id);
		Integer count = platformProcessDao.deleteProcessById(id);
		//更新平台进程的关系
		tpRelationDao.deleteTposProcessRelationByProcessId(id);
		
		String transferType = serverBasics.getTransferType();
		if(TransferType.IP.getTransferType().equals(transferType)) {
			String manageIp = serverBasics.getServerManageIp();
			Integer port = serverBasics.getPort();
			//String url = "http://"+manageIp+":"+port+"/serverwatch/process/"+id+"/delete";
			String url = String.format(GlobalConstants.DEL_PROCESS_URL,manageIp,port,id);
			Map<String,Object> resultStr =  RestTemplateUtil.postForObject(url,id,Map.class);
			if(resultStr.get("result") == null || !(Boolean)resultStr.get("result")) {
				throw new Exception("同步进程信息到探针失败");
			}
		}
		return count;
	}

	@Override
	public List<PlatformVO> getPlatformVOByProcessId(Integer id) {
		List<PlatformVO> platformList = platformProcessDao.getPlatformListByProcessId(id);
		return platformList;
	}
	
	@Override
	public ReturnData getProcessByid(Integer id) {
		
		BaseReturn baseRetrun = new BaseReturn();
		try {
		Map<String,Object> resultMap =	platformProcessDao.getProcessById(id);
		return baseRetrun.returnResult(0, "获取成功", null, null, resultMap);
		} catch (Exception e) {
			logger.error("PlatformProcessServiceImpl--getProcessByid",e);
		}
		return baseRetrun.returnError("获取失败");
	}

	@Override
	public List<PlatformProcess> getPlatformProcessListByTposId(String registerid) {
		List<PlatformProcess> processes = platformProcessDao.getProcessByTposRegisterid(registerid);
		return processes;
	}

	@Override
	public void recoverProcessStatusBatch(List<PlatformProcess> processList) {
		platformProcessDao.recoverProcessStatusBatch(processList);
	}

	@Override
	public void recoverProcessStateByIds(String processIds) {
		platformProcessDao.recoverProcessStateByIds(processIds);
	}

	@Override
	public void deleteProcessByIds(String processIds) {
		platformProcessDao.deleteProcessByIds(processIds);
	}

	@Override
	public void recoverProcessStatusByIds(String procIdStr) {
		platformProcessDao.recoverProcessStatusByIds(procIdStr);
	}

	@Override
	public Map<String, Object> getTopsNumByServerUnique(String serverUnique) {
		
		return platformProcessDao.getTopsNumByServerUnique(serverUnique);
	}

}
