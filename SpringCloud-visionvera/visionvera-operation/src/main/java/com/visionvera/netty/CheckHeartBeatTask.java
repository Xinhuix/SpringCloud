/*package com.visionvera.netty;

import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.slweoms.AlarmInfo;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.ywcore.SlweomsDao;
import com.visionvera.service.AlarmService;
import com.visionvera.service.ServerBasicsService;
import com.visionvera.util.ProbeDisplayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

*//**
 * 检测服务器心跳
 * @author dql
 *
 *//*
@Component
public class CheckHeartBeatTask {
	
	private Logger logger = LoggerFactory.getLogger(CheckHeartBeatTask.class);
	
	@Autowired
	private JRedisDao jRedisDao;
	@Autowired
	private SlweomsDao slweomsDao;
	
	@Autowired
	private ServerBasicsService serverBasicsService;
	@Autowired
	private AlarmService alarmService;
	
	@Value("${netty.heartbeat.checktime}")
	private Integer checkTime;
	
	@PostConstruct
	public void checkHeartBeat() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						Set<String> keys = jRedisDao.keys("servers_*");
						if(keys != null && keys.size() > 0) {
							for (String key : keys) {
								Long heartBeatTime = (Long)jRedisDao.getObject(key);
								Long nowTime = new Date().getTime();
								if(nowTime - heartBeatTime > checkTime *1000) {
									logger.info("服务器====" + key + "========已离线");
									try {
										handleServerOffLine(key);
									}catch(Exception e) {
										logger.error("设置服务器离线失败",e);
									}
								} else {
									logger.info("服务器====" + key + "====在线");
								}
							}
						}
						logger.info("检测服务器在线情况完成========="+ new Date());
						Thread.sleep(checkTime * 1000);
					} catch(Exception e) {
						logger.error("检测服务器在线情况出现异常",e);
					}
				}
			}

		}).start();
	}

	@Scheduled(cron = "0 0/1 * * * ?")
	public void checkHeartBeat() {
		//while(true) {
			try {
				Set<String> keys = jRedisDao.keys("servers_*");
				if(keys != null && keys.size() > 0) {
					for (String key : keys) {
						Long heartBeatTime = (Long)jRedisDao.getObject(key);
						Long nowTime = new Date().getTime();
						if(nowTime - heartBeatTime > checkTime *1000) {
							logger.info("服务器====" + key + "========已离线");
							handleServerOffLine(key);
						} else {
							logger.info("服务器====" + key + "====在线");
						}
					}
				}
				logger.info("检测服务器在线情况完成========="+ new Date());

			} catch(Exception e) {
				logger.error("检测服务器在线情况出现异常",e);
			}
		//}
	}
	
	private void handleServerOffLine(String serverUnique) {
		//设置服务器离线
		ServerBasics serverBasics = serverBasicsService.getServerBasicsByServerUnique(serverUnique);
		if(serverBasics == null) {
			return;
		}
		//当服务器处于关闭状态或者删除状态时，不产生离线报警
		if(ProbeDisplayUtil.PROBE_STATE_STOP.equals(serverBasics.getOpenState()) || ProbeDisplayUtil.PROBE_STATE_DELETE.equals(serverBasics.getState())) {
			jRedisDao.delObject(GlobalConstants.SERVER_ONLINE_STATUS_PREFIX+serverUnique);
			jRedisDao.delObject(serverUnique);
			return;
		}	
		Integer onLineStatus = serverBasics.getServerOnLine();
		if(GlobalConstants.SERVER_ONLINE_STATE.equals(onLineStatus)) {
			serverBasics.setServerOnLine(GlobalConstants.SERVER_OFFLINE_STATE);
			serverBasics.setOffLineTime(new Date());
			serverBasicsService.updateServerOnLine(serverBasics);
			jRedisDao.setObject(GlobalConstants.SERVER_ONLINE_STATUS_PREFIX+serverUnique, GlobalConstants.SERVER_OFFLINE_STATE, 604800);//默认7天
		}
		//产生报警
		String serverProvince = serverBasics.getServerProvince();
		RegionVO regionb = slweomsDao.getRegionVOById(serverProvince);
		String provinceRegionbName = regionb.getName();
		
		Long timestamp = System.currentTimeMillis();
		AlarmInfo alarm = new AlarmInfo();
		alarm.setEndpoint(serverBasics.getServerHostname());
		alarm.setMetric(serverBasics.getServerName());
		alarm.setNote(serverBasics.getServerName()+ " 已离线,"+provinceRegionbName +","+serverBasics.getServerManageIp());
		alarm.setStatus("PROBLEM");
		alarm.setTimestamp(timestamp);
		alarm.setRegisterid(serverBasics.getServerUnique());
		alarm.setAlarmType(6);
		alarm.setTreatmentStates(1);
		alarm.setAlarmLevel(1);
		alarm.setKindType(1);
		alarm.setState(0);
		alarm.setCreateTime(new Date());
		alarmService.insertAlarm(alarm);
	}
}
*/