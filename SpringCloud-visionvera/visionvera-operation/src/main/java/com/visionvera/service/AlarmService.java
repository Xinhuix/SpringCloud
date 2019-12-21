package com.visionvera.service;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.alarm.AlarmDomain;
import com.visionvera.bean.slweoms.AlarmInfo;
import com.visionvera.bean.slweoms.ServerHardwareVO;
import com.visionvera.enums.LocalAlarmTypes;

import java.util.List;
import java.util.Map;

/**
 * 报警Service
 * @author dql
 *
 */
public interface AlarmService {
	
	/**
	 * 获取服务器离线报警的ID
	 * @param serverUnique
	 * @return
	 */
	List<Integer> getServerOfflineAlarmIds(String serverUnique);
	
	/**
	 * 更新报警的处理状态
	 * @param alarmIds
	 */
	void updateAlarmTreatmentStates(List<Integer> alarmIds);
	
	/**
	 * 插入警报
	 * @param alarm
	 */
	void insertAlarm(AlarmInfo alarm);
	
	/**
	 * 产生离线报警
	 * @param message
	 */
	void insertAlarmOffLine(String message);
	
	/**
	 * 产生平台报警
	 * @param message
	 *//*
	void insertAlarmPlatform(String message);*/
	
	/**
	 * 检查应用服务器阈值，产生报警
	 * @param serverHardwareVO
	 */
	void checkServerHardwareVO(ServerHardwareVO serverHardwareVO) throws Exception;
	
	/**
	 * 查询指定条件下最早的报警信息
	 * @param expireDay  过期时间，单位day
	 * @param size 查询数量
	 * @return
	 */
	List<Long> getAlarmInfoEarlist(int expireDay, Integer size);
	
	/**
	 * 根据报警信息id删除报警
	 * @param alarmInfoList
	 * @return
	 */
	int deleteAlarmInfoList(List<Long> alarmInfoList);

    void insertAlarmConf(String message);

	/**
	 * 创建唐古拉配置监测告警
	 * @param msgObj
	 */
    void insertTglAlarm(JSONObject msgObj);

    Map<String, Object> addAlarm(AlarmDomain alarmDomain);

	/**
	 * 添加探针平台告警
	 * @param alarmDomain
	 */
	void addProbePlatformAlarm(AlarmDomain alarmDomain);
	/**
	 * 添加平台配置检测告警
	 * @param alarmDomain
	 */
	void addCheckConfigAlarm(JSONObject msgObj);
	/**
	 * 上报异常端口告警
	 * @param alarmDomain
	 */
	void reportExceptionPortAlarm(JSONObject msgObj);
	/**
	 * 上报异常进程告警
	 * @param alarmDomain
	 */
	void reportExceptionProcessAlarm(JSONObject msgObj);
	/**
	 * 上报异常进程告警
	 * @param alarmDomain
	 */
	void reportOfflineProcessAlarm(JSONObject msgObj);
	/**
	 * 上报唐古拉配置错误告警
	 * @param alarmDomain
	 */
	void reportTglConfigAlarm(JSONObject msgObj);

	AlarmDomain generateAlarm(String deviceId, String deviceName,LocalAlarmTypes alarmType, String state, String raiseTime,
									 String detail, String regionId, String source);
}
