package com.visionvera.task;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.service.ServerBasicsService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.ProbeManagerMsgUtil;

/**
 * 需求：
 *   定时根据配置的时间去调探针管理查询GIS、唐古拉平台的配置信息
 */
@Component
public class ConfigCheckTask {

    private Logger logger = LoggerFactory.getLogger(ConfigCheckTask.class);

    @Autowired
    private ServerBasicsService serverBasicsService;

    //@Scheduled(cron = "0 0/10 * * * ?")
    @Scheduled(cron = "0 0 */1 * * ?")
    public void platformConfCheck() {
        
    	
    	try {
    		String time="";
    		int hour = DateUtil.getHour();
    		if(hour<10){
    			time ="0"+hour;
    		}else{
    			time =""+hour;
    		}
        	List<Map<String,Object>> list  =serverBasicsService.getConfigCheckList(time);
        	 if(list!=null&&list.size()>0){
        		 for(Map<String,Object> server:list){
        			 JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(server));
        			 ProbeManagerMsgUtil.platformCheckInfo(json);
        		 }
        	 }
			
		} catch (Exception e) {
			logger.error("唐古拉、GIS配置检测定时任务出错",e);
		}
    
     }

    
    public static void main(String[] args) {
		System.out.println(DateUtil.getHour());
	}










}