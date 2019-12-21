package com.visionvera.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.alarm.AlarmDomain;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.slweoms.*;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.datacore.TerminalInfoDao;
import com.visionvera.dao.ywcore.*;
import com.visionvera.enums.LocalAlarmTypes;
import com.visionvera.mq.provider.RabbitProvider;
import com.visionvera.service.AlarmService;
import com.visionvera.service.ServerBasicsService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 报警Service
 * @author dql
 *
 */
@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class AlarmServiceImpl implements AlarmService {
	
	private Logger logger = LoggerFactory.getLogger(AlarmServiceImpl.class);
	
	@Autowired
	private ServerBasicsService serverBasicsService;
	
	@Autowired
	private AlarmDao alarmDao;
	
	@Autowired
	private JRedisDao jRedisDao;
	
	@Autowired
	private ServerBasicsDao serverBasicsDao;
	
	@Autowired
	private SlweomsDao slweomsDao;
	
	@Autowired
	private PlatformDao platformDao;
	
	@Autowired
	private PlatformProcessDao iProcessDao;
	@Autowired
	private TerminalInfoDao terminalInfoDao;

	@Autowired
	private com.visionvera.feign.AlarmService alarmService;
	@Autowired
	private RabbitProvider rabbitProvider;

	@Value("${sys_bit}")
	private Integer sysBit;
	@Value("${alarm_switch:false}")
	private Boolean alarmSwitch;
	
	@Override
	public List<Integer> getServerOfflineAlarmIds(String serverUnique) {
		return alarmDao.getServerOfflineAlarmIds(serverUnique);
	}

	@Override
	public void updateAlarmTreatmentStates(List<Integer> alarmIds) {
		alarmDao.updateAlarmTreatmentStates(alarmIds);
	}

	@Override
	public void insertAlarm(AlarmInfo alarm) {
		alarmDao.insertAlarm(alarm);
	}
	
	@Override
	public void checkServerHardwareVO(ServerHardwareVO serverHardwareVO) throws Exception{
		String serverUnique = serverHardwareVO.getServerUnique();
		if(StringUtil.isNull(serverUnique)) {
			logger.error("缺失服务器唯一标识");
			return;
		}
		
		String alarmTypes = jRedisDao.get(GlobalConstants.ALARM_REDIS_PREFIX + serverUnique);
		List<String> alarmTypeList;
		if(StringUtil.isNull(alarmTypes)) {
			alarmTypeList = new ArrayList<String>();
		}else {
			alarmTypeList = Arrays.asList(alarmTypes.split(","));
		}
		String newAlarmTypes = "";
		
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
		if(serverBasics==null){
			logger.warn("AlarmServiceImpl===checkServerHardwareVO===服务器信息不存在："+serverUnique);
			return;
		}
		String serverManageIp = serverBasics.getServerManageIp();
		String serverProvince = serverBasics.getServerProvince();
		RegionVO regionVO = slweomsDao.getRegionVOById(serverProvince);
		String provinceRegionName = regionVO.getName();
		
		List<AlarmDomain> alarmDomains = new ArrayList<>();
		String serverName = serverBasics.getServerName();
		String raiseTime = DateUtil.date2String("yyyy-MM-dd HH:mm:ss");
		String[] regionIdArr = serverBasics.getServerDistrict().split(",");
		//cpu检测
		String cpuThreshold = serverBasics.getCpuThreshold();
		if(StringUtil.isNotNull(cpuThreshold)) {
			String serverCPUSumRate = serverHardwareVO.getServerCPUSumRate();
			float cpuSumRateFloat = Float.parseFloat(serverCPUSumRate);
			float cpuThresholdFloat = Float.parseFloat(cpuThreshold);
			if(cpuSumRateFloat > cpuThresholdFloat) {
				String detail = StringUtil.removeSuffix(serverName
						+ "CPU使用率超过" + cpuThresholdFloat + "%,当前为"
						+ String.format("%.2f", cpuSumRateFloat) + "%,"
						+ provinceRegionName + "," + serverManageIp,",");
				newAlarmTypes += "cpu,";
				AlarmDomain alarmDomain = generateAlarm(serverUnique,serverName,LocalAlarmTypes.cpu,"default",raiseTime,detail,
						regionIdArr[regionIdArr.length-1],"OPERATION");
				alarmDomains.add(alarmDomain);
			}else if(alarmTypeList.contains("cpu")){
				//生成恢复
				String detail = "CPU使用率恢复正常，阈值：" + cpuThresholdFloat + "%,当前:" + String.format("%.2f", cpuSumRateFloat) + "%,"
						+ provinceRegionName;
				AlarmDomain alarmDomain = generateAlarm(serverUnique,serverName,LocalAlarmTypes.cpu,"clear",raiseTime,detail,
						regionIdArr[regionIdArr.length-1],"OPERATION");
				alarmDomains.add(alarmDomain);
			}
		}
		
		//内存检测
		String ddrThreshold = serverBasics.getDdrThreshold();
		if(StringUtil.isNotNull(ddrThreshold)) {
			String serverDDRRate = serverHardwareVO.getServerDDRRate();
			float ddrRateFloat = Float.parseFloat(serverDDRRate);
			float ddrThresholdFloat = Float.parseFloat(ddrThreshold);
			if(ddrRateFloat > ddrThresholdFloat) {
				String detail = StringUtil.removeSuffix(serverName
						+ "内存使用率超过" + ddrThresholdFloat + "%,当前为"
						+ String.format("%.2f", ddrRateFloat) + "%,"
						+ provinceRegionName + "," + serverManageIp,",");
				newAlarmTypes += "memory,";
				AlarmDomain alarmDomain = generateAlarm(serverUnique,serverName,LocalAlarmTypes.memory,"default",raiseTime,detail,
						regionIdArr[regionIdArr.length-1],"OPERATION");
				alarmDomains.add(alarmDomain);
			}else if(alarmTypeList.contains("memory")) {
				//生成恢复
				String detail = "内存使用率恢复，阈值：" + ddrThresholdFloat + "%,当前：" + String.format("%.2f", ddrRateFloat) + "%,"
						+ provinceRegionName;
				AlarmDomain alarmDomain = generateAlarm(serverUnique,serverName,LocalAlarmTypes.memory,"clear",raiseTime,detail,
						regionIdArr[regionIdArr.length-1],"OPERATION");
				alarmDomains.add(alarmDomain);
			}
		}
		
		//硬盘检测
		String hddThreshold = serverBasics.getHddThreshold();
		if(StringUtil.isNotNull(hddThreshold)) {
			String serverHDDAllRate = serverHardwareVO.getServerHDDAllRate();
			float hddAllRateFloat = Float.parseFloat(serverHDDAllRate);
			float hddThresholdFloat = Float.parseFloat(hddThreshold);
			if(hddAllRateFloat > hddThresholdFloat) {
				String detail = StringUtil.removeSuffix(serverName
						+ "硬盘使用率超过" + hddThresholdFloat + "%,当前为"
						+ String.format("%.2f", hddAllRateFloat) + "%,"
						+ provinceRegionName + "," + serverManageIp,",");
				newAlarmTypes += "disk,";
				//生成报警
				AlarmDomain alarmDomain = generateAlarm(serverUnique,serverName,LocalAlarmTypes.disk,"default",raiseTime,detail,
						regionIdArr[regionIdArr.length-1],"OPERATION");
				alarmDomains.add(alarmDomain);
			} else if(alarmTypeList.contains("disk")) {
				//生成恢复
				String detail = "硬盘使用率恢复，阈值：" + hddThresholdFloat + "%,当前为" + String.format("%.2f", hddAllRateFloat) + "%,"
						+ provinceRegionName;
				AlarmDomain alarmDomain = generateAlarm(serverUnique,serverName,LocalAlarmTypes.disk,"clear",raiseTime,detail,
						regionIdArr[regionIdArr.length-1],"OPERATION");
				alarmDomains.add(alarmDomain);
			}
		}
		
		//网络上下行检测
		String netCardName = serverHardwareVO.getNetCardName();
		String serverNETUpData = serverHardwareVO.getServerNETUpData();
		String serverNETDownData = serverHardwareVO.getServerNETDownData();
		String[] netCardNameArr = netCardName.split(",");
		String[] netUpDataArr = serverNETUpData.split(",");
		String[] netDownDataArr = serverNETDownData.split(",");
		
		String netUpThreshold = serverBasics.getNetUpThreshold();
		String netDownThreshold = serverBasics.getNetDownThreshold();
		String detail = "";
		for (int i = 0; i < netCardNameArr.length; i++) {
			String detailTemp = "";
			String upDataSpeed = netUpDataArr[i];
			String downDataSpeed = netDownDataArr[i];
			String netName = netCardNameArr[i];
			if(StringUtil.isNotNull(netUpThreshold)) {
				float upV = Float.parseFloat(netUpThreshold);
				float upSpeed = Float.parseFloat(upDataSpeed);
				if(upSpeed > upV) {
					detailTemp = "网卡" + netName + "上行速率超过" + upV + "kbps,当前为" + String.format("%.2f", upSpeed) + "kbps";
				}
			}
			if(StringUtil.isNotNull(netDownThreshold)) {
				float downV = Float.parseFloat(netDownThreshold);
				float downSpeed = Float.parseFloat(downDataSpeed);
				
				if(downSpeed > downV) {
					if(StringUtil.isNull(detailTemp)) {
						detailTemp = "网卡" + netName + "下行速率超过" + downV + "kbps,当前为" + String.format("%.2f", downSpeed) + "kbps";
					}else {
						detailTemp = detailTemp + ",下行速率超过" + downV + "kbps,当前为" + String.format("%.2f", downSpeed) + "kbps";
					}
				}
			}
			
			if(!StringUtil.isNull(detailTemp)) {
				detail = detail + detailTemp + ";";
			}
		}
		
		if(StringUtil.isNotNull(detail)) {
			detail = StringUtil.removeSuffix(serverName + detail + provinceRegionName + "," + serverManageIp,",");
			newAlarmTypes += "network,";
			//生成报警
			AlarmDomain alarmDomain = generateAlarm(serverUnique,serverName,LocalAlarmTypes.network,"default",raiseTime,detail,
					regionIdArr[regionIdArr.length-1],"OPERATION");
			alarmDomains.add(alarmDomain);
		}else if(alarmTypeList.contains("network")) {
			//生成恢复
			detail = "网卡报警已恢复，" + provinceRegionName;
			AlarmDomain alarmDomain = generateAlarm(serverUnique,serverName,LocalAlarmTypes.network,"clear",raiseTime,detail,
					regionIdArr[regionIdArr.length-1],"OPERATION");
			alarmDomains.add(alarmDomain);
		}
		
		//调用报警接口
		if(alarmDomains.size() > 0) {
			for (AlarmDomain alarmDomain : alarmDomains) {
				addAlarm(alarmDomain);
			}
			jRedisDao.set(GlobalConstants.ALARM_REDIS_PREFIX + serverUnique, newAlarmTypes, 432000);
		}
	}

	@Override
	public AlarmDomain generateAlarm(String deviceId, String deviceName, LocalAlarmTypes alarmType, String state, String raiseTime,
									  String detail, String regionId, String source) {
		AlarmDomain alarmDomain = new AlarmDomain();
		alarmDomain.setDeviceId(deviceId);
		alarmDomain.setDeviceName(deviceName);
		alarmDomain.setType(alarmType.getAlarmType());
		alarmDomain.setSubType(alarmType.getSubType());
		alarmDomain.setLevel(alarmType.getLevelId());
		alarmDomain.setState(state);
		alarmDomain.setRaiseTime(raiseTime);
		alarmDomain.setDetail(detail);
		alarmDomain.setRegionId(regionId);
		alarmDomain.setBits(sysBit);
		alarmDomain.setSource(source);
		return alarmDomain;
	}

	@Override
	public void insertAlarmOffLine(String message) {
		JSONObject msgObj = JSONObject.parseObject(message);
		JSONObject paramObj = msgObj.getJSONObject("param");
		JSONArray offLineArray = paramObj.getJSONArray("offLine");
		
		List<AlarmDomain> alarmDomains = new ArrayList<>();
		for (Object object : offLineArray) {
			JSONObject offLineObj = (JSONObject)object;
			String serverUnique = offLineObj.getString("uuid");
			String state = offLineObj.getString("state");
			
			ServerBasics serverBasics = serverBasicsService.getServerBasicsByServerUnique(serverUnique);
			if(serverBasics==null){
				logger.warn("AlarmServiceImpl===insertAlarmOffLine===服务器信息不存在或已删除:"+serverUnique);
				continue;
			}
			if(GlobalConstants.SERVER_UNDISPLOY_STATE == serverBasics.getState()){
				
				logger.info("AlarmServiceImpl===insertAlarmOffLine===探针服务器未部署:"+serverUnique);
				continue;
			}
			String serverProvince = serverBasics.getServerProvince();
			String[] regionIdArr = serverBasics.getServerDistrict().split(",");
			RegionVO regionVO = slweomsDao.getRegionVOById(serverProvince);
			String provinceName = regionVO.getName();
			
			AlarmDomain alarmDomain;
			Date currentDate = new Date();
			if(GlobalConstants.ALARM_PROBLEM.equals(state)) {
				serverBasics.setServerOnLine(GlobalConstants.SERVER_OFFLINE_STATE);
				serverBasics.setOffLineTime(currentDate);
				serverBasics.setModifyTime(currentDate);
				serverBasicsDao.updateServerBasic(serverBasics);
				
				String detail = serverBasics.getServerName() + "已离线," + provinceName;
				alarmDomain = generateAlarm(serverUnique,serverBasics.getServerName(),LocalAlarmTypes.offline,"default",DateUtil.date2String(currentDate),
						detail,regionIdArr[regionIdArr.length-1],"OPERATION");
			}else {
				serverBasics.setServerOnLine(GlobalConstants.SERVER_ONLINE_STATE);
				serverBasics.setOnLineStartTime(currentDate);
				serverBasics.setModifyTime(currentDate);
				serverBasicsDao.updateServerBasic(serverBasics);
				
				String detail = serverBasics.getServerName() + "离线已恢复," + provinceName;
				alarmDomain = generateAlarm(serverUnique,serverBasics.getServerName(),LocalAlarmTypes.offline,"clear",DateUtil.date2String(currentDate),
						detail,regionIdArr[regionIdArr.length-1],"OPERATION");
			}
			alarmDomains.add(alarmDomain);
		}
		if(alarmDomains.size() > 0) {
			for (AlarmDomain alarmDomain : alarmDomains) {
				addAlarm(alarmDomain);
			}
		}
	}

	/*@Override
	public void insertAlarmPlatform(String message) {
		JSONObject msgObj = JSONObject.parseObject(message);
		String serverUnique = msgObj.getString("uuid");
		ServerBasics serverBasics = serverBasicsService.getServerBasicsByServerUnique(serverUnique);
		if(serverBasics==null){
			logger.warn("AlarmServiceImpl===insertAlarmPlatform===服务器信息不存在："+serverUnique);
			return;
		}
		
		if(ProbeDisplayUtil.PROBE_STATE_STOP.equals(serverBasics.getOpenState()) 
				|| ProbeDisplayUtil.PROBE_STATE_DELETE.equals(serverBasics.getState())){
			return;
		}
		JSONObject paramObj = msgObj.getJSONObject("param");
		JSONArray processArr = paramObj.getJSONArray("platform");
		
		Map<Integer,String> processMap = new HashMap<Integer,String>();
		for (Object processObj : processArr) {
			JSONObject process = (JSONObject)processObj;
			Integer processId = Integer.valueOf(process.getString("processId"));
			String status = process.getString("status");
			processMap.put(processId, status);
		}
		
		List<PlatformVO> platformVOList = platformDao.getPlatformListByServerUnique(serverUnique);
		
		List<AlarmDomain> alarmDomains = new ArrayList<>();
		for (PlatformVO platformTpos : platformVOList) {
			AlarmDomain alarm = checkPlatformTpos(processMap,serverBasics,platformTpos);
			if(alarm != null) {
				if("default".equals(alarm.getState())) {
					jRedisDao.set(GlobalConstants.ALARM_REDIS_PREFIX +platformTpos.getTposRegisterid(),"true",604800);
				}else if("clear".equals(alarm.getState())) {
					jRedisDao.del(GlobalConstants.ALARM_REDIS_PREFIX + platformTpos.getTposRegisterid());
				}
				alarmDomains.add(alarm);
			}
		}
		if(alarmDomains.size()>0) {
			for (AlarmDomain alarmDomain : alarmDomains) {
				addAlarm(alarmDomain);
			}
		}
	}*/
	
	/*//检查各平台状态，生成报警
	private AlarmDomain checkPlatformTpos(Map<Integer, String> processMap, ServerBasics serverBasics,PlatformVO platformTpos) {
		boolean alarmFlag = false;     //平台是否报警
		String tposRegisterid = platformTpos.getTposRegisterid();
		List<PlatformProcess> processes = iProcessDao.getProcessByTposRegisterid(tposRegisterid);
		String abnormalProcessName = "";
		for (PlatformProcess platformProcess : processes) {
			Integer processId = platformProcess.getId();
			String processStatus = platformProcess.getProcessStatus();
			String status = processMap.get(processId);
			if(GlobalConstants.ALARM_PROBLEM.equals(status)) {
				alarmFlag = true;
				if(!GlobalConstants.ALARM_PROBLEM.equals(processStatus)) {
					//更新进程状态
					platformProcess.setProcessStatus(GlobalConstants.PROCESS_STATUS_PROBLEM);
					iProcessDao.updatePlatformProcess(platformProcess);
				}
				logger.info("=====进程名称："+platformProcess.getShowName());
				abnormalProcessName = abnormalProcessName + platformProcess.getShowName() + ",";
			}else if(GlobalConstants.ALARM_OK.equals(status)) {
				//alarmFlag = true;
				String retFlag = jRedisDao.get(GlobalConstants.ALARM_REDIS_PREFIX+tposRegisterid);
				//if(!GlobalConstants.ALARM_OK.equals(processStatus)) {
				if(StringUtil.isNotNull(retFlag) && Boolean.parseBoolean(retFlag)) {
					alarmFlag = true;
					//更新进程状态
					platformProcess.setProcessStatus(GlobalConstants.PROCESS_STATUS_OK);
					iProcessDao.updatePlatformProcess(platformProcess);
				}
			}
		}
		
		if(alarmFlag) {
			String detail;
			String state;
			String[] regionIdArr = serverBasics.getServerDistrict().split(",");
			if(StringUtil.isNotNull(abnormalProcessName)) {
				//异常报警
				detail = serverBasics.getServerName() + "中的" + platformTpos.getTposName() + "的" + StringUtil.removeSuffix(abnormalProcessName, ",") + "进程异常";
				state = "default";
			} else {
				//恢复报警
				detail = serverBasics.getServerName() + "中的" + platformTpos.getTposName() + "的进程恢复";
				state = "clear";
			}
			return generateAlarm(tposRegisterid,platformTpos.getTposName(),LocalAlarmTypes.softabnormal,state,DateUtil.date2String("yyyy-MM-dd HH:mm:ss"),
					detail,regionIdArr[regionIdArr.length-1],"OPERATION");
		}
		return null;
	}*/

	/*private AlarmInfo generateAlarm(ServerBasics serverBasics, PlatformVO platformTpos,String detail, String alarmStatus, int alarmType, int alarmLevel,
			int kindType) {
		AlarmInfo alarm = new AlarmInfo();
		alarm.setEndpoint(serverBasics.getServerHostname());
		alarm.setMetric(serverBasics.getServerName());
		if(StringUtil.isNull(serverBasics.getServerManageIp())) {
			detail = StringUtil.removeSuffix(detail,",");
		}
		alarm.setNote(detail);
		alarm.setStatus(alarmStatus);
		alarm.setTimestamp(System.currentTimeMillis());
		if(platformTpos != null) {
			alarm.setRegisterid(platformTpos.getTposRegisterid());
		}else {
			alarm.setRegisterid(serverBasics.getServerUnique());
		}
		alarm.setAlarmType(alarmType);
		alarm.setTreatmentStates(1);
		alarm.setAlarmLevel(alarmLevel);
		alarm.setKindType(kindType);
		alarm.setState(0);
		alarm.setCreateTime(new Date());
		return alarm;
	}*/
	
	@Override
	public List<Long> getAlarmInfoEarlist(int expireDay, Integer size) {
		List<Long> alarmIds = alarmDao.getAlarmInfoEarlist(expireDay,size);
		return alarmIds;
	}

	@Override
	public int deleteAlarmInfoList(List<Long> alarmIdList) {
		int num = alarmDao.deleteAlarmInfoList(alarmIdList);
		return num;
	}

	@Override
	public void insertAlarmConf(String message) {
		JSONObject mesObj = JSONObject.parseObject(message);
		String deviceId = mesObj.getString("deviceId");
		String bits = mesObj.getString("bits");
		PlatformVO platformVO = platformDao.getPlatformByTposRegisterid(deviceId);
		if(platformVO==null || platformVO.getConfCheck() == 0){
			return;
		}
		String abbreviation = platformVO.getAbbreviation();
		switch(abbreviation) {
			case GlobalConstants.PLATFORM_GIS_SIGN:
				JSONArray checkItemArr = mesObj.getJSONArray("list");
				StringBuilder sb = new StringBuilder(platformVO.getTposName()).append("平台");
				String status="clear";
				String detail;
				if(checkItemArr != null && checkItemArr.size() > 0) {
					//告警
					sb.append("下列配置异常\"");
					for (Object obj : checkItemArr) {
						JSONObject checkItem = (JSONObject)obj;
						String operationName = checkItem.getString("operationName");
						Integer state = checkItem.getInteger("state");
						if(state == -1) {
							sb.append(operationName).append(";");
						}
					}
					detail = sb.append("\"").substring(0, sb.length() - 1)+"\"";
					status="default";
				}else {
					//产生恢告警
					detail = sb.append("配置恢复正常").toString();
				}
				generateConfAlarm(platformVO,LocalAlarmTypes.softconfig.getAlarmType(),LocalAlarmTypes.softconfig.getSubType(),
						status,Integer.valueOf(bits),GlobalConstants.PLATFORM_GIS_SIGN,detail);
				break;
			default:
		}
	}

	/**
	 * 创建唐古拉配置监测告警
	 *
	 * @param msgObj
	 */
	@Override
	public void insertTglAlarm(JSONObject msgObj) {
		JSONObject param = msgObj.getJSONObject("param");
		String platid =msgObj.getString("platid");
		PlatformVO platformVO = platformDao.getPlatformByTposRegisterid(platid);
		if(platformVO==null || platformVO.getConfCheck() == 0){
			logger.warn("唐古拉配置监测告警平台不存在:"+platid);
			return;
		}
		String status="clear";
		StringBuilder sb = new StringBuilder();
		
		String v2vMac =param.getString("v2vMac");
		String v2vNum =param.getString("v2vNum");
		v2vNum = v2vNum.replaceAll("^(0+)", "");//去除设备号码前面补的0
		Integer terminalCount = terminalInfoDao.selectTerminalInfo(v2vMac,v2vNum);
		if (terminalCount<=0){
			sb.append("Mserver入网虚拟号码、Mserver入网虚拟MAC").append(";");
		}
		if(param.containsKey("commuAddr")&&StringUtil.isNotNull(param.getString("commuAddr"))){
			sb.append("Mserver通信地址").append(";");
		}
		if(param.containsKey("signalAddr")&&StringUtil.isNotNull(param.getString("signalAddr"))){
			sb.append("Mserver信令地址").append(";");
		}
		if(param.containsKey("webAddr")&&StringUtil.isNotNull(param.getString("webAddr"))){
			sb.append("网站地址").append(";");
		}
		if(param.containsKey("serverAddr")&&StringUtil.isNotNull(param.getString("serverAddr"))){
			sb.append("Mserver视联网MAC").append(";");
		}
		if (param.containsKey("dispAddr") && param.getJSONArray("dispAddr").size() > 0) {
			JSONArray dispAddr = param.getJSONArray("dispAddr");
			for (int i = 0; i < dispAddr.size(); i++) {
				if(StringUtil.isNotNull(dispAddr.getJSONObject(i).getString("info"))){
					sb.append("视联网调度基础服务").append(";");
					continue;
				}
			}
		}
		if (sb.length()>0){
			sb.insert(0,platformVO.getTposName()+"平台下列配置异常\"");
			sb.append("\"");
			status="default";
		}
		Map<String,Object> result = generateConfAlarm(platformVO,LocalAlarmTypes.softconfig.getAlarmType(),LocalAlarmTypes.softconfig.getSubType(),
				status,16,GlobalConstants.PLATFORM_GIS_SIGN,sb.toString());
		logger.info("创建唐古拉配置监测告警Result:"+result);
	}
	
	/**
	 * 创建GIS配置监测告警
	 *
	 * @param msgObj
	 */
	@Override
	public void addCheckConfigAlarm(JSONObject msgObj) {
		String platid =msgObj.getString("platid");
		PlatformVO platformVO = platformDao.getPlatformByTposRegisterid(platid);
		if(platformVO==null || platformVO.getConfCheck() == 0){
			logger.warn("AlarmServiceImpl===addCheckConfigAlarm===平台信息不存在:"+platid);
			return;
		}
		String status="default";
		if ("1".equals(msgObj.getString("ret"))) {
			status="clear";
		}
		Map<String,Object> result =generateConfAlarm(platformVO,LocalAlarmTypes.softconfig.getAlarmType(),LocalAlarmTypes.softconfig.getSubType(),
				status,16,GlobalConstants.PLATFORM_GIS_SIGN,msgObj.getString("msg"));
		logger.info("创建GIS配置监测告警Result:"+result);
		
	}
	

	/**
	 * 产生检查平台配置的告警
	 * @param platformVO  平台
	 * @param alarmType  告警类型
	 * @param subType  告警子类型
	 * @param state  告警状态
	 * @param bits  告警系统位数，16和64
	 * @param platformType 平台类型
	 * @param detail 告警内容
	 */
	private Map<String,Object> generateConfAlarm(PlatformVO platformVO, String alarmType, String subType, String state, int bits,
								   String platformType,String detail) {
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			String serverUnique =platformVO.getServerUnique();
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
			if(serverBasics==null){
				logger.warn("AlarmServiceImpl===generateConfAlarm===服务器信息不存在:"+serverUnique);
				result.put("retcode", 1);
				result.put("retmsg", "配置告警失败，服务器信息不存在");
				return result;
			}
			String[] regionIds = serverBasics.getServerDistrict().split(",");
			StringBuilder sb =new StringBuilder();
			for (String regionId : regionIds) {
				if(StringUtil.isNotNull(regionId)) {
					RegionVO regionVOById = slweomsDao.getRegionVOById(regionId);
					sb.append(regionVOById.getName());
				}
			}
			AlarmDomain alarmDomain = new AlarmDomain();
			alarmDomain.setDeviceId(platformVO.getTposRegisterid());
			alarmDomain.setType(alarmType);
			alarmDomain.setSubType(subType);
			alarmDomain.setState(state);
			alarmDomain.setRaiseTime(DateUtil.date2String(new Date()));
			alarmDomain.setRegionId(regionIds[regionIds.length - 1]);
			alarmDomain.setDeviceName(platformVO.getTposName() + "(" + platformVO.getTposIp() + ")");
			alarmDomain.setPlatformId("platform");
			alarmDomain.setPlatformName("应用服务器");
			alarmDomain.setBits(bits);
			alarmDomain.setSource(platformType);
			alarmDomain.setDetail(sb.append(detail).toString());
			//插入
			result = addAlarm(alarmDomain);
		} catch (Exception e) {
			logger.error("AlarmServiceImpl===generateConfAlarm===检查平台配置的告警失败",e);
			result.put("retcode", 1);
			result.put("retmsg", "配置告警异常");
		}
		return result;

	}
	
	
	@Override
	public Map<String,Object> addAlarm(AlarmDomain alarmDomain){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		
		//如果是64位运维工作站且开关开启则告警不上报
		if(sysBit==64&&!alarmSwitch){
			resultMap.put("errcode", "1");
			resultMap.put("errmsg", "64位系统告警开关关闭，告警不上报");
			return resultMap;
		}
        /*String alarmId = alarmDomain.getDeviceId()+":"+alarmDomain.getType()+":"+ alarmDomain.getSubType();
		String alarmFlag = jRedisDao.get(GlobalConstants.ALARM_REDIS_PREFIX+alarmId);
		//判断之前是否有过告警，如果有需要恢复
		if ("clear".equals(alarmDomain.getState())) {
			if(StringUtil.isNull(alarmFlag)) {
				resultMap.put("errcode", "1");
				resultMap.put("errmsg", "告警不存在或已恢复");
				return resultMap;
			}
			alarmDomain.setClearMode("auto");
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			alarmDomain.setClearTime(sdf.format(new Date()));
		}
		resultMap = alarmService.addAlarm(alarmDomain);
		//0成功，1失败
		Integer errcode = (Integer) resultMap.get("errcode");
		if (errcode == 0 && "default".equals(alarmDomain.getState())) {
			//保存到redis 604800=7*24*60*60
			jRedisDao.set(GlobalConstants.ALARM_REDIS_PREFIX + alarmId, "true", 604800);
		} else if (errcode == 0 && "clear".equals(alarmDomain.getState())) {
			jRedisDao.del(GlobalConstants.ALARM_REDIS_PREFIX + alarmId);
		}*/
		rabbitProvider.sendAlarmMessage(alarmDomain);
		
		
		return resultMap;
	}

	/**
	 * 协转版本不一致上报告警或恢复告警 告警示例：{"funcName":"ProbePlatformAlarm","uuid":"servers_313e3850-55d9-41ee-a9d3-980647e3bad5",
	 * "param":{"deviceId":"slw-JQ02-4NnLM8XEKtWm","state":"default","detail":"版本不一致：当前版本 5.16.3.0, 推荐版本 {\"version\":\"1.1.12\"}.","source":"SLWXZ"}}
	 */
	@Override
	public void addProbePlatformAlarm(AlarmDomain alarmDomain) {
		try {
			String platformId = alarmDomain.getDeviceId();
		    PlatformVO platform = platformDao.getPlatformByTposRegisterid(platformId);
		    if(platform==null){
		    	 logger.warn("AlarmServiceImpl===addProbePlatformAlarm===协转版本不一致上报告警平台信息不存在："+platformId); 
		    	 return;
		    }
			 String serverUnique =platform.getServerUnique();
			 ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
			 if(serverBasics==null){
				 logger.warn("AlarmServiceImpl===addProbePlatformAlarm===协转版本不一致上报告警服务器信息不存在："+serverUnique); 
				 return;
			 }
    		String[] regionIds = serverBasics.getServerDistrict().split(",");
    		if(regionIds!=null){
    			alarmDomain.setRegionId(regionIds[regionIds.length-1]);
    		}
	        alarmDomain.setDeviceName(platform.getTposName()+"("+platform.getTposIp()+")");
	        alarmDomain.setDeviceInfo(platform.getTposName());
	        alarmDomain.setBits(sysBit);
	        alarmDomain.setType("platform");
	        alarmDomain.setSubType("softversion");
	    	alarmDomain.setSource("OPERATION");
	        alarmDomain.setDetail(serverBasics.getServerName()+","+platform.getTposName()+alarmDomain.getDetail());
	        alarmDomain.setRaiseTime(StringUtil.isNull(alarmDomain.getRaiseTime())?DateUtil.date2String(new Date()):alarmDomain.getRaiseTime());
	        Map<String,Object> result = addAlarm(alarmDomain); 
	        logger.info("协转版本不一致上报告警或恢复告警结果："+result);
	        return;
	       
		} catch (Exception e) {
			logger.error("AlarmServiceImpl===addProbePlatformAlarm===协转版本不一致上报告警或恢复告警异常:",e);
		}
     
    }

	/**
	 * 异常进程上报告警   探针管理上报数据示例：{"funcName":"ProcessInfo","uuid":"servers_29df3a9e-6a44-4df1-8f0c-5177d766b363",
     * "param":{"name":"Bbeagle.exe","msg":"存在异常进程，异常进程为[Bbeagle.exe]"}}
	 */
	@Override
	public void reportExceptionProcessAlarm(JSONObject msgObj) {

		try {
			AlarmDomain alarmDomain = new AlarmDomain();
			String serverUnique = msgObj.getString("uuid");
			JSONObject param = msgObj.getJSONObject("param");
			String processName = param.getString("name");
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
			 if(serverBasics==null){
				 logger.warn("AlarmServiceImpl===reportExceptionProcessAlarm===异常进程上报告警服务器信息不存在："+serverUnique); 
				 return;
			 }
			String serverName = serverBasics.getServerName();
			String[] regionIds = serverBasics.getServerDistrict().split(",");
			alarmDomain.setDeviceName(serverName);
			alarmDomain.setRegionId(serverBasics.getServerProvince());
			if (regionIds != null) {
				alarmDomain.setRegionId(regionIds[regionIds.length - 1]);// 取行政区域最后一级
			}
			alarmDomain.setDetail("[" + serverName + "]" + param.getString("msg"));
			if ("0".equals(param.getString("ret"))) {
				alarmDomain.setState("default");
			} else {
				alarmDomain.setState("clear");
			}
			alarmDomain.setDeviceId(serverUnique + ":" + processName);
			alarmDomain.setType("platform");
			alarmDomain.setSubType("abnormalProcess");
			alarmDomain.setRaiseTime(DateUtil.date2String(new Date()));
			alarmDomain.setLevel("major");
			alarmDomain.setBits(sysBit);
			alarmDomain.setSource("OPERATION");
			// 插入
			Map<String,Object> result = addAlarm(alarmDomain);
			logger.info("异常进程上报告警结果："+result);

		} catch (Exception e) {
			logger.error("AlarmServiceImpl===reportExceptionProcessAlarm===异常进程上报告警异常", e);
		}

	}

	/**
	 * 异常端口上报告警  探针管理上报数据示例 {"funcName":"PortInfo","uuid":"servers_29df3a9e-6a44-4df1-8f0c-5177d766b363",
     * "param":{"port":"80","msg":"存在异常端口占用，异常端口为[80]"}}
	 */
	@Override
	public void reportExceptionPortAlarm(JSONObject msgObj) {
		try {
			AlarmDomain alarmDomain = new AlarmDomain();
			String serverUnique = msgObj.getString("uuid");
			JSONObject param = msgObj.getJSONObject("param");
			String port = param.getString("port");
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
			 if(serverBasics==null){
				 logger.warn("AlarmServiceImpl===reportExceptionPortAlarm===异常端口上报告警服务器信息不存在："+serverUnique); 
				 return;
			 }
			String serverName = serverBasics.getServerName();
			String[] regionIds = serverBasics.getServerDistrict().split(",");
			alarmDomain.setDeviceName(serverName);
			alarmDomain.setRegionId(serverBasics.getServerProvince());
			if (regionIds != null) {
				alarmDomain.setRegionId(regionIds[regionIds.length - 1]);// 取行政区域最后一级
			}
			alarmDomain.setDetail("[" + serverName + "]" + param.getString("msg"));
			if ("0".equals(param.getString("ret"))) {
				alarmDomain.setState("default");
			} else {
				alarmDomain.setState("clear");
			}
			alarmDomain.setDeviceId(serverUnique);
			alarmDomain.setType("platform");
			alarmDomain.setSubType("abnormalPort");
			alarmDomain.setRaiseTime(DateUtil.date2String(new Date()));
			alarmDomain.setLevel("major");
			alarmDomain.setBits(sysBit);
			alarmDomain.setSource("OPERATION");
			// 插入
			Map<String,Object> result = addAlarm(alarmDomain);
			logger.info("异常端口上报告警结果："+result);

		} catch (Exception e) {
			logger.error("AlarmServiceImpl===reportExceptionPortAlarm===异常端口上报告警异常", e);
		}

	}
	
	
	
	/**
	 * 离线进程上报告警   探针管理上报数据示例：{"funcName":"ProcessInfoAlarm","uuid":"servers_a15c62aa-2600-40dc-b724-8885ac282aae",
	 * "param":{"processId":"39","type":"4","name":"cmsserver","state":"0","msg":"进程离线"}}
	 */
	@Override
	public void reportOfflineProcessAlarm(JSONObject msgObj) {
		try {
			AlarmDomain alarmDomain = new AlarmDomain();
			String serverUnique = msgObj.getString("uuid");
			JSONObject param = msgObj.getJSONObject("param");
			String processId = param.getString("processId");
			String processName = param.getString("name");
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
			 if(serverBasics==null){
				 logger.warn("AlarmServiceImpl===reportOfflineProcessAlarm===离线进程上报告警服务器信息不存在："+serverUnique); 
				 return;
			 }
			Map<String,Object> platformProcessMap = platformDao.getPlatformProcessByProcessId(processId);
			if(platformProcessMap==null){
				 logger.warn("AlarmServiceImpl===reportOfflineProcessAlarm===离线进程上报告警平台或进程不存在："+processId); 
				 return;
			}
			String serverName = serverBasics.getServerName();
			String platformName = (String) platformProcessMap.get("tposName");
			String[] regionIds = serverBasics.getServerDistrict().split(",");
			alarmDomain.setDeviceName(platformName);
			alarmDomain.setRegionId(serverBasics.getServerProvince());
			if (regionIds != null) {
				alarmDomain.setRegionId(regionIds[regionIds.length - 1]);// 取行政区域最后一级
			}
			alarmDomain.setDetail( serverName + "中的"+platformName +"的"+processName+ param.getString("msg"));
			if ("0".equals(param.getString("state"))) {
				alarmDomain.setState("default");
			} else {
				alarmDomain.setState("clear");
			}
			alarmDomain.setDeviceId(serverUnique + ":" + processId);
			alarmDomain.setType("platform");
			alarmDomain.setSubType("softabnormal");
			alarmDomain.setRaiseTime(DateUtil.date2String(new Date()));
			alarmDomain.setLevel("critical");
			alarmDomain.setBits(sysBit);
			alarmDomain.setSource("OPERATION");
			// 插入
			Map<String,Object> result = addAlarm(alarmDomain);
			logger.info("离线进程上报告警结果："+result);

		} catch (Exception e) {
			logger.error("AlarmServiceImpl===reportOfflineProcessAlarm===离线进程上报告警异常", e);
		}

	}

	/**
	 * 转发唐古拉告警  告警示例：{"funcName":"TglAlarm","uuid":"servers_4c301cb3-daa1-48a7b19","platid":"","param":{"deviceId":"servers_96","type":"device","subType":"cpu","state":"default",
     *  "raiseTime":"2019-01-14 10:13:24","regionId":"110101001014","deviceName":"应用服务器","deviceInfo":"","detail":"CPU超过阈值哈哈","bits":16,"source":"TGL"} }
	 */
	@Override
	public void reportTglConfigAlarm(JSONObject msgObj) {
		try {
			String serverUnique = msgObj.getString("uuid");
			String platid = msgObj.getString("platid");
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
			if(serverBasics==null){
				 logger.warn("AlarmServiceImpl===reportTglConfigAlarm===转发唐古拉告警服务器信息不存在："+serverUnique); 
				 return;
			 }
			AlarmDomain alarmDomain = msgObj.getJSONObject("param").toJavaObject(AlarmDomain.class);
			alarmDomain.setDeviceId(platid);
			alarmDomain.setRaiseTime(DateUtil.date2String(new Date()));
			alarmDomain.setBits(sysBit);
			
			String[] regionIds = serverBasics.getServerDistrict().split(",");
			if(regionIds!=null){
				alarmDomain.setRegionId(regionIds[regionIds.length - 1]);
			}
	        String detail = alarmDomain.getDetail();
	        if(detail.substring(detail.length()-1).equals("、")){
	            detail=detail.substring(0,detail.length()-1);
	        }
	        alarmDomain.setDetail(detail);
	        Map<String,Object> result = alarmService.addAlarm(alarmDomain);
	        logger.info("转发唐古拉告警结果："+result);
			
		} catch (Exception e) {
			 logger.error("转发唐古拉告警异常",e);
		}
		
		
	}

}
