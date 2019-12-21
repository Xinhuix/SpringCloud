package com.visionvera.util;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.constrant.GlobalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 修改IP监测探针配置
 * @author dql714099655
 *
 */
@Component
public class IPProbeReqUtil {
	
	private static Logger logger = LoggerFactory.getLogger(IPProbeReqUtil.class);
	
	/**
	 * 同步运维平台ip到监测探针
	 * @param serverBasics
	 * @return
	 */
	public static boolean synchrOperationIp(ServerBasics serverBasics) {
		String manageIp = serverBasics.getServerManageIp();
		Integer port = serverBasics.getPort();
		String operationIp = serverBasics.getOperationIp();
		String url = String.format(GlobalConstants.UPDATE_OPERATION_URL,manageIp,port);
		logger.info("同步微服务operation服务的ip到监测探针，url:"+url);
		
		Map<String,Object> resultMap = RestTemplateUtil.postForObject(url,operationIp,Map.class);
		if(resultMap.get("result") != null || (Boolean)resultMap.get("result")) {					
			return true;
		}
		return false;
	}
	
	/**
	 * 停止监测探针检测
	 * @param serverBasics
	 * @return
	 */
	public static boolean stopProbeCheck(ServerBasics serverBasics) {
		String manageIp = serverBasics.getServerManageIp();
		Integer port = serverBasics.getPort();
		String url = String.format(GlobalConstants.STOP_PROBE_CHECK_URL,manageIp,port);
		logger.info("停止监测探针检测，url:"+url);
		
		Map<String,Object> resultMap = RestTemplateUtil.getForObject(url,Map.class);
		if(resultMap.get("result") != null || (Boolean)resultMap.get("result")) {					
			return true;
		}
		return false;
	}
	
	/**
	 * 启动监测探针检测
	 * @param serverBasics
	 * @return
	 */
	public static boolean startProbeCheck(ServerBasics serverBasics) {
		String manageIp = serverBasics.getServerManageIp();
		Integer port = serverBasics.getPort();
		String url = String.format(GlobalConstants.START_PROBE_CHECK_URL,manageIp,port);
		logger.info("启动监测探针检测，url:"+url);
		
		Map<String,Object> resultMap = RestTemplateUtil.getForObject(url,Map.class);
		if(resultMap.get("result") != null || (Boolean)resultMap.get("result")) {					
			return true;
		}
		return false;
	}
	
	/**
	 * 移除Windows系统Ip版探针
	 * @param serverBasics
	 * @return
	 */
	public static boolean removeProbe(ServerBasics serverBasics) {
		try {
			String manageIp = serverBasics.getServerManageIp();
			Integer port = serverBasics.getPort();
			String url = String.format(GlobalConstants.REMOVE_PROBE_URL,manageIp,port);
			logger.info("移除监测探针，url:"+url);
			
			Map<String,Object> resultMap = RestTemplateUtil.getForObject(url,Map.class);
			if(resultMap.get("result") != null || (Boolean)resultMap.get("result")) {					
				return true;
			}
			return false;
		} catch(Exception e) {
			return false;
		}
	}
	
	public static boolean handleProcess(ServerBasics serverBasics,String method,PlatformProcess process){
		String manageIp = serverBasics.getServerManageIp();
		Integer port = serverBasics.getPort();
		String url = String.format(GlobalConstants.HANDLE_PROCESS,manageIp,port,method);
		logger.info("操作探针进程，url:"+url);
		logger.info("请求参数为"+JSONObject.toJSONString(process));

		Map<String,Object> resultMap = RestTemplateUtil.postForObject(url,process,Map.class);
		logger.info("操作结果返回为："+JSONObject.toJSONString(resultMap));
		if(resultMap.get("result") != null || (Boolean)resultMap.get("result")) {					
			return true;
		}
		return false;
	}

	public static int compareProbeVersion(String newVersion,String oldVersion) {
		try {
			String[] newVersionArr = newVersion.split("\\.");
			String[] oldVersionArr = oldVersion.split("\\.");
			for(int i = 0; i < newVersionArr.length; i++) {
				int newV = Integer.parseInt(newVersionArr[i]);
				int oldV =0;
				if(i<oldVersionArr.length){
					oldV = Integer.parseInt(oldVersionArr[i]);
				}
				int offSet = newV - oldV;
				if(offSet != 0) {
					return offSet;
				}
			}
		} catch(Exception e) {
			logger.error("比较探针版本号发生异常",e);
		}
		return 0;
	}
	
	public static int compareProbeVersionSupportDifferentLength(String newVersion,String oldVersion) {
		try {
			String[] newVersionArr = newVersion.split("\\.");
			String[] oldVersionArr = oldVersion.split("\\.");
			int newLength = newVersionArr.length;
			int oldLength = oldVersionArr.length;
			
			if(newLength-oldLength>0){
				return 1;
			}else if(newLength==oldLength){
				for(int i = 0; i < newLength; i++) {
					int newV = Integer.parseInt(newVersionArr[i]);
					int oldV =0;
					if(i<oldLength){
						oldV = Integer.parseInt(oldVersionArr[i]);
					}
					int offSet = newV - oldV;
					if(offSet != 0) {
						return offSet;
					}
				}
			}else{
				return -1;
			}
			
			
		} catch(Exception e) {
			logger.error("比较探针版本号发生异常",e);
		}
		return 0;
	}


	public static void main(String[] args) {
		String newVersion = "3.5.5";
		String oldVersion = "2.1.0.0";
		System.out.println(compareProbeVersionSupportDifferentLength(newVersion,oldVersion));
	}
}
