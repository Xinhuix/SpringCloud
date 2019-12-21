package com.visionvera.task;

import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.dao.JRedisDao;
import com.visionvera.service.AlarmService;
import com.visionvera.service.ServerBasicsService;
import com.visionvera.service.ServersHardwareService;
import com.visionvera.util.TimeUtil;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时删除服务器硬件信息
 * @author dql
 *
 */
@Component
public class DeleteHardwareTask {
	
	private Logger logger = LoggerFactory.getLogger(DeleteHardwareTask.class);
	
	@Autowired
	private ServerBasicsService serverBasicsService;
	
	@Autowired
	private ServersHardwareService serversHardwareService;
	
	@Autowired
	private AlarmService alarmService;
	
	@Autowired
	private ProcessEngine engine;
	
	@Autowired
	private JRedisDao jedisDao;

	@Scheduled(cron = "0 0 2 * * ?")
	public void deleteHardware() {
		try {			
			//查询所有的服务器包括已删除的
			List<ServerBasics> serverBasicsList = serverBasicsService.getAllServerBasics();
			Integer size = 500;
			for (ServerBasics serverBasics : serverBasicsList) {
				deleteServersHardware(serverBasics,size);
			}
			logger.info("删除服务器硬件信息定时任务执行完成");
		}catch(Exception e) {
			logger.error("定时删除硬件信息出错", e);
		}
	}
	
	private void deleteServersHardware(ServerBasics serverBasics, Integer size) {
		while(true) {
			//删除过期的服务器硬件信息，每次删除保留两条，保证每个服务器的硬件信息总是存在
			List<Long> serversHardwareIds = serversHardwareService.getServersHardwareListEarliest(serverBasics.getServerUnique(),size);
			if(serversHardwareIds.size() < size) {
				return;
			}
			serversHardwareIds.remove(serversHardwareIds.size()-1);
			int num = serversHardwareService.deleteServersHardwareList(serversHardwareIds);
			logger.info("服务器"+serverBasics.getServerName()+"共删除"+num+"条硬件信息");
		}
	}

	/**
	 * 删除运维库中的报警数据
	 */
	@Scheduled(cron = "0 0 4 * * ?")
	public void deleteAlarm() {
		try {
			Integer size = 500;
			Integer expireDay = 3;
			deleteAlarmInfo(expireDay,size);
			logger.info("删除报警信息定时任务完成");
		}catch(Exception e) {
			logger.error("定时删除运维数据库报警信息出错", e);
		}
	}
	
	
	private void deleteAlarmInfo(Integer expireDay, Integer size) {
		while(true) {
			List<Long> alarmIdList = alarmService.getAlarmInfoEarlist(expireDay,size);
			if(alarmIdList.size()==0) {
				return;
			}
			int num = alarmService.deleteAlarmInfoList(alarmIdList);
			logger.info("服务器共删除"+num+"条报警信息");
		}
	}

	/**
	 * 将已完成的流程历史数据设置到缓存中去
	 */
	@Scheduled(cron = "0/10 * * * * ?")
	public void setscheduleListExtCache() {
		try {
			// 获取历史数据服务对象
			HistoryService historyService = this.engine.getHistoryService();
			List<HistoricTaskInstance> doneList = historyService
					.createHistoricTaskInstanceQuery().finished().orderByTaskCreateTime().desc().list();//查询
			
			this.jedisDao.setList(CommonConstrant.REDIS_CACHE_TASK_DONE_LIST, doneList, TimeUtil.getSecondsByMinute(null));//将值设置到Redis中
		} catch (Exception e) {
			logger.error("将预约列表设置缓存失败 ===== DeleteHardwareTask ===== setscheduleListExtCache =====> ", e);
		}
	}

}
