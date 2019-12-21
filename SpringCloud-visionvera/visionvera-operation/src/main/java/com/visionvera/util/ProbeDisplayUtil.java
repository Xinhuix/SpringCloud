package com.visionvera.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 监测探针部署各步骤帮助
 * @author dql714099655
 *
 */
@Component
public class ProbeDisplayUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ProbeDisplayUtil.class);
	
	/**
	 * 注入监测探针远程部署需要的环境变量
	 */
	public static String SW_UPGRADE;
	@Value("${probe.SW_UPGRADE}")
	public void setSwUpgrade(String swUpgrade) {
		SW_UPGRADE = swUpgrade;
	}
	
	/**
	 * 测试服务器远程登录以及登录成功信息   Start
	 */
	public static final String TESTLOGIN_SERVER = "$PM_UPGRADE '<IP>' '<User>' '<PassWord>' testlogin |grep UPGRADE-RESULT-LINE";
	public static final String TESTLOGIN_SUCESS = "login ok";
	/**
	 * 初始化监测探针环境，以及成功信息
	 */
	public static final String INIT_PROBE_ENV = "$PM_UPGRADE '<IP>' '<User>' '<PassWord>' init |grep UPGRADE-RESULT-LINE";
	public static final String INIT_PROBE_ENV_SUCESS ="init ssh ok"; 
	/**
	 * 部署监测探针以及各阶段信息
	 */
	public static final String DISPLAY_PROBE 
			= "$PM_UPGRADE '<IP>' '<User>' '<PassWord>' install <installPackPath> | grep UPGRADE-STATUS-LINE";
	public static final String SFTP_ZIP_SUCESS = "sftp zip file,ok"; //上传安装包成功
	public static final String UNZIP_SUCESS = "unzip zip file,ok";	//解压安装包成功
	public static final String LINK_RUN_PATH_SUCESS = "link run path,ok";	//建立软连接成功
	public static final String INSTALL_SERVICE_SUCESS = "install service,ok";	//安装服务成功
	public static final String EXPORT_ENV_SUCESS = "export env,ok";	//导入环境变量成功
	public static final String INSTALL_PROBE_SUCESS = "install-finished-with-ok";	//检测探针安装成功
	/**
	 * 启动监测探针以及成功提示信息
	 */
	public static final String START_PROBE = "sh $PM_UPGRADE '<IP>' '<User>' '<PassWord>' start |grep UPGRADE-RESULT-LINE";
	public static final String START_PROBE_SUCESS = "v2vprobemonitor run success";
	/**
	 * 停止监测探针以及成功提示信息
	 */
	public static final String STOP_PROBE = "$PM_UPGRADE '<IP>' '<User>' '<PassWord>' stop| grep UPGRADE-RESULT-LINE";
	public static final String STOP_PROBE_SUCESS_NOTONLINE = "Stop v2vprobemonitor";
	public static final String STOP_PROBE_SUCESS_ONLINE = "kill";
	public static final String STOP_PROBE_INSTALL_FIRSTLY = "please install program firstly";
	
	/**
	 * 停止JAVA监测探针以及成功提示信息
	 */
	public static final String STOP_JAVA_PROBE = "$SW_UPGRADE '<IP>' '<User>' '<PassWord>' stop| grep UPGRADE-RESULT-LINE";
	public static final String STOP_JAVA_PROBE_SUCESS = "stop ok";
	public static final String STOP_JAVA_INSTALL_FIRSTLY = "please install serverwatch firstly";
	/**
	 * 移除监测探针以及成功提示信息
	 */
	public static final String REMOVE_PROBE = "$PM_UPGRADE '<IP>' '<User>' '<PassWord>' remove |grep UPGRADE-RESULT-LINE";
	public static final String REMOVE_PROBE_SUCESS = "remove,ok";

	/**
	 * 重启监测探针以及成功提示信息
	 */
	public static final String RESTART_PROBE = "$PM_UPGRADE '<IP>' '<User>' '<PassWord>' restart | grep UPGRADE-RESULT-LINE";
	public static final String RESTART_PROBE_SUCESS = "Stop v2vprobemonitor";
	
	/**
	 * 查看探针状态以及成功失败提示信息
	 */
	public static final String PROBE_STATUS = "$PM_UPGRADE '<IP>' '<User>' '<PassWord>' status | grep UPGRADE-RESULT-LINE";
	public static final String PROBE_STATUS_SUCESS = "v2vprobemonitor run success";
	public static final String PROBE_STATUS_FAILD = "v2vprobemonitor run faild";
	
	/**
	 * windows，IP环境自动升级url
	 */
	public static final String UPGRADE_PROBE_URL = "/serverwatch/probe/upGradeProbe";
	/**
	 * 获取探针的最新版本
	 */
	public static final String PROBE_CURRENT_VERSION = "/serverwatch/probe/getCurrentVersion";
	
	/**
	 * 监测探针状态常量
	 */
	public static final Integer PROBE_STATE_NOT_DIPLAY = 0;	//未部署
	public static final Integer PROBE_STATE_DIPLAYED = 1;	//已部署
	public static final Integer PROBE_STATE_CONFIGURED = 2;	//已配置
	public static final Integer PROBE_STATE_DELETE = 3;	//探针已删除
	/**
	 * 监测探针启动状态常量
	 */
	public static final Integer PROBE_STATE_OPEN = 1;  //检测端启动状态
	public static final Integer PROBE_STATE_STOP = 0;	//检测端关闭状态
	
	/**
	 * 设置环境变量
	 * @param envVar
	 */
	private static Map<String,String> setEnvVar(String envVar) {
		Map<String,String> envMap = new HashMap<String,String>();
		envMap.put("SW_UPGRADE", SW_UPGRADE);
		return envMap;
	}
	
	/**
	 * 测试服务器远程登录
	 * @param ip  服务器IP
	 * @param user	SSH用户
	 * @param password	SSH密码
	 * @return  远程登录成功返回true,否则返回false
	 */
	public static boolean testRemoteLogin(String ip,String user,String password) {
		Map<String,String> envMap = setEnvVar(SW_UPGRADE);
		String command = TESTLOGIN_SERVER.replace("<IP>", ip).replace("<User>", user)
				.replace("<PassWord>", password);
		logger.info("测试服务器登录，执行命令：" + command);
		String[] loginCommand = new String[]{"sh","-c",command};
		String result = ProcessUtil.execute(loginCommand, envMap);
		logger.info("测试服务器登录，返回结果："+ result);
		if(StringUtil.isNotNull(result) && result.contains(TESTLOGIN_SUCESS)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 初始化监测探针环境 
	 * @param ip	服务器IP
	 * @param user  SSH用户
	 * @param password  SSH密码
	 * @return
	 */
	public static boolean initProbeEnv(String ip,String user,String password) {
		Map<String,String> envMap = setEnvVar(SW_UPGRADE);
		String command = INIT_PROBE_ENV.replace("<IP>", ip).replace("<User>", user)
				.replace("<PassWord>", password);
		logger.info("初始化监测探针环境，执行命令：" + command);
		String[] initCommand = new String[]{"sh","-c",command};
		String result = ProcessUtil.execute(initCommand, envMap);
		logger.info("初始化监测探针环境，返回结果："+ result);
		if(StringUtil.isNotNull(result) && result.contains(INIT_PROBE_ENV_SUCESS)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 部署监测探针
	 * @param ip 服务器ip
	 * @param user SSH用户
	 * @param password  SSH密码
	 * @param installPackPath  安装包目录
	 * @return	map<String,Object>   ret：标识部署结果，true成功，false失败    msg:部署信息
	 */
	public static Map<String,Object> displayProbe(String ip,String user,String password,String installPackPath) {
		Map<String,String> envMap = setEnvVar(SW_UPGRADE);
		//DISPLAY_PROBE = "$PM_UPGRADE '<IP>' '<User>' '<PassWord>' install <installPackPath> | grep UPGRADE-STATUS-LINE";
		String command = DISPLAY_PROBE.replace("<IP>", ip).replace("<User>", user)
					.replace("<PassWord>", password).replace("<installPackPath>", installPackPath);
		logger.info("部署监测探针，执行命令："+command);
		String[] displayCommand = new String[]{"sh","-c",command};
		String result = ProcessUtil.execute(displayCommand, envMap);
		logger.info("部署监测探针，返回结果："+result);
		Map<String,Object> retMap = new HashMap<String, Object>();
		retMap.put("ret", false);
		if(StringUtil.isNotNull(result)){
			if(!result.contains(SFTP_ZIP_SUCESS)) {
				retMap.put("msg", "部署失败，失败原因：上传安装包失败");
				return retMap;
			}else if(!result.contains(UNZIP_SUCESS)) {
				retMap.put("msg", "部署失败，失败原因：解压安装包失败");
				return retMap;
			}else if(!result.contains(LINK_RUN_PATH_SUCESS)) {
				retMap.put("msg", "部署失败，失败原因：建立软连接失败");
				return retMap;
			}/*else if(!result.contains(INSTALL_SERVICE_SUCESS)) {
				retMap.put("msg", "部署失败，失败原因：安装服务失败");
				return retMap;
			}else if(!result.contains(EXPORT_ENV_SUCESS)) {
				retMap.put("msg", "部署失败，失败原因：导入环境变量失败");
				return retMap;
			}*/
			
			retMap.put("ret", true);
			retMap.put("msg", "部署成功");
			return retMap;
		}
		retMap.put("msg", "部署失败，失败原因：未知错误");
		return retMap;
	}

	/**
	 * 启动监测探针
	 * @param ip	服务器ip
	 * @param user	SSH用户
	 * @param password	SSH密码
	 * @return 启动成功返回 true,失败返回 false
	 */
	public static boolean startProbe(String ip,String user,String password) {
		Map<String, String> envMap = setEnvVar(SW_UPGRADE);
		String command = START_PROBE.replace("<IP>", ip).replace("<User>", user)
				.replace("<PassWord>", password);
		logger.info("启动监测探针，执行命令："+command);
		String[] startCommand = new String[]{"sh","-c",command};
		String result = ProcessUtil.execute(startCommand, envMap);
		logger.info("启动监测探针，返回结果："+ result);
		if(StringUtil.isNotNull(result) && result.contains(START_PROBE_SUCESS)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 停止监测探针
	 * @param ip	服务器IP
	 * @param user	SSH用户
	 * @param password SSH密码
	 * @return true:停止监测探针成功，false:停止探针失败
	 */
	public static boolean stopProbe(String ip,String user,String password) {
		Map<String,String> envMap = setEnvVar(SW_UPGRADE);
		String command = STOP_PROBE.replace("<IP>", ip).replace("<User>", user)
				.replace("<PassWord>", password);
		logger.info("停止监测探针，执行命令："+command);
		String[] stopCommand = new String[]{"sh","-c",command};
		String result = ProcessUtil.execute(stopCommand, envMap);
		logger.info("停止监测探针，返回结果：" + result);
		if(StringUtil.isNotNull(result) && (result.contains(STOP_PROBE_SUCESS_NOTONLINE)||result.contains(STOP_PROBE_SUCESS_ONLINE)||
		result.contains(STOP_PROBE_INSTALL_FIRSTLY)		)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * 停止Java监测探针
	 * @param ip	服务器IP
	 * @param user	SSH用户
	 * @param password SSH密码
	 * @return true:停止监测探针成功，false:停止探针失败
	 */
	public static boolean stopJavaProbe(String ip,String user,String password) {
		Map<String,String> envMap = setEnvVar(SW_UPGRADE);
		String command = STOP_JAVA_PROBE.replace("<IP>", ip).replace("<User>", user)
				.replace("<PassWord>", password);
		logger.info("停止监测探针，执行命令："+command);
		String[] stopCommand = new String[]{"sh","-c",command};
		String result = ProcessUtil.execute(stopCommand, envMap);
		logger.info("停止监测探针，返回结果：" + result);
		if(StringUtil.isNotNull(result) && (result.contains(STOP_JAVA_PROBE_SUCESS)||result.contains(STOP_JAVA_INSTALL_FIRSTLY))) {
			return true;
		}
		return false;
	}
	
	/**
	 * 移除监测探针
	 * @param ip	服务器IP
	 * @param user	SSH用户
	 * @param password	SSH密码
	 * @return	true:移除监测探针成功，false:移除监测探针失败
	 */
	public static boolean removeProbe(String ip,String user,String password) {
		try {
			
			Map<String,String> envMap = setEnvVar(SW_UPGRADE);
			String command = REMOVE_PROBE.replace("<IP>", ip).replace("<User>", user)
					.replace("<PassWord>", password);
			logger.info("移除监测探针，执行命令：" + command);
			String[] removeCommand = new String[]{"sh","-c",command};
			String result = ProcessUtil.execute(removeCommand, envMap);
			logger.info("移除监测探针，返回结果：" + result);
			if(StringUtil.isNotNull(result) && result.contains(REMOVE_PROBE_SUCESS)) {
				return true;
			}
			return false;
		}catch(Exception e) {
			return false;
		}
	}
	
	/**
	 * 重启检测探针
	 * @param ip 服务器IP
	 * @param user	SSH用户
	 * @param password SSH密码
	 * @return true:重启探针成功，false:重启探针失败
	 */
	public static boolean restartProbe(String ip,String user,String password) {
		Map<String,String> envMap = setEnvVar(SW_UPGRADE);
		String command = RESTART_PROBE.replace("<IP>", ip).replace("<User>", user)
				.replace("<PassWord>", password);
		logger.info("重启监测探针，执行命令："+command);
		String[] restartCommand = new String[]{"sh","-c",command};
		String result = ProcessUtil.execute(restartCommand, envMap);
		logger.info("重启监测探针，返回结果：" + result);
		if(StringUtil.isNotNull(result) && result.contains(RESTART_PROBE_SUCESS)) {
			return true;
		}
		return false;
	}
	/**
	 * 查看探针状态
	 * @param ip 服务器IP
	 * @param user	SSH用户
	 * @param password SSH密码
	 * @return true:重启探针成功，false:重启探针失败
	 */
	public static boolean getProbeStatus(String ip,String user,String password) {
		Map<String,String> envMap = setEnvVar(SW_UPGRADE);
		String command = PROBE_STATUS.replace("<IP>", ip).replace("<User>", user)
				.replace("<PassWord>", password);
		logger.info("查看探针状态，执行命令："+command);
		String[] statusCommand = new String[]{"sh","-c",command};
		String result = ProcessUtil.execute(statusCommand, envMap);
		logger.info("查看探针状态，返回结果：" + result);
		if(StringUtil.isNotNull(result) && result.contains(PROBE_STATUS_SUCESS)) {
			return true;
		}
		return false;
	}
	
}
