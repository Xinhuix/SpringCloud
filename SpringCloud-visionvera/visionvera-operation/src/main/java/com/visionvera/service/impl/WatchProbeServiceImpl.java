package com.visionvera.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.alarm.AlarmDomain;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.ywcore.*;
import com.visionvera.enums.LocalAlarmTypes;
import com.visionvera.enums.OperationSystem;
import com.visionvera.enums.TransferType;
import com.visionvera.service.AlarmService;
import com.visionvera.service.WatchProbeService;
import com.visionvera.util.*;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 监测探针Service
 * @author dql
 *
 */
@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class WatchProbeServiceImpl extends BaseReturn implements WatchProbeService {
	
	private Logger logger = LoggerFactory.getLogger(WatchProbeServiceImpl.class);
	
	@Autowired
	private ServerBasicsDao serverBasicsDao;
	
	@Autowired
	private AlarmDao alarmDao;
	
	@Autowired
	private PlatformDao platformDao;
	@Autowired
	private SlweomsDao slweomsDao;
	
	@Autowired
	private PlatformProcessDao processDao;
	@Autowired
	private AlarmService alarmService;
	
	@Value("${probe.installPack_path}")
	private String installPackPath;
	
	@Override
	public ReturnData getDeployedVersion(Map<String, Object> paramMap) {
		List<String> versionList = serverBasicsDao.getDeployedVersion(paramMap);
		return super.returnResult(0, "获取已部署监测探针版本号成功", null, versionList);
	}

	@Override
	public Map<String,Object> displayWatchProbe(Integer id,String version) throws Exception{
		Map<String,Object> resultMap = new HashMap<>();

		ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(id);
		String serverOs = serverBasics.getServerOs();
		String transferType = serverBasics.getTransferType();

		String oldVersion = serverBasics.getVersion();
		if(version.equals(oldVersion)) {
			resultMap.put("ret",true);
			resultMap.put("msg","服务器探针已是最新版本");
			resultMap.put("version",version);
			return resultMap;
		}
		
		if(OperationSystem.Windows.getDesc().equals(serverOs) || TransferType.V2V.equals(transferType)) {
			logger.error("ID为"+id+"的服务器操作系统为："+serverOs+",传输协议为："+transferType);
			resultMap.put("ret",false);
			resultMap.put("msg","远程部署当前只支持Linux/IP协议的环境");
			resultMap.put("version",oldVersion);
			return resultMap;
		}
		
		String manageIp = serverBasics.getServerManageIp();
		String user = serverBasics.getUser();
		logger.info(manageIp+"远程登录密码解密前为=="+serverBasics.getPassword());
		String password = AesUtils.decrypt(serverBasics.getPassword());
		boolean initRet = ProbeDisplayUtil.initProbeEnv(manageIp, user, password);
		if(!initRet) {
			resultMap.put("ret",false);
			resultMap.put("msg","部署失败，失败原因：初试化监测探针环境失败");
			resultMap.put("version",oldVersion);
			return resultMap;
		}

		ProbeDisplayUtil.stopProbe(manageIp,user,password);

		//封装安装包
		String tempFolder = installPackPath + File.separator + System.currentTimeMillis();
		String installFileName = "ipprobemonitor_V"+version+".zip";
		String installFilePath = buildIpProbeMonitorInstallPackage(serverBasics,tempFolder,installFileName);
		
		Map<String, Object> retMap;
		try {
			retMap = ProbeDisplayUtil.displayProbe(manageIp, user, password, installFilePath);
		}finally {
			//删除安装包
			FileUtils.forceDelete(new File(tempFolder));			
		}
		if((boolean) retMap.get("ret")) {
			if(ProbeDisplayUtil.startProbe(manageIp, user, password)){
				serverBasics.setState(ProbeDisplayUtil.PROBE_STATE_DIPLAYED);
				serverBasics.setVersion(version);
				serverBasicsDao.updateServerBasic(serverBasics);
				resultMap.put("ret",true);
				resultMap.put("msg",(String)retMap.get("msg"));
				resultMap.put("version",version);
				//return super.returnResult(0, (String)retMap.get("msg"), null, null, version);
			}else {
				resultMap.put("ret",true);
				resultMap.put("msg","部署失败，失败原因：启动失败");
				resultMap.put("version",oldVersion);
				//return super.returnError("部署失败，失败原因：启动失败");
			}
		}else {
			//return super.returnError((String)retMap.get("msg"));
			resultMap.put("ret",false);
			resultMap.put("msg",(String)retMap.get("msg"));
			resultMap.put("version",oldVersion);
		}
		return resultMap;
	}

	@Override
	public ReturnData testRemoteLogin(Integer id) throws Exception{
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(id);
		String serverOs = serverBasics.getServerOs();
		String transferType = serverBasics.getTransferType();
		
		if(OperationSystem.Windows.getDesc().equals(serverOs) 
				|| TransferType.V2V.getTransferType().equals(transferType)) {
			logger.error("ID为"+id+"的服务器操作系统为:"+serverOs+",传输协议为:"+transferType);
			return super.returnError("当前只支持Linux/IP环境支持远程登录");
		}
		
		String user = serverBasics.getUser();
		String manageIp = serverBasics.getServerManageIp();
		logger.info(manageIp+"远程登录密码解密前为=="+serverBasics.getPassword());
		String password = AesUtils.decrypt(serverBasics.getPassword());
		if(StringUtil.isNull(user) || StringUtil.isNull(password) || StringUtil.isNull(manageIp)) {
			return super.returnError("登录失败：用户名，密码或者管理ip为空");
		}
		
		boolean loginFlag = ProbeDisplayUtil.testRemoteLogin(manageIp, user, password);
		if(loginFlag) {
			return super.returnSuccess("登录成功");
		}else {
			return super.returnError("登录失败");
		}
	}

	@Override
	public ReturnData startWatchProbe(Integer id) throws Exception{
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(id);
		String transferType = serverBasics.getTransferType();
		if(TransferType.IP.getTransferType().equals(transferType)) {
			boolean retFlag = IPProbeReqUtil.startProbeCheck(serverBasics);
			if(!retFlag) {
				logger.info("开启检测探针监测失败");
				return super.returnError("启动监测探针失败,请检查探针是否在线和网络状态");
			}
			serverBasics.setOpenState(ProbeDisplayUtil.PROBE_STATE_OPEN);
			serverBasicsDao.updateServerBasic(serverBasics);
			return super.returnSuccess("启动监测探针成功");			
		}
		return super.returnError("启动监测探针失败，不支持的传输协议");
	}


	@Override
	public ReturnData stopWatchProbe(Integer id) throws Exception{
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(id);
		String transferType = serverBasics.getTransferType();
		logger.info("ID为"+id+"的服务器传输协议为"+transferType);
		if(TransferType.IP.getTransferType().equals(transferType)) {
			boolean retFlag = IPProbeReqUtil.stopProbeCheck(serverBasics);
			if(!retFlag) {
				logger.info("停止检测探针监测失败");
				return super.returnError("关闭监测探针失败,请检查探针是否在线和网络状态");
			}
			serverBasics.setOpenState(ProbeDisplayUtil.PROBE_STATE_STOP);
			serverBasicsDao.updateServerBasic(serverBasics);
			recoverAlarm(serverBasics);
			return super.returnSuccess("关闭监测探针成功");			
		}
		return super.returnError("关闭监测探针失败，不支持的传输协议");
	}
	

	@Override
	public List<Map<String,Object>> testRemoteLoginBatch(List<Integer> serverIdList) throws Exception{
		List<Map<String,Object>> loginMsgList = new ArrayList<Map<String,Object>>();
		for (Integer serverId : serverIdList) {
			Map<String,Object> loginMsg = new HashMap<String, Object>();
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(serverId);
			String user = serverBasics.getUser();
			String manageIp = serverBasics.getServerManageIp();
			String password = serverBasics.getPassword();

			if(StringUtil.isNull(manageIp) || StringUtil.isNull(user) || StringUtil.isNull(password)) {
				loginMsg.put("ret",false);
				loginMsg.put("loginMsg","缺少用户名，密码或者管理ip");
			} else {
				logger.info(manageIp+"远程登录密码解密前为=="+password);
				password = AesUtils.decrypt(password);
                try {
                	if(ProbeDisplayUtil.testRemoteLogin(manageIp, user, password)) {
						loginMsg.put("ret",true);
						loginMsg.put("loginMsg", "登录成功");
					}else {
						loginMsg.put("ret",false);
						loginMsg.put("loginMsg", "登录失败");
					}
					
				} catch (Exception e) {
					logger.error("登录异常",e);
					loginMsg.put("ret",false);
					loginMsg.put("loginMsg", "登录失败");
				}
			}
			loginMsg.put("id", serverBasics.getId());
			loginMsg.put("serverName", serverBasics.getServerName());
			loginMsg.put("version", serverBasics.getVersion());
			loginMsgList.add(loginMsg);
		}
		return loginMsgList;
	}

	@Override
	public Map<String,Object> removeWatchProbe(Integer id,String userId) throws Exception{
		Map<String,Object> resultMap = new HashMap<>();
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(id);
		
		Integer state = serverBasics.getState();
		if(ProbeDisplayUtil.PROBE_STATE_NOT_DIPLAY.equals(state)) {
			serverBasics.setState(ProbeDisplayUtil.PROBE_STATE_DELETE);
			serverBasicsDao.updateServerBasic(serverBasics);
			resultMap.put("ret",true);
			resultMap.put("msg","移除监测探针成功");
			return resultMap;
			//return super.returnSuccess("服务器删除成功");
		}
		
		serverBasics.setState(ProbeDisplayUtil.PROBE_STATE_DELETE);
		serverBasicsDao.updateServerBasic(serverBasics);
		
		String transferType = serverBasics.getTransferType();
		if(TransferType.IP.getTransferType().equals(transferType)) {
			String serverOs = serverBasics.getServerOs();
			if(OperationSystem.Linux.getDesc().equals(serverOs)) {
				String manageIp = serverBasics.getServerManageIp();
				String user = serverBasics.getUser();
				logger.info(manageIp+"远程登录密码解密前为=="+serverBasics.getPassword());
				String password = AesUtils.decrypt(serverBasics.getPassword());
				boolean retFlag = ProbeDisplayUtil.removeProbe(manageIp, user, password);
				if(!retFlag) {
					recoverAlarm(serverBasics);
					resultMap.put("ret",false);
					resultMap.put("msg","服务器已停止监测，移除监测探针失败，请手动删除");
					return resultMap;
					//return super.returnSuccess("服务器已停止监测，删除探针失败，请手动删除");
				}
			}else if(OperationSystem.Windows.getDesc().equals(serverOs)) {
				boolean retFlag = IPProbeReqUtil.removeProbe(serverBasics);
				if(!retFlag) {
					recoverAlarm(serverBasics);
					resultMap.put("ret",false);
					resultMap.put("msg","服务器已停止监测，移除监测探针失败，请手动删除");
					return resultMap;
					//return super.returnSuccess("服务器已停止监测，删除探针失败，请手动删除");
				}
			}
		}/*else if(TransferType.V2V.getTransferType().equals(transferType)) {
			boolean retFlag = ProbeManagerMsgUtil.deleteV2vProbe(serverBasics,userId);
			if(!retFlag) {
				resultMap.put("ret", false);
				resultMap.put("msg", "服务器已停止监测，移除监测探针失败，请手动删除");
				return resultMap;
			}
		}else {
			resultMap.put("ret", false);
			resultMap.put("msg", "不支持的传输协议");
			return resultMap;
		}*/
		recoverAlarm(serverBasics);
		resultMap.put("ret",true);
		resultMap.put("msg","移除监测探针成功");
		return resultMap;
		//return super.returnSuccess("删除服务器成功");
	}
	

	private void recoverAlarm(ServerBasics serverBasics) {
		List<AlarmDomain> alarmDomains = new ArrayList<>();
		String serverProvince = serverBasics.getServerProvince();
		String serverUnique = serverBasics.getServerUnique();
		String serverName = serverBasics.getServerName();
		String nowTime = DateUtil.date2String("yyyy-MM-dd HH:mm:ss");
		String[] regionIdArr = serverBasics.getServerDistrict().split(",");
		//生成cpu报警
		AlarmDomain cpuAlarm
				= alarmService.generateAlarm(serverUnique, serverName, LocalAlarmTypes.cpu,"clear",nowTime,"cpu告警恢复",
				regionIdArr[regionIdArr.length-1],"OPERATION");
		AlarmDomain ddrAlarm
				= alarmService.generateAlarm(serverUnique, serverName,LocalAlarmTypes.memory,"clear",nowTime,"内存告警恢复",
				regionIdArr[regionIdArr.length-1],"OPERATION");
		AlarmDomain hddAlarm
				= alarmService.generateAlarm(serverUnique, serverName,LocalAlarmTypes.disk,"clear",nowTime,"硬盘告警恢复",
				regionIdArr[regionIdArr.length-1],"OPERATION");
		AlarmDomain networkAlarm
				= alarmService.generateAlarm(serverUnique, serverName,LocalAlarmTypes.network,"clear",nowTime,"流量告警恢复",
				regionIdArr[regionIdArr.length-1],"OPERATION");
		alarmDomains.add(cpuAlarm);
		alarmDomains.add(ddrAlarm);
		alarmDomains.add(hddAlarm);
		alarmDomains.add(networkAlarm);
		//进程报警
		List<PlatformVO> platformList = platformDao.getPlatformListByServerUnique(serverBasics.getServerUnique());
		for (PlatformVO platformVO : platformList) {
			List<PlatformProcess> processList = processDao.getProcessByTposRegisterid(platformVO.getTposRegisterid());
			for (PlatformProcess process : processList) {
				AlarmDomain processAlarm
						= alarmService.generateAlarm(serverUnique+":"+process.getId(),platformVO.getTposName(),LocalAlarmTypes.softabnormal,"clear",
						nowTime,"平台运行异常告警恢复",regionIdArr[regionIdArr.length-1],"OPERATION");
				alarmDomains.add(processAlarm);
			}

			AlarmDomain configAlarm
					= alarmService.generateAlarm(platformVO.getTposRegisterid(),platformVO.getTposName(),LocalAlarmTypes.softconfig,"clear",
					nowTime,"平台配置告警恢复",regionIdArr[regionIdArr.length-1],"OPERATION");
			alarmDomains.add(configAlarm);
		}
		//离线告警
		AlarmDomain offLineAlarm = alarmService.generateAlarm(serverUnique,serverName,LocalAlarmTypes.offline,"clear",nowTime,
				"服务器离线告警恢复",regionIdArr[regionIdArr.length-1],"OPERATION");
		alarmDomains.add(offLineAlarm);
		for (AlarmDomain alarmDomain : alarmDomains) {
			alarmService.addAlarm(alarmDomain);
		}

	}
	
	@Override
	public String getLocalRecentVersion(String transferType) {
		String[] command = new String[]{"sh","-c","ls /home/ftp"};
		String result = ProcessUtil.execute(command);
		logger.info("查询版本结果"+result);
		Pattern pattern;
		Matcher matcher;
		if(TransferType.IP.getTransferType().equals(transferType)) {
			//pattern = Pattern.compile("visionvera-serverwatch-([0-9]+\\.[0-9]+\\.[0-9]+)\\.zip");
			pattern = Pattern.compile("ipprobemonitor_V([0-9]+\\.[0-9]+\\.[0-9]+)\\.zip");
		}else {
			//v2vmonitor_V1.0.0.zip
			//pattern = Pattern.compile("v2vmonitor_V([0-9]{+}\\.[0-9]{+}\\.[0-9]{+})\\.zip");
			pattern = Pattern.compile("v2vprobemonitor_V([0-9]+\\.[0-9]+\\.[0-9]+)\\.zip");
		}
		String recentVersion = "";
		if(StringUtil.isNotNull(result)) {
			matcher = pattern.matcher(result);
			while(matcher.find()) {
				String version = matcher.group(1);
				logger.info("匹配版本号结果"+version);
				if(StringUtil.isNull(recentVersion) || IPProbeReqUtil.compareProbeVersionSupportDifferentLength(version,recentVersion) >= 0) {
					recentVersion = version;
				}
			}
		}
		return recentVersion;
	}
	
	@Override
	public void downLoadInstallPackage(Integer serverId, String version,HttpServletResponse response) 
			throws Exception{
		String tempFolder = "";
		try {
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(serverId);
			String transferType = serverBasics.getTransferType();
			logger.info("ID为"+serverId+"的服务器传输协议为"+transferType);
			if(TransferType.IP.getTransferType().equals(transferType)) {
				//1.封装安装包
				tempFolder = installPackPath + File.separator + System.currentTimeMillis();
				//String installFileName = "visionvera-serverwatch-"+version+".zip";
				String installFileName = "ipprobemonitor_V"+version+".zip";
				buildIpProbeMonitorInstallPackage(serverBasics,tempFolder,installFileName);
				//2.下载安装包
				downloadLocalFile(tempFolder, installFileName, response);
			}else if(TransferType.V2V.getTransferType().equals(transferType)){
				tempFolder = installPackPath + File.separator + System.currentTimeMillis();
				String installFileName = "v2vprobemonitor_V" + version + ".zip";
				buildV2vInstallPackage(serverBasics,tempFolder,installFileName,version);
				downloadLocalFile(tempFolder, installFileName, response);
			}
		}finally {
			//3.删除安装包
			FileUtils.forceDelete(new File(tempFolder));
		}
	}
	
	
	/**
	 * 升级探针
	 */
	@Override
	public Map<String,Object> upGradeWatchProbe(Integer id, String version) throws Exception{
		Map<String,Object> resultMap = new HashMap<>();
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(id);
		if(serverBasics==null){
			resultMap.put("ret",false);
			resultMap.put("msg","服务器探针不存在");
			return resultMap;
		}
		serverBasics.setOperationIp(ProbeManagerMsgUtil.probeManageIp);//升级时使用配置文件配置的探针管理IP
		serverBasics.setPort(ProbeManagerMsgUtil.probeManagePort);
		String oS = serverBasics.getServerOs();
		String transferType = serverBasics.getTransferType();
		String oldVersion = serverBasics.getVersion();
		if(OperationSystem.Windows.getDesc().equals(oS)&&serverBasics.getServerOnLine()==2) {
			resultMap.put("ret",false);
			resultMap.put("msg","Windows服务器探针离线无法升级");
			return resultMap;
		}

		if(version.equals(oldVersion)) {
			resultMap.put("ret",true);
			resultMap.put("msg","服务器探针已是最新版本");
			resultMap.put("version",version);
			return resultMap;
			//return super.returnResult(0, "服务器探针已是最新版本", null, null, version);
		}
		//1.封装安装包
		String installFilePath="";
		String tempFolder = installPackPath + File.separator + System.currentTimeMillis();
		String installFileName = "ipprobemonitor_V"+version+".zip";
		try {
			installFilePath = buildIpProbeMonitorInstallPackage(serverBasics,tempFolder,installFileName);
		} catch (Exception e) {
			logger.error("WatchProbeServiceImpl===upGradeWatchProbe===封装安装包异常",e);
			//删除安装包
			FileUtils.forceDelete(new File(tempFolder));
			resultMap.put("ret",false);
			resultMap.put("msg","探针升级失败：封装安装包失败");
			resultMap.put("version",oldVersion);
			return resultMap;
		}
		//根据操作系统和传输协议的不同进行不同的处理
		if(TransferType.IP.getTransferType().equals(transferType)) {
			try {
				if(OperationSystem.Windows.getDesc().equals(oS)) {

					//1.封装java安装包  将ipprobe放入serverwatch中
					try {
						String  javaUpgradePath = buildJavaUpgradeInstallPackage(tempFolder,installFilePath,installFileName);
						logger.info("JAVA安装包目录："+javaUpgradePath);
						//2.获得安装包字节数组
						byte[] fileBytes = FileUtils.readFileToByteArray(new File(javaUpgradePath));
						//4.传递安装包
						String managerIp = serverBasics.getServerManageIp();
						//Integer port = serverBasics.getPort();
						String url = String.format(GlobalConstants.UPGRADE_PROBE_URL,managerIp,GlobalConstants.JAVA_PROBE_PORT);
						logger.info("发送探针安装包url=="+url);
						Map<String,byte[]> filesMap = new HashMap<String, byte[]>();
						filesMap.put(javaUpgradePath, fileBytes);
						String result = HttpPostFileUtils.postFiles(url, null, filesMap);
						logger.info("===================传递文件流结果" + result);
						//5处理返回结果
						JSONObject resultObj = JSONObject.parseObject(result);
						if(resultObj==null){
							resultMap.put("ret",false);
							resultMap.put("msg","最新版探针安装包传输失败，探针升级失败");
							resultMap.put("version",oldVersion);
							return resultMap;
						}
						if(!resultObj.getBoolean("result")) {
							resultMap.put("ret",false);
							resultMap.put("msg","监测探针升级失败");
							resultMap.put("version",oldVersion);
							return resultMap;
						}
						
					} catch (Exception e) {
						logger.error("WatchProbeServiceImpl===upGradeWatchProbe===升级JAVA探针异常：",e);
						resultMap.put("ret",false);
						resultMap.put("msg","监测探针升级失败");
						resultMap.put("version",oldVersion);
						return resultMap;
					}finally{
						//删除安装包
						FileUtils.forceDelete(new File(tempFolder));
						logger.info("删除临时安装目录=="+tempFolder);
					}
			
					
			}else if(OperationSystem.Linux.getDesc().equals(oS)){
				String serverManageIp = serverBasics.getServerManageIp();
				String user = serverBasics.getUser();
				String password = serverBasics.getPassword();
				logger.info(serverManageIp+"远程登录密码解密前为=="+password);
				password = AesUtils.decrypt(password);
				boolean testLoginRet = ProbeDisplayUtil.testRemoteLogin(serverManageIp, user, password);
				if(!testLoginRet) {
					FileUtils.forceDelete(new File(tempFolder));
					logger.info("删除临时目录："+tempFolder);
					resultMap.put("ret",false);
					resultMap.put("msg","探针升级失败：远程登录服务器失败");
					resultMap.put("version",oldVersion);
					return resultMap;
				}
				//停止java旧版探针
				if("3.1.11".equals(oldVersion)||"1.4.4".equals(oldVersion)/*IPProbeReqUtil.compareProbeVersionSupportDifferentLength(oldVersion,"2.1.13") < 0*/){
					boolean stopRet = ProbeDisplayUtil.stopJavaProbe(serverManageIp, user, password);
					if(!stopRet){
						FileUtils.forceDelete(new File(tempFolder));
						logger.info("删除临时目录："+tempFolder);
						resultMap.put("ret",false);
						resultMap.put("msg","探针升级失败：停止JAVA监测探针失败");
						resultMap.put("version",oldVersion);
						return resultMap;
					}
					
				}
				//停止探针
			/*	if(!"3.1.11".equals(oldVersion)&&IPProbeReqUtil.compareProbeVersionSupportDifferentLength(oldVersion,"2.1.3") >= 0){
				try {	
				boolean stopRet = ProbeDisplayUtil.stopProbe(serverManageIp, user, password);
				if(!stopRet){
					//删除安装包
					FileUtils.forceDelete(new File(tempFolder));
					logger.info("删除临时目录："+tempFolder);
					resultMap.put("ret",false);
					resultMap.put("msg","探针升级失败：停止监测探针失败");
					resultMap.put("version",oldVersion);
					return resultMap;
				}
				}catch(Exception e){
					logger.error("WatchProbeServiceImpl===upGradeWatchProbe===监测探针停止异常",e);
				}
				
				}*/
				//部署探针
				Map<String, Object> displayProbe=null;
				try {
					 displayProbe = ProbeDisplayUtil.displayProbe(serverManageIp, user, password, installFilePath);
				}catch(Exception e){
					logger.error("WatchProbeServiceImpl===upGradeWatchProbe===监测探针部署异常",e);
				}finally {
					//删除安装包
					FileUtils.forceDelete(new File(tempFolder));			
				}
				if(displayProbe==null){
					resultMap.put("ret",false);
					resultMap.put("msg","探针升级失败：监测探针部署失败");
					resultMap.put("version",oldVersion);
					return resultMap;
				}
				boolean retFlag = (Boolean)displayProbe.get("ret");
				if(retFlag) {
					boolean startRet = ProbeDisplayUtil.startProbe(serverManageIp, user, password);
					if(startRet) {
						serverBasics.setVersion(version);
						serverBasicsDao.updateServerBasic(serverBasics);
							resultMap.put("ret",true);
							resultMap.put("msg","监测探针升级成功");
							resultMap.put("version",version);
							return resultMap;
						}else {
							resultMap.put("ret",false);
							resultMap.put("msg","探针升级失败：监测探针启动失败");
							resultMap.put("version",oldVersion);
							return resultMap;
						}
					}else {
						resultMap.put("ret",false);
						resultMap.put("msg",(String)displayProbe.get("msg"));
						resultMap.put("version",oldVersion);
						return resultMap;
					}
				}else {
					resultMap.put("ret",false);
					resultMap.put("msg","监测探针升级失败：请补充服务器操作系统信息");
					resultMap.put("version",oldVersion);
					return resultMap;
				}
			}catch(Exception e) {
				logger.error("WatchProbeServiceImpl===upGradeWatchProbe===监测探针升级异常",e);
				resultMap.put("ret",false);
				resultMap.put("msg","监测探针升级异常");
				resultMap.put("version",oldVersion);
				return resultMap;
			}
		}else {
			resultMap.put("ret",false);
			resultMap.put("msg","监测探针升级失败：请补充服务器传输协议信息");
			resultMap.put("version",oldVersion);
			return resultMap;
		}
		serverBasics.setVersion(version);
		serverBasicsDao.updateServerBasic(serverBasics);
		resultMap.put("ret",true);
		resultMap.put("msg","监测探针升级成功");
		resultMap.put("version",version);
		return resultMap;
	}
	
	/**
	 * 生成监测探针安装包，并返回文件完全路径
	 * @return
	 * @throws Exception
	 */
	/*private String buildIpInstallPackage(ServerBasics serverBasics,String tempFolder,String installFileName) throws Exception {
		//生成临时目录，经安装包复制到临时目录并解压
		String srcInstallFile = installPackPath + File.separator + installFileName;
		FileUtils.copyFile(new File(srcInstallFile), new File(tempFolder,installFileName));
		ZipUtil.unZip(new File(tempFolder + File.separator + installFileName),tempFolder);
		//获取服务器信息，修改安装包配置文件
		String configFile = tempFolder + File.separator + installFileName.replace(".zip", "")+File.separator+"sw_config"+File.separator+"comconfig.xml";
		Document doc = Dom4jHelper.parse(configFile, "utf-8");
		doc.selectSingleNode("//serverUnique").setText(serverBasics.getServerUnique());
		doc.selectSingleNode("//operationIp").setText(serverBasics.getOperationIp()==null? "":serverBasics.getOperationIp());
		doc.selectSingleNode("//openstate").setText(serverBasics.getOpenState()==null? "":serverBasics.getOpenState()+"");
		
		List<PlatformProcess> allProcesses = processDao.getAllProcesses(serverBasics.getServerUnique());
		Node processesNode = doc.selectSingleNode("//processes");
		Element processesElement = (Element)processesNode;
		for (PlatformProcess process : allProcesses) {
			Element processElement = processesElement.addElement("process");
			processElement.addElement("id").setText(process.getId()+"");
			processElement.addElement("name").setText(process.getProcessName());
			processElement.addElement("port").setText(process.getProcessPort() == null? "" : process.getProcessPort()+"");
		}
		List<PlatformVO> platformVOList = platformDao.getPlatformListByServerUnique(serverBasics.getServerUnique());
		Node configCheckNode = doc.selectSingleNode("//config-check");
		Element configCheckElement = (Element)configCheckNode;
		for (PlatformVO platformVO : platformVOList) {
			Element serviceElement = configCheckElement.addElement("service");
			serviceElement.addAttribute("tposRegisterid",platformVO.getTposRegisterid());
			serviceElement.addAttribute("type",platformVO.getAbbreviation());
			serviceElement.addAttribute("addr",platformVO.getTposIp());
			serviceElement.addAttribute("check",platformVO.getConfCheck()+"");
			serviceElement.addAttribute("checkTime",StringUtil.isNull(platformVO.getConfCheckTime())? "" : platformVO.getConfCheckTime());
		}
		Dom4jHelper.save(doc, configFile, "utf-8");
		//删除安装包并重新打包
		//FileUtils.forceDelete(new File(tempFolder + File.separator + installFileName));
		ZipUtil.toZip(tempFolder +File.separator+ installFileName.replace(".zip", ""), new FileOutputStream(new File(tempFolder + File.separator + installFileName)),true);
		return tempFolder + File.separator + installFileName;
	}*/
	
	/**
	 * 生成V2v版本监测探针安装包
	 * @param serverBasics 服务器信息
	 * @param tempFolder   生成安装包目录
	 * @param installFileName 安装包名称
	 * @param version 版本号
	 * @return
	 */
	private String buildV2vInstallPackage(ServerBasics serverBasics, String tempFolder, String installFileName, String version) throws Exception{
		String srcInstallFile = installPackPath + File.separator + installFileName;
		FileUtils.copyFile(new File(srcInstallFile), new File(tempFolder,installFileName));
		ZipUtil.unZip(new File(tempFolder + File.separator + installFileName), tempFolder);

		String cfgFile = tempFolder + File.separator + installFileName.replace(".zip", "") + File.separator + "probe.xml";
		Document doc = Dom4jHelper.parse(cfgFile, "utf-8");
		if(GlobalConstants.REUSE_XZ_TERNO_YES ==serverBasics.getReuseXzNo()) {
			doc.selectSingleNode("//configure/protocol").setText("tcp");
		} else {
			doc.selectSingleNode("//configure/protocol").setText("v2v");
			doc.selectSingleNode("//configure/monitor/v2v/servermac").setText(serverBasics.getNetMac());
			doc.selectSingleNode("//configure/monitor/v2v/mac").setText(serverBasics.getV2vNetMac());
		}

		Element mapElemenet = (Element)doc.selectSingleNode("//configure/map");
		List<PlatformProcess> allProcesses = processDao.getAllProcesses(serverBasics.getServerUnique());
		for (PlatformProcess process : allProcesses) {
			Element procElement = mapElemenet.addElement("id");
			procElement.addAttribute("uuid", process.getId()+"");
			procElement.addAttribute("name", process.getProcessName());
			procElement.addAttribute("port", process.getProcessPort()==null? "" :process.getProcessPort()+"");
			procElement.setText("v2v");
		}
		Element platformElement = (Element)doc.selectSingleNode("//configure/platform");
		List<PlatformVO> platformVOList = platformDao.getPlatformListByServerUnique(serverBasics.getServerUnique());
		for (PlatformVO platformVO : platformVOList) {
			Element idElement = platformElement.addElement("id");
			idElement.addAttribute("platid",platformVO.getTposRegisterid());
			idElement.addAttribute("type",platformVO.getAbbreviation());
			idElement.addAttribute("check",GlobalConstants.REUSE_XZ_TERNO_YES == platformVO.getConfCheck()? "start":"stop");
			idElement.addAttribute("checkTime",StringUtil.isNull(platformVO.getConfCheckTime())? "" : platformVO.getConfCheckTime());
			idElement.addAttribute("version",platformVO.getTposPlatformVersion());
			idElement.addAttribute("versionSwitch",GlobalConstants.REUSE_XZ_TERNO_YES == platformVO.getVersionCheck()? "start":"stop");
		}
		doc.selectSingleNode("//configure/version").setText(version);


		Dom4jHelper.save(doc, cfgFile, "utf-8");
		//FileUtils.forceDelete(new File(tempFolder + File.separator + installFileName));
		ZipUtil.toZip(tempFolder +File.separator+ installFileName.replace(".zip", ""), new FileOutputStream(new File(tempFolder + File.separator + installFileName)),true);
		return tempFolder + File.separator + installFileName;
	}
	/**
	 * 生成IP版本监测探针安装包
	 * @param serverBasics 服务器信息
	 * @param tempFolder   生成安装包目录
	 * @param installFileName 安装包名称
	 * @param version 版本号
	 * @return
	 */
	private String buildIpProbeMonitorInstallPackage(ServerBasics serverBasics, String tempFolder, String installFileName) throws Exception{
		String srcInstallFile = installPackPath + File.separator + installFileName;
		FileUtils.copyFile(new File(srcInstallFile), new File(tempFolder,installFileName));
		ZipUtil.unZip(new File(tempFolder + File.separator + installFileName), tempFolder);
		
		String cfgFile = tempFolder + File.separator + installFileName.replace(".zip", "") + File.separator + "probe.xml";
		Document doc = Dom4jHelper.parse(cfgFile, "utf-8");
		doc.selectSingleNode("//configure/protocol").setText("IP");
		doc.selectSingleNode("//configure/probeIP").setText(serverBasics.getServerManageIp());//探针IP
		doc.selectSingleNode("//configure/monitor/eth/ip").setText(ProbeManagerMsgUtil.probeManageIp);//探针管理IP
		doc.selectSingleNode("//configure/monitor/eth/port").setText(String.valueOf(ProbeManagerMsgUtil.probeManagePort));
		doc.selectSingleNode("//configure/acquisition").setText("start");
		Dom4jHelper.save(doc, cfgFile, "utf-8");
		String winmonitorFolder =tempFolder + File.separator + installFileName.replace(".zip", "") + File.separator+"winmonitor";
		String linuxmonitorFolder =tempFolder + File.separator + installFileName.replace(".zip", "") + File.separator+"linuxmonitor";
		FileUtils.copyFile(new File(cfgFile), new File(winmonitorFolder+File.separator+"probe.xml"));
		FileUtils.copyFile(new File(cfgFile), new File(linuxmonitorFolder+File.separator+"probe.xml"));
		//整个包压缩
		ZipUtil.toZip(tempFolder +File.separator+ installFileName.replace(".zip", ""), new FileOutputStream(new File(tempFolder + File.separator + installFileName)),true);
		return tempFolder + File.separator + installFileName;
	}
	/**
	 * 生成JAVA-IP版本升级过渡安装包
	 * @param tempFolder   生成安装包目录
	 * @param ipprobemonitorinstallPackPath ipprobe安装包路径+名称
	 * @return
	 */
	@SuppressWarnings("unused")
	private String buildJavaUpgradeInstallPackage( String tempFolder,String ipprobemonitorinstallPackPath,String ipprobemonitorName) throws Exception{
		String installFileName ="visionvera-serverwatch-2.1.0.0.zip";
		
		String srcInstallFile = installPackPath + File.separator + installFileName;
		FileUtils.copyFile(new File(srcInstallFile), new File(tempFolder,installFileName));
		ZipUtil.unZip(new File(tempFolder + File.separator + installFileName), tempFolder);
		FileUtils.copyFile(new File(ipprobemonitorinstallPackPath), new File(tempFolder+ File.separator + installFileName.replace(".zip", ""),ipprobemonitorName));
		//整个包压缩
		ZipUtil.toZip(tempFolder +File.separator+ installFileName.replace(".zip", ""), new FileOutputStream(new File(tempFolder + File.separator + installFileName)),true);
		return tempFolder + File.separator + installFileName;
	}
	
	private void downloadLocalFile(String folderName,String fileName, HttpServletResponse response) throws Exception {
		 
        // 读到流中
        InputStream inStream = new FileInputStream(folderName + "/" + fileName);// 文件的存放路径
        // 设置输出的格式
        response.reset();
        response.setHeader("content-Type", "application/x-zip-compressed;charset=UTF-8");
        // 下载文件的默认名称
        response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(fileName, "utf-8"));
        // 循环取出流中的数据
        byte[] b = new byte[4096];
        int len;
        try {
            while ((len = inStream.read(b)) > 0)
                response.getOutputStream().write(b, 0, len);
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	
	@Override
	public ReturnData checkProbeStatus(Integer serverId) {
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(serverId);
		String transferType = serverBasics.getTransferType();
		if(TransferType.IP.getTransferType().equals(transferType)) {
			String ip = serverBasics.getServerManageIp();
			Integer port = serverBasics.getPort();
			String url = String.format(GlobalConstants.PROBE_CURRENT_VERSION,ip,port);
			Map<String,Object> retMap = RestTemplateUtil.getForObject(url,Map.class);
			String msg = (String)retMap.get("msg");
			logger.info("获取监测探针版本信息：" + msg);
			if(retMap.get("result") != null && (Boolean)retMap.get("result")==true) {
				String version = (String)retMap.get("data");
				serverBasics.setVersion(version);
				if(ProbeDisplayUtil.PROBE_STATE_NOT_DIPLAY.equals(serverBasics.getState())){
					serverBasics.setState(ProbeDisplayUtil.PROBE_STATE_DIPLAYED);
				}
				serverBasicsDao.updateServerBasic(serverBasics);
				return super.returnSuccess("监测探针状态正常");
			}
		}
		return super.returnError("监测探针状态异常");
	}

	@Override
	public void upV2VGradeProbe(List<Integer> idList, String version,Session session) {
		List<ServerBasics> serverBasicsList = serverBasicsDao.getServerBasicsByIds(idList);
		List<String> uuidList = new ArrayList<String>();
		for (ServerBasics serverBasics : serverBasicsList) {
			String serverUnique = serverBasics.getServerUnique();
			uuidList.add(serverUnique);
			//WebSocket.uuidAndSessionMap.put(serverUnique, session);
		}
		
		//发送下载信息
		boolean retFlag = ProbeManagerMsgUtil.upGradeProbe(uuidList, version, null);
		if(retFlag) {
			//WebSocket.sendMessage("发送探针升级信息成功", session);
		}else {
			//WebSocket.sendMessage("发送探针升级信息失败", session);
		}
	}

	@Override
	public void handleUpGradeMsg(String message) {
		JSONObject msgObj = JSONObject.parseObject(message);
		String serverUnique = msgObj.getString("uuid");
		String ret = msgObj.getString("ret");
		
		JSONObject respObj = new JSONObject();
		if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
			String version = msgObj.getString("version");
			ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
			serverBasics.setVersion(version);
			serverBasicsDao.updateServerBasic(serverBasics);
			
			//WebSocket.upGradeUuidList.remove(serverUnique);
			
			respObj.put("state", 0);
			respObj.put("serverUnique", serverUnique);
			respObj.put("msg", "升级成功");
		}else {
			respObj.put("state", 1);
			respObj.put("serverUnique", serverUnique);
			respObj.put("msg", "升级失败");
		}
		//Session session = WebSocket.uuidAndSessionMap.get(serverUnique);
		//WebSocket.uuidAndSessionMap.remove(serverUnique);
		//WebSocket.sendMessage(respObj.toJSONString(), session);
	}

	@Override
	public void saveV2vProbeInstallPack(MultipartFile file,String filename,String type) throws Exception{
		File destFile = new File(installPackPath+File.separator+filename);
		FileUtils.copyInputStreamToFile(file.getInputStream(), destFile);
	}

}
