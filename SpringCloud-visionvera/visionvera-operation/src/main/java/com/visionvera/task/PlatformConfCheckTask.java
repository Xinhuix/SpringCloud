package com.visionvera.task;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.enums.LocalAlarmTypes;
import com.visionvera.service.PlatformService;
import com.visionvera.util.ProbeManagerMsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * 需求：
 *   平台配置文件检测出现告警后，对应的平台每10min检测一次配置文件。以达到告警及时恢复的目的。
 */
@Component
public class PlatformConfCheckTask {

    private Logger logger = LoggerFactory.getLogger(PlatformConfCheckTask.class);

    @Autowired
    private JRedisDao jRedisDao;
    @Autowired
    private PlatformService platformService;
  

    @Scheduled(cron = "0 0/10 * * * ?")
    public void platformConfCheck() {
        try {
            Set<String> keys = jRedisDao.keys(GlobalConstants.ALARM_REDIS_PREFIX + "*" +
                    LocalAlarmTypes.softconfig.getAlarmType() + ":" + LocalAlarmTypes.softconfig.getSubType());
            for (String key : keys) {
                String alarmSign = jRedisDao.get(key);
                if("true".equals(alarmSign)) {
                    //通知探针检测配置文件
                    String tposRegisterid = key.replaceAll(GlobalConstants.ALARM_REDIS_PREFIX + "(.*):"
                            +LocalAlarmTypes.softconfig.getAlarmType()+":"+LocalAlarmTypes.softconfig.getSubType(),"$1");
                    PlatformVO platformVO = platformService.getPlatformByTposRegisterid(tposRegisterid);
                    if(platformVO!=null){
                    	 JSONObject json = new JSONObject();
                    	 json.put("serverUnique", platformVO.getServerUnique());
                    	 json.put("tposRegisterid", tposRegisterid);
                    	 json.put("tposIp", platformVO.getTposIp());
                    	 json.put("abbreviation", platformVO.getAbbreviation());
                    	checkGisTglConf(json);
                    }else{
                    	logger.warn("平台配置告警后，定时检测平台配置文件平台不存在："+tposRegisterid);
                    }
               
                }
            }
        } catch(Exception e){
            logger.error("定时检测平台配置文件出现异常", e);
        }
    }

    /**
     * 检查GIS平台配置文件
     * @param platformVO
    
    private void checkGisConf(PlatformVO platformVO) {
        ServerBasics serverBasics = serverBasicsService.getServerBasicsByServerUnique(platformVO.getServerUnique());
        String url = String.format(GlobalConstants.PROBE_PLATFORM_CONF_CHECK,serverBasics.getServerManageIp(),
                serverBasics.getPort(),platformVO.getAbbreviation());
        logger.info("检测Gis配置文件url:" + url);
        Map<String,Object> resultMap = RestTemplateUtil.getForObject(url,Map.class);
        if(resultMap.get("result") != null || (Boolean)resultMap.get("result")) {
            logger.info("通知监测探针检查配置文件成功");
        }else {
            logger.error("通知监测探针检查配置文件异常");
        }
    } */

    /**
     * 检查TGL平台配置文件
     * @param platformVO
     */
    private void checkGisTglConf(JSONObject json) {
        boolean retFlag = ProbeManagerMsgUtil.platformCheckInfo(json);
        if(retFlag) {
            logger.info("通知监测探针检查配置文件成功");
        }else {
            logger.error("通知监测探针检查配置文件异常");
        }
    }

}
