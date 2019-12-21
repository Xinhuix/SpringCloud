package com.visionvera.service.impl;

import com.visionvera.bean.alarm.AlarmDomain;
import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.PlatformTypeVO;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.ywcore.PlatformDao;
import com.visionvera.dao.ywcore.PlatformTypeDao;
import com.visionvera.dao.ywcore.ServerBasicsDao;
import com.visionvera.enums.LocalAlarmTypes;
import com.visionvera.enums.TransferType;
import com.visionvera.service.AlarmService;
import com.visionvera.service.PlatformProcessService;
import com.visionvera.service.PlatformService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.ProbeManagerMsgUtil;
import com.visionvera.websocket.WebSocketPushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class PlatformServiceImpl implements PlatformService {

    private Logger logger = LoggerFactory.getLogger(PlatformServiceImpl.class);
    @Autowired
    private JRedisDao jRedisDao;
	@Autowired
	private PlatformDao platformDao;
    @Autowired
	private ServerBasicsDao serverBasicsDao;
    @Autowired
    private PlatformTypeDao platformTypeDao;
    @Autowired
    private AlarmService alarmService;
    @Autowired
    private PlatformProcessService processService;

	@Override
	public int getProcessExceptionCount(String registerid) {
		return platformDao.getProcessExceptionCount(registerid);
	}

	@Override
	public List<PlatformVO> getPlatformListByServerUnique(String serverUnique) {
		return platformDao.getPlatformListByServerUnique(serverUnique);
	}

	@Override
	public int insertPlatform(PlatformVO platformVO, String userId) throws Exception {
		String tposRegisterid = "slw".concat(UUID.randomUUID().toString());
		platformVO.setTposRegisterid(tposRegisterid);		
		int num = platformDao.insertPlatform(platformVO);

        ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(platformVO.getServerUnique());
        String transferType = serverBasics.getTransferType();
        PlatformTypeVO platformTypeVO = platformTypeDao.getPlatformTypeByTypeId(platformVO.getTposPlatformType());
        platformVO.setAbbreviation(platformTypeVO.getAbbreviation());
        platformVO.setTransferType(transferType);
        //platformVO.setConfCheck(0);
      /*  if(TransferType.IP.getTransferType().equals(transferType)) {
            String url = String.format(GlobalConstants.PROBE_ADD_PLATFORM_URL,serverBasics.getServerManageIp(),serverBasics.getPort());

            Map<String,Object> resultMap = RestTemplateUtil.postForObject(url,platformVO,Map.class);
            if(!(Boolean)resultMap.get("result")) {
                logger.error("监测探针添加监测平台失败");
                throw new Exception("监测探针添加监测平台失败");
            }
        }else if(TransferType.V2V.getTransferType().equals(transferType)) {
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.ADD_PLATFORM
                    +"::"+platformVO.getTposRegisterid()+"::"+userId,"", WebSocketPushMessage.EXPIRE_SECONDS);
            boolean retFlag = ProbeManagerMsgUtil.addAndModifyPlatform(ProbeManagerMsgUtil.ADD_PLATFORM,platformVO,userId);
            if(!retFlag) {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.ADD_PLATFORM
                        +"::"+platformVO.getTposRegisterid()+"::"+userId);
               logger.error("监测探针添加监测平台失败");
               throw new Exception("监测探针添加监测平台失败");
            }
        }*/
        jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.ADD_PLATFORM
                +"::"+platformVO.getTposRegisterid()+"::"+userId,"", WebSocketPushMessage.EXPIRE_SECONDS);
        boolean retFlag = ProbeManagerMsgUtil.addAndModifyPlatform(ProbeManagerMsgUtil.ADD_PLATFORM,platformVO,userId);
        if(!retFlag) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.ADD_PLATFORM
                    +"::"+platformVO.getTposRegisterid()+"::"+userId);
           logger.error("监测探针添加监测平台失败");
           throw new Exception("监测探针添加监测平台失败");
        }
        return num;
	}

	@Override
	public int updatePlatform(PlatformVO platformVO) throws Exception{
		int num = platformDao.updatePlatform(platformVO);
        PlatformVO oldPlatformVO = platformDao.getPlatformByTposRegisterid(platformVO.getTposRegisterid());

        ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(oldPlatformVO.getServerUnique());
        platformVO.setAbbreviation(oldPlatformVO.getAbbreviation());
        //platformVO.setConfCheck(oldPlatformVO.getConfCheck());

        String transferType = serverBasics.getTransferType();
        if(TransferType.IP.getTransferType().equals(transferType)) {
            String url = String.format(GlobalConstants.PROBE_MODIFY_PLATFORM_URL,serverBasics.getServerManageIp(),serverBasics.getPort());
            Map<String,Object> resultMap = RestTemplateUtil.postForObject(url,platformVO,Map.class);
            if(!(Boolean)resultMap.get("result")) {
                logger.error("监测探针修改平台失败");
                throw new Exception("监测探针修改平台失败");
            }
        }
        return num;
	}

	@Override
	public int deletePlatform(String tposRegisterid) throws Exception{
		ServerBasics serverBasics = platformDao.getServerBasicsByTposRegisterid(tposRegisterid);
		String transferType = serverBasics.getTransferType();
		if(TransferType.IP.getTransferType().equals(transferType)) {
            String url = String.format(GlobalConstants.PROBE_REMOVE_PLATFORM_URL,serverBasics.getServerManageIp(),serverBasics.getPort(),tposRegisterid);
            Map<String,Object> resultMap = RestTemplateUtil.getForObject(url,Map.class);
            if(!(Boolean) resultMap.get("result")) {
                logger.error("监测探针删除平台失败");
                throw new Exception("监测探针删除平台失败");
            }

        }
        recoverPlatformAlarm(tposRegisterid);
		jRedisDao.del(GlobalConstants.ALARM_REDIS_PREFIX + tposRegisterid +":"+
                LocalAlarmTypes.softconfig.getAlarmType() + ":" + LocalAlarmTypes.softconfig.getSubType());

        int num =  platformDao.deletePlatform(tposRegisterid);
        return num;
	}

    /**
     * 恢复平台告警
     * @param tposRegisterid
     */
    private void recoverPlatformAlarm(String tposRegisterid) {
        String nowTime = DateUtil.date2String("yyyy-MM-dd HH:mm:ss");
        ServerBasics serverBasics = platformDao.getServerBasicsByTposRegisterid(tposRegisterid);
        PlatformVO platform = platformDao.getPlatformByTposRegisterid(tposRegisterid);
        String serverUnique = serverBasics.getServerUnique();
        String[] regionIdArr = serverBasics.getServerDistrict().split(",");
        List<PlatformProcess> processList = processService.getPlatformProcessListByTposId(tposRegisterid);
        List<AlarmDomain>  alarms = new ArrayList<>();
        for (PlatformProcess process : processList) {
            AlarmDomain processAlarm
                    = alarmService.generateAlarm(serverUnique+":"+process.getId(),platform.getPlatformName(),LocalAlarmTypes.softabnormal,"clear",
                    nowTime,"平台运行异常告警恢复",regionIdArr[regionIdArr.length-1],"OPERATION");
            alarms.add(processAlarm);
        }

        AlarmDomain configAlarm
                = alarmService.generateAlarm(tposRegisterid,platform.getPlatformName(),LocalAlarmTypes.softconfig,"clear",
                nowTime,"平台配置告警恢复",regionIdArr[regionIdArr.length-1],"OPERATION");
        alarms.add(configAlarm);
        if(alarms.size() > 0) {
            for (AlarmDomain alarm : alarms) {
                alarmService.addAlarm(alarm);
            }
        }
    }

    @Override
    public PlatformVO getPlatformByTposRegisterid(String tposRegisterid) {
	    PlatformVO platformVO = platformDao.getPlatformByTposRegisterid(tposRegisterid);
        return platformVO;
    }

    @Override
    public int recoverPlatform(String platid) {
	    int num =  platformDao.recoverPlatform(platid);
        return num;
    }

    @Override
    public int updatePlatformVersionByPlatformType(int platformType,String version) {
	    int num = platformDao.updatePlatfomVersionByPlatformType(platformType,version);
        return num;
    }

}
