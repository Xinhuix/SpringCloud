package com.visionvera.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.netty.NettyTcpClient;

/**
 * 探针管理服务TCP交互信息
 * @author dql
 *
 */
@Component
public class ProbeManagerMsgUtil {

	private static Logger logger = LoggerFactory.getLogger(ProbeManagerMsgUtil.class);
	
	public static final String TCP_SUCESS_RET = "0";    //监测探针返回成功信息标志
	
	public static final String TCP_RECENT_VERSION_FUNC = "get_version";  //获取监测探针最新版本标志funcName
	public static final String TCP_DOWNLOAD_PROBE = "download_probe";	//同步v2v探针安装包
	public static final String ADD_PROC = "add_proc";			//添加进程
	public static final String MODIFY_PROC = "modify_proc";		//修改进程
	public static final String DEL_PROC = "del_proc";	//删除进程
	public static final String CMD_FUNC = "cmd";   //操作进程
	public static final String PROBE_OPEN_STATE = "acqState";		//监测探针关闭/开启
	
	public static final String TCP_GET_PROBE_VERSION = "get_probe_version";		//获取单个探针版本号
	public static final String PROBE_UPGRADE = "update_monitors";		//探针升级
	public static final String TCP_LAUNCH_WEB = "web";		//web页面发起
	public static final String TCP_LAUNCH_BACK = "back";	//后台发起

	public static final String ADD_PLATFORM = "add_platform"; //添加平台
	public static final String MODIFY_PLATFORM = "modify_platform";		//修改平台
	//public static final String MODIFY_SERVER = "modify_server";		//修改探针（服务器）
	public static final String MODIFY_PROBE = "modify_probe";		//修改探针
	public static final String DEL_PLATFORM = "del_platform";		//删除平台

	public static final String PLATFORM_CONF_CHECK = "platformConfCheck";//平台配置检测
	
	public static final String PLATFORM_VERSION_CHECK = "platformVersionCheck";//版本不一致检测

	public static final String DEL_PROBE = "del_probe";		//删除探针（服务器）
	
	public static final String PROCESS_STATUS_CHECKINFO = "ProcessStatusCheckInfo";//进程状态监测开关
	
	public static final String PROCESS_RESTART = "ProcessRestart";//进程重启
	
	public static final String YUZHI_SETVALUE = "YuzhiSetValue";//阈值设置接口
	
	public static final String PLATFORM_CHECKINFO = "PlatformCheckInfo";//GIS、唐古拉平台配置检测
	
	public static final String PLATFORM_CHECKALARM = "PlatformCheckAlarm";//GIS、唐古拉平台配置检测上报告警
	
	public static final String PROCESS_INFO = "ProcessInfo";//异常进程上报告警
	
	public static final String PROCESS_INFO_ALARM = "ProcessInfoAlarm";//离线进程上报告警
	
	public static final String PORT_INFO = "PortInfo";//异常端口上报告警
	
	public static final String INFO_CHANGE = "InfoChange";//进程状态刷新
	
	public static final String PLAT_RESTART = "PlatRestart";//平台重启
	
	public static final String ADD_PROBE_BATCH = "add_probe_batch";//探针批量导入通知探针管理
	
	public static final String PORT_INFO_CHANGE = "port_info_change";//端口变更通知探针管理
	
	public static String probeManageIp;
	
	public static Integer probeManagePort =60003;//探针管理端口

	@Value("${probe_manage.ip}")
	public void setProbeManageIp(String probeManageIp) {
		ProbeManagerMsgUtil.probeManageIp = probeManageIp;
	}

	public static boolean modifyConfig(ServerBasics serverBasics,String userId) {
		JSONObject sendData = new JSONObject();
		sendData.put("funcName", MODIFY_PROBE);

		JSONObject eachData = new JSONObject();
		eachData.put("uuid", serverBasics.getServerUnique());
		eachData.put("reuseXzNo",serverBasics.getReuseXzNo() + "");
		eachData.put("terminalCode",serverBasics.getTerminalCode());
		eachData.put("ip", StringUtil.isNull(serverBasics.getServerManageIp()) ? "" : serverBasics.getServerManageIp());
		eachData.put("port", serverBasics.getPort() == null ? "" : serverBasics.getPort() + "");
		eachData.put("serMac", serverBasics.getNetMac() == null ? "" : serverBasics.getNetMac());
		eachData.put("virMac", serverBasics.getV2vNetMac() == null ? "" : serverBasics.getV2vNetMac());
		eachData.put("state",serverBasics.getState()+"");
		eachData.put("userId", userId);
		sendData.put("param", eachData);
		String resultStr = NettyTcpClient.sendMsg(probeManageIp, sendData.toJSONString());
		return getTcpResult(resultStr);

	}

	/**
	 * 获取平台推荐版本
	 * @param funcName
	 * @return
	 */
    public static boolean getPlatformSuggestedVersion(String funcName) {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",funcName);
    	String resultStr = NettyTcpClient.sendMsg(probeManageIp,dataJson.toJSONString());
		return getTcpResult(resultStr);
    }



	/**
	 * 更改服务器探针的开启状态
	 * @param uuidList
	 * @param openState
	 * @param userId
	 * @return
	 */
	public static boolean updateProbeOpenState(List<String> uuidList, String openState, String userId) {
		JSONObject dataObj = new JSONObject();
		JSONObject paramObj = new JSONObject();
		paramObj.put("uuids", uuidList);
		paramObj.put("state", openState);
		paramObj.put("userId",userId);
		dataObj.put("funcName", "acqState");
		dataObj.put("param", paramObj);

		String retMsg = NettyTcpClient.sendMsg(probeManageIp, dataObj.toJSONString());
		return getTcpResult(retMsg);
	}

	/**
	 * 通知探针管理升级探针
	 * @param uuidList  服务器唯一标识集合
	 * @param userId
	 * @return true:探针管理服务接收信息成功    false:探针管理服务接收信息失败
	 */
	public static boolean upGradeProbe(List<String> uuidList, String version, String userId) {
		JSONObject dataObj = new JSONObject();
		dataObj.put("funcName", PROBE_UPGRADE);
		JSONObject paramObj = new JSONObject();
		paramObj.put("uuids", uuidList);
		paramObj.put("version", version);
		paramObj.put("userId",userId);
		dataObj.put("param", paramObj);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, dataObj.toJSONString());
		return getTcpResult(retMsg);
	}
	
	/**
	 * 获取最新探针版本号
	 * @param type web：前端发起   back:后台定时任务发起
	 * @param userId
	 * @return
	 */
	public static boolean getProbeRecentVersion(String type, String userId) {
		JSONObject dataObj = new JSONObject();
		dataObj.put("funcName", TCP_RECENT_VERSION_FUNC);
		JSONObject paramObj = new JSONObject();
		paramObj.put("type", type);
		paramObj.put("userId",userId);
		dataObj.put("param", paramObj);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, dataObj.toJSONString());
		return getTcpResult(retMsg);
	}
	
	/**
	 * 同步探针管理安装包
	 * @param type  web:前端发起  back:后台定时任务发起
	 * @param userId
	 */
	public static boolean syncV2vProbeInstallPac(String type, String userId) {
		logger.info("type =====" + type);
		JSONObject dataObj = new JSONObject();
		dataObj.put("funcName", "download_probe");
		JSONObject paramObj = new JSONObject();
		paramObj.put("type", type);
		paramObj.put("userId",userId);
		dataObj.put("param", paramObj);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, dataObj.toJSONString());
		logger.info("同步探针最新版本返回========" + retMsg);
		return getTcpResult(retMsg);
	}
	
	/**
	 * 移除v2v监测探针
	 * @param serverBasics 服务器信息
	 * @return
	 */
	public static boolean deleteV2vProbe(ServerBasics serverBasics,String userId) {
		JSONObject dataObj = new JSONObject();
		dataObj.put("funcName", DEL_PROBE);

		JSONObject paramJson = new JSONObject();
		JSONArray uuidArr = new JSONArray();
		JSONObject serverObj = new JSONObject();
		serverObj.put("uuid",serverBasics.getServerUnique());
		serverObj.put("state",serverBasics.getState()+"");
		uuidArr.add(serverObj);
		paramJson.put("uuids",uuidArr);
		paramJson.put("userId",userId);
		dataObj.put("param", paramJson);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, dataObj.toJSONString());
		return getTcpResult(retMsg);
	}
	
	/**
	 * 向探针管理服务同步服务器配置信息
	 * @param serverList
	 * @return
	 */
	public static boolean sendConfig(List<ServerBasics> serverList) {
		JSONObject sendData = new JSONObject();
		sendData.put("funcName", "add_probe");
		
		JSONArray dataArray = new JSONArray();
		for (ServerBasics serverBasics : serverList) {
			JSONObject eachData = new JSONObject();
			eachData.put("uuid", serverBasics.getServerUnique());
			eachData.put("terminalCode",serverBasics.getTerminalCode());
			eachData.put("ip", StringUtil.isNull(serverBasics.getServerManageIp()) ? "" : serverBasics.getServerManageIp());
			eachData.put("port", serverBasics.getPort() == null ? "" : serverBasics.getPort() + "");
			eachData.put("serverOs", serverBasics.getServerOs());
			dataArray.add(eachData);
		}
		sendData.put("param", dataArray);
		
		String resultStr = NettyTcpClient.sendMsg(probeManageIp, sendData.toJSONString());

		return getTcpResult(resultStr);
	}
	
	/**
	 * 向探针管理服务发送进程信息
	 * @param serverBasics
	 * @param process
	 * @param funcName
	 * @param userId
	 * @return
	 */
	/*public static boolean sendProcMsg(ServerBasics serverBasics, PlatformProcess process, String funcName, String userId) {
		JSONObject transferData = new JSONObject();
		transferData.put("funcName", funcName);
		transferData.put("uuid", serverBasics.getServerUnique());
		
		JSONObject paramData = new JSONObject();
		
		JSONArray procArray = new JSONArray();
		JSONObject procObj = new JSONObject();
		procObj.put("processId", process.getId()+"");
		procObj.put("name", process.getProcessName());
		procObj.put("port", process.getProcessPort()==null?"":process.getProcessPort()+"");
		procArray.add(procObj);
		
		paramData.put("proc", procArray);
		paramData.put("userId",userId);
		
		transferData.put("param", paramData);
		
		String resultStr = NettyTcpClient.sendMsg(probeManageIp, transferData.toJSONString());
		return getTcpResult(resultStr);
	}*/
	
	/**
	 * 获取探针版本号
	 * @param serverUnique 服务器唯一标识
	 * @param userId
	 * @return
	 */
	public static boolean getProbeVersion(String serverUnique, String userId) {
		JSONObject dataJson = new JSONObject();
		dataJson.put("funcName", "get_probe_version");
		JSONObject paramObj = new JSONObject();
		paramObj.put("uuid", serverUnique);
		paramObj.put("userId",userId);
		dataJson.put("param", paramObj);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, dataJson.toJSONString());
		return getTcpResult(retMsg);
	}

	public static boolean addAndModifyPlatform(String funcName, PlatformVO platformVO, String userId) {
		JSONObject dataJson = new JSONObject();
		dataJson.put("funcName",funcName);
		dataJson.put("uuid",platformVO.getServerUnique());

		JSONObject paramJson = new JSONObject();
		paramJson.put("platid",platformVO.getTposRegisterid());
		paramJson.put("type",platformVO.getAbbreviation());
		paramJson.put("addr",platformVO.getTposIp());
		paramJson.put("version",platformVO.getTposPlatformVersion());
		Integer confCheck = platformVO.getConfCheck();
		if(confCheck == 0) {
			paramJson.put("switch","stop");
			paramJson.put("checkTime","");
		}else {
			paramJson.put("switch","start");
			paramJson.put("checkTime",platformVO.getConfCheckTime());
		}
		paramJson.put("versionCheck",platformVO.getVersionCheck() == 0? "stop":"start");
		paramJson.put("userId",userId);
		paramJson.put("tposName",platformVO.getTposName());//平台名称
		paramJson.put("tposPhone",platformVO.getTposPhone());//平台联系人电话
		paramJson.put("tposLinkman",platformVO.getTposLinkman());//平台联系人
		paramJson.put("tposEmail",platformVO.getTposEmail());//平台联系人邮箱
		paramJson.put("tposPlatformVersion",platformVO.getTposPlatformVersion());//平台版本
		paramJson.put("serverUnique",platformVO.getServerUnique());//服务器唯一标识
		paramJson.put("tposState",String.valueOf(platformVO.getTposState()));//平台状态 0正常，-1删除
		paramJson.put("transferType",platformVO.getTransferType());//传输协议 ip v2v
		dataJson.put("param",paramJson);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
		logger.info("addAndModifyPlatform--探针管理返回结果："+retMsg);
		return getTcpResult(retMsg);
	}

	public static boolean removePlatform(PlatformVO platformVO,String userId) {
		JSONObject dataJson = new JSONObject();
		dataJson.put("funcName",DEL_PLATFORM);
		dataJson.put("uuid",platformVO.getServerUnique());

		JSONObject paramJson = new JSONObject();
		paramJson.put("platid",platformVO.getTposRegisterid());
		paramJson.put("type",platformVO.getAbbreviation());
		paramJson.put("userId",userId);
		dataJson.put("param",paramJson);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
		return getTcpResult(retMsg);
	}

	/**
	 * 通知探针进行平台配置检测
	 * @param platformVO
	 */
    public static boolean checkPlatformCof(PlatformVO platformVO) {
		JSONObject dataJson = new JSONObject();
		dataJson.put("funcName",PLATFORM_CONF_CHECK);
		dataJson.put("uuid",platformVO.getServerUnique());

		JSONObject paramJson = new JSONObject();
		paramJson.put("platid",platformVO.getTposRegisterid());
		paramJson.put("type",platformVO.getAbbreviation());
		dataJson.put("param",paramJson);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
		return getTcpResult(retMsg);
    }
   
    /**
     *通知探针管理平台版本是否一致检测开关
     * @param platformVO
     */
    public static boolean checkPlatformVersionCheck(PlatformVO platformVO) {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",PLATFORM_VERSION_CHECK);
    	dataJson.put("uuid",platformVO.getServerUnique());
    	
    	JSONObject paramJson = new JSONObject();
    	paramJson.put("platid",platformVO.getTposRegisterid());
    	paramJson.put("type",platformVO.getAbbreviation());
    	paramJson.put("switch",platformVO.getVersionCheck()==1?"start":"stop");
    	dataJson.put("param",paramJson);
    	String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
    	return getTcpResult(retMsg);
    }
   
    /**
     * 进程配置检测开关
     * @param JSONObject
     */
    public static boolean processStatusCheckInfo(JSONObject json) {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",PROCESS_STATUS_CHECKINFO);
    	dataJson.put("uuid",json.get("serverUnique"));
    	
    	JSONObject paramJson = new JSONObject();
		paramJson.put("platid",json.get("platid"));
		paramJson.put("state",json.get("state"));
		paramJson.put("userId",json.get("userId"));
		dataJson.put("param",paramJson);
    	String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
    	return getTcpResult(retMsg);
    }
    /**
     * 进程重启
     * @param JSONObject
     */
    public static boolean processRestart(JSONObject json) {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",PROCESS_RESTART);
    	dataJson.put("uuid",json.get("serverUnique"));
    	
    	JSONObject paramJson = new JSONObject();
    	paramJson.put("type",json.get("abbreviation"));
    	paramJson.put("processName",json.get("processName"));
    	paramJson.put("userId",json.get("userId"));
    	dataJson.put("param",paramJson);
    	String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
    	return getTcpResult(retMsg);
    }
   
    /**
     * 删除进程
     * @param JSONObject
     */
    public static boolean delProc(JSONObject json) {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",DEL_PROC);
    	dataJson.put("uuid",json.get("serverUnique"));
    	
    	JSONObject paramJson = new JSONObject();
    	paramJson.put("proc",json.get("procList"));
    	paramJson.put("userId",json.get("userId"));
    	dataJson.put("param",paramJson);
    	String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
    	return getTcpResult(retMsg);
    }
    /**
     * GIS、唐古拉平台配置检测
     * @param JSONObject
     */
    public static boolean platformCheckInfo(JSONObject json) {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",PLATFORM_CHECKINFO);
    	dataJson.put("uuid",json.get("serverUnique"));
    	
    	JSONObject paramJson = new JSONObject();
    	paramJson.put("platid",json.get("tposRegisterid"));
    	paramJson.put("plataddr",json.get("tposIp"));
    	paramJson.put("type",json.get("abbreviation"));
    	dataJson.put("param",paramJson);
    	String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
    	return getTcpResult(retMsg);
    }
    /**
     * 平台重启(感知中心调用)
     * @param JSONObject
     */
    public static boolean platRestart(JSONObject json) {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",PLAT_RESTART);
    	JSONObject paramJson = new JSONObject();
    	paramJson.put("platid",json.get("tposRegisterid"));
    	paramJson.put("userId",json.get("userId"));
    	dataJson.put("param",paramJson);
    	String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
    	return getTcpResult(retMsg);
    }
    /**
     * 探针批量导入通知探针管理
     * @param JSONObject
     */
    public static boolean addProbeBatch() {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",ADD_PROBE_BATCH);
    	JSONObject paramJson = new JSONObject();
    	paramJson.put("userId","");
    	dataJson.put("param",paramJson);
    	String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
    	return getTcpResult(retMsg);
    }
    /**
     * 端口变更通知探针管理
     * @param JSONObject
     */
    public static boolean portInfoChange() {
    	JSONObject dataJson = new JSONObject();
    	dataJson.put("funcName",PORT_INFO_CHANGE);
    	JSONObject paramJson = new JSONObject();
    	paramJson.put("userId","");
    	dataJson.put("param",paramJson);
    	String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
    	return getTcpResult(retMsg);
    }

	/**
	 * 平台进程处理
	 * @return
	 */
	/*public static boolean handlePlatform(PlatformVO platformVO, List<PlatformProcess> processList, String method, String userId) {
		JSONObject dataJson = new JSONObject();
		dataJson.put("funcName",CMD_FUNC);
		dataJson.put("uuid",platformVO.getServerUnique());

		JSONArray cmdArray = new JSONArray();
		String processIds = "";
		for (PlatformProcess platformProcess : processList) {
			if(GlobalConstants.PROCESS_STATUS_OK.equals(platformProcess.getProcessStatus())) {
				continue;
			}
			String startScript = platformProcess.getStartScript();
			String startScriptPath = platformProcess.getStartScriptPath();
			String shutdownScript = platformProcess.getShutdownScript();
			String shutdownScriptPath = platformProcess.getShutdownScriptPath();
			if("start".equals(method)) {
				cmdArray.add(StringUtil.isNotNull(startScript) ? startScript : startScriptPath);
			}
			if("stop".equals(method)) {
				cmdArray.add(StringUtil.isNotNull(shutdownScript) ? shutdownScript : shutdownScriptPath);
			}
			if("restart".equals(method)) {
				cmdArray.add(StringUtil.isNotNull(shutdownScript) ? shutdownScript : shutdownScriptPath);
				cmdArray.add(StringUtil.isNotNull(startScript) ? startScript : startScriptPath);
			}
			processIds = platformProcess.getId() +",";
		}

		if(processIds.length() > 0) {
			processIds = processIds.substring(0,processIds.length() -1);
		}

		JSONObject paramJson = new JSONObject();
		paramJson.put("cmd",cmdArray);
		paramJson.put("registerid",platformVO.getTposRegisterid());
		paramJson.put("processId",processIds);
		paramJson.put("method",method);
		paramJson.put("userId",userId);

		dataJson.put("param",paramJson);
		String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
		return getTcpResult(retMsg);
	}*/

/*	public static boolean handProcess(PlatformVO platformVO, PlatformProcess process, String method, String userId) {
		JSONObject dataJson = new JSONObject();
		dataJson.put("funcName",CMD_FUNC);
		dataJson.put("uuid",platformVO.getServerUnique());

		JSONArray cmdArray = new JSONArray();
		String startScript = process.getStartScript();
		String startScriptPath = process.getStartScriptPath();
		String shutdownScript = process.getShutdownScript();
		String shutdownScriptPath = process.getShutdownScriptPath();

		if("start".equals(method)) {
			cmdArray.add(StringUtil.isNotNull(startScript) ? startScript : startScriptPath);
		}
		if("stop".equals(method)) {
			cmdArray.add(StringUtil.isNotNull(shutdownScript) ? shutdownScript : shutdownScriptPath);
		}
		if("restart".equals(method)) {
			cmdArray.add(StringUtil.isNotNull(shutdownScript) ? shutdownScript : shutdownScriptPath);
			cmdArray.add(StringUtil.isNotNull(startScript) ? startScript : startScriptPath);
		}
		JSONObject paramJson = new JSONObject();
		paramJson.put("cmd",cmdArray);
		paramJson.put("registerid","");
		paramJson.put("processId",process.getId()+"");
		paramJson.put("method",method);
		paramJson.put("userId",userId);
		dataJson.put("param",paramJson);

		String retMsg = NettyTcpClient.sendMsg(probeManageIp, JSONObject.toJSONString(dataJson));
		return getTcpResult(retMsg);
	}*/


	public static boolean getTcpResult(String result) {
		if(!"error".equals(result)) {
			JSONObject retObj = JSONObject.parseObject(result);
			if(TCP_SUCESS_RET.equals(retObj.getString("ret"))) {
				return true;
			}
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getTcpResultMap(String result) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
 		if(!"error".equals(result)) {
 			resultMap = JSON.parseObject(result,Map.class);
		}
		return resultMap;
	}
}
