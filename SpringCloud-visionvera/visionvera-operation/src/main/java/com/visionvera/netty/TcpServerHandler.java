package com.visionvera.netty;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.alarm.AlarmDomain;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.bean.slweoms.ServerHardwareVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.enums.TransferType;
import com.visionvera.service.*;
import com.visionvera.util.IPProbeReqUtil;
import com.visionvera.util.ProbeDisplayUtil;
import com.visionvera.util.ProbeManagerMsgUtil;
import com.visionvera.util.StringUtil;
import com.visionvera.websocket.WebSocketPushMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 处理Tcp接收的消息handler
 * @author dql714099655
 *
 */
@Component
@Sharable
public class TcpServerHandler extends ChannelInboundHandlerAdapter{
	
	private Logger logger = LoggerFactory.getLogger(TcpServerHandler.class);
	
	@Autowired
	private ServerBasicsService serverService;
	
	@Autowired
	private ServersHardwareService serversHardwareService;
	
	@Autowired
	private WatchProbeService probeService;
	
	@Autowired
	private AlarmService alarmService;
	
	@Autowired
	private PlatformProcessService processService;

	@Autowired
	private PlatformService platformService;

	@Autowired
	private JRedisDao jRedisDao;
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			String message = (String)msg;
			logger.info("Netty tcp server 接收消息 ：" + message);
			
			//判断是否为心跳包
			if(GlobalConstants.TCP_HEARTBEAT_PACKAGE.equals(message)) {
				ctx.writeAndFlush(GlobalConstants.TCP_HEARTBEAT_PACKAGE + GlobalConstants.TCP_END_DELIMITER);
				return;
			}
			
			//获取正文内容
			message = message.replaceAll("##\\*\\*\\d{8}json", "");
			handleTcpMessage(message);
			
			//ctx.writeAndFlush("接收消息sucess：" + message);
			
		} catch(Exception e) {
			logger.error("Netty tcp server 异常", e);
			e.printStackTrace();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("===Netty Tcp Server 出现异常====", cause);
        if(cause instanceof java.io.IOException) {
            ctx.close();
            return;
        }
        super.exceptionCaught(ctx, cause);
        ctx.close();
	}
	
	private void handleTcpMessage(String message) throws Exception{
		
		Map<String,Object> resultMap = new HashMap<String,Object>();
		JSONObject msgObj = JSONObject.parseObject(message);
 		String funcName = msgObj.getString("funcName");
 		String userId = msgObj.getString("userId");
 		if(StringUtil.isNotNull(userId)) {
            String uuid = msgObj.getString("uuid");
            String registerid = StringUtil.isNotNull(msgObj.getString("registerid"))?
                    msgObj.getString("registerid"):msgObj.getString("platid");
            String procId = StringUtil.isNotNull(msgObj.getString("processIds"))?
                    msgObj.getString("processIds"):msgObj.getString("processId");
            if(StringUtil.isNotNull(uuid)) {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+funcName
                        +":"+uuid+":::"+userId);
            }
            if(StringUtil.isNotNull(registerid)) {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+funcName
                        +"::"+registerid+"::"+userId);
            }
            if(StringUtil.isNotNull(procId)) {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+funcName
                        +":::"+procId+":"+userId);
            }
        }

		switch(funcName) {
			//应用服务器硬件信息
			case GlobalConstants.TCP_DEVICE_FLAG:
				ServerHardwareVO serverHardwareVO = encapsulateDeviceInfo(msgObj);
				serversHardwareService.insertServerHardwareVO(serverHardwareVO);
				//检测应用服务器阈值产生报警
				alarmService.checkServerHardwareVO(serverHardwareVO);
				break;
			//探针离线报警	
			case GlobalConstants.TCP_OFFLINE_FLAG:
				alarmService.insertAlarmOffLine(message);
				break;
			//平台离线告警
			/*case GlobalConstants.TCP_PLATFORM_FLAG:
				alarmService.insertAlarmPlatform(message);
				break;*/
			//版本检测上报	
			case ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC:
				handleRecentVersion(message);
				break;
			case ProbeManagerMsgUtil.PROBE_OPEN_STATE:
				String ret = msgObj.getString("ret");
				String state = msgObj.getString("state");
				String uuid = msgObj.getString("uuid");

				resultMap.put("func", ProbeManagerMsgUtil.PROBE_OPEN_STATE);
				if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
					resultMap.put("errcode",0);
					resultMap.put("server",uuid);
					ServerBasics serverBasics = serverService.getServerBasicsByServerUnique(uuid);
					if("start".equals(state)) {
						resultMap.put("msg","开启监测探针成功");
						serverBasics.setOpenState(ProbeDisplayUtil.PROBE_STATE_OPEN);
					}else if("stop".equals(state)) {
						resultMap.put("msg","关闭监测探针成功");
						serverBasics.setOpenState(ProbeDisplayUtil.PROBE_STATE_STOP);
					}
					serverService.updateServerBasics(serverBasics);
				}else {
					resultMap.put("errcode",1);
					if("start".equals(state)) {
						resultMap.put("msg","开启监测探针失败");
					} else if("stop".equals(state)) {
						resultMap.put("msg","关闭监测探针失败");
					}
				}
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.PROBE_UPGRADE:
				probeUpgrade(msgObj);
				break;
			case ProbeManagerMsgUtil.TCP_GET_PROBE_VERSION:
				String version = msgObj.getString("version");
				uuid = msgObj.getString("uuid");
				ret = msgObj.getString("ret");

				resultMap = new HashMap<>();
				resultMap.put("func", ProbeManagerMsgUtil.TCP_GET_PROBE_VERSION);
				if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
					ServerBasics serverBasics = serverService.getServerBasicsByServerUnique(uuid);
					if(ProbeDisplayUtil.PROBE_STATE_NOT_DIPLAY.equals(serverBasics.getState())) {
						serverBasics.setState(ProbeDisplayUtil.PROBE_STATE_DIPLAYED);
					}
					serverBasics.setVersion(version);
					serverService.updateServerBasics(serverBasics);
					resultMap.put("errcode",0);
					resultMap.put("server",uuid);
					resultMap.put("version",version);
					resultMap.put("msg","获取监测探针版本成功");
				} else {
					resultMap.put("errcode",1);
					resultMap.put("server",uuid);
					resultMap.put("msg","获取监测探针版本失败");
				}
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.ADD_PROC:
				resultMap = resAddProcess(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.ADD_PROC);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.MODIFY_PROC:
				resultMap = resModifyProcess(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.MODIFY_PROC);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.DEL_PROC:
				resultMap = resDelProcess(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.DEL_PROC);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.CMD_FUNC:
				resultMap = processHandle(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.CMD_FUNC);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
		    //主动检测唐古拉平台配置  检测后上报的告警	
			case GlobalConstants.TCP_TGL_CHECK_INFO:
				alarmService.insertTglAlarm(msgObj);
				break;
			//GIS平台配置检测上报告警
			case ProbeManagerMsgUtil.PLATFORM_CHECKALARM:
				alarmService.addCheckConfigAlarm(msgObj);
				break;	
            //转发唐古拉告警				
			case GlobalConstants.TCP_TGL_ALARM:
				alarmService.reportTglConfigAlarm(msgObj);
				break;
			case ProbeManagerMsgUtil.ADD_PLATFORM:
				resultMap = resAddPlatform(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.ADD_PLATFORM);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.MODIFY_PLATFORM:
				resultMap = resModifyPlatform(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.MODIFY_PLATFORM);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.DEL_PLATFORM:
				resultMap = resDelPlatform(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.DEL_PLATFORM);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.DEL_PROBE:
				resultMap = resDelServer(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.DEL_PROBE);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			case ProbeManagerMsgUtil.TCP_DOWNLOAD_PROBE:
				ret = msgObj.getString("ret");
				String type = msgObj.getString("type");
				if("web".equals(type)) {
					resultMap = new HashMap<>();
					resultMap.put("func", ProbeManagerMsgUtil.TCP_DOWNLOAD_PROBE);
					if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
						resultMap.put("errcode",0);
						resultMap.put("msg","探针安装包同步成功");
					}else {
						resultMap.put("errcode",1);
						resultMap.put("msg","探针安装包同步失败");
					}

					Set<Object> userIdSet = jRedisDao.getSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+"download");
					jRedisDao.delSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+"download");
					jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+"download:sub");
					//获取所有的session
					for (Object userIdObj : userIdSet) {
						String user = (String)userIdObj;
						Session session = WebSocketPushMessage.getWebSocketMap().get(user);
						WebSocketPushMessage.sendMessage(session,JSONObject.toJSONString(resultMap));
					}
				}
				break;
			case ProbeManagerMsgUtil.MODIFY_PROBE:
				resultMap = modifyServer(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.MODIFY_PROBE);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;
			//协转版本不一致告警或恢复告警	
			case GlobalConstants.PROBE_PLATFORM_ALARM:
				alarmService.addProbePlatformAlarm(msgObj.getJSONObject("param").toJavaObject(AlarmDomain.class));
				break;
			//进程重启
			/*case ProbeManagerMsgUtil.PROCESS_RESTART:
				resultMap = processRestart(msgObj);
				resultMap.put("func", ProbeManagerMsgUtil.PROCESS_RESTART);
				WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(userId),
						JSONObject.toJSONString(resultMap));
				break;*/
			//离线进程上报告警		
			case ProbeManagerMsgUtil.PROCESS_INFO_ALARM:
				alarmService.reportOfflineProcessAlarm(msgObj);
				break;
			//异常进程上报告警	
			case ProbeManagerMsgUtil.PROCESS_INFO:
				alarmService.reportExceptionProcessAlarm(msgObj);
				break;
			//异常端口上报告警		
			case ProbeManagerMsgUtil.PORT_INFO:
				alarmService.reportExceptionPortAlarm(msgObj);
				break;	
			//进程状态变更页面刷新	
			case ProbeManagerMsgUtil.INFO_CHANGE:
				WebSocketPushMessage.sendToAllUser(message);
				break;	
			default: break;
		}
	}

	/**
	 * 修改服务器
	 * @param msgObj
	 */
	private Map<String,Object> modifyServer(JSONObject msgObj) {
		Map<String,Object> resultMap = new HashMap<>();
		try {
			String ret = msgObj.getString("ret");
			String uuid = msgObj.getString("uuid");
			String userId = msgObj.getString("userId");
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PROBE
                    +":"+uuid+":::"+userId);

            String serverStr = jRedisDao.get(ProbeManagerMsgUtil.MODIFY_PROBE+":"+uuid+":"+userId);
            jRedisDao.del(ProbeManagerMsgUtil.MODIFY_PROBE+":"+uuid+":"+userId);
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				ServerBasics serverBasics = JSONObject.parseObject(serverStr,ServerBasics.class);
				ReturnData returnData = serverService.updateServerBasics(serverBasics);
				resultMap.put("errcode",returnData.getErrcode()==0?0:1);
				resultMap.put("server",uuid);
				resultMap.put("msg",returnData.getErrcode()==0?"服务器信息修改成功":"服务器信息修改失败");
			}else {
				resultMap.put("errcode",1);
				resultMap.put("server",uuid);
				resultMap.put("msg","服务器修改失败");
			}
		}catch(Exception e) {
		    logger.error("修改服务器异常",e);
			String uuid = msgObj.getString("uuid");
			resultMap.put("errcode",1);
			resultMap.put("server",uuid);
			resultMap.put("msg","服务器修改异常");
		}
		return resultMap;
	}

	/**
	 * 探针升级
	 * @param msgObj
	 * @return
	 */
	private void probeUpgrade(JSONObject msgObj) {
		Map<String,Object> resultMap = new HashMap<>();
		resultMap.put("func", ProbeManagerMsgUtil.PROBE_UPGRADE);
		String uuid = msgObj.getString("uuid");
		Set<Object> userIdSet = jRedisDao.getSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "upGrade:" + uuid);
		//jRedisDao.delSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "upGrade:" + uuid);
        jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "upGrade:" + uuid+ ":sub");
		try {
			String version = msgObj.getString("version");
			String ret = msgObj.getString("ret");
			uuid = msgObj.getString("uuid");

			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				ServerBasics serverBasics = serverService.getServerBasicsByServerUnique(uuid);
				serverBasics.setVersion(version);
				//serverService.updateServerBasics(serverBasics,null);
				serverService.updateServerProbeVersion(serverBasics);
				resultMap.put("errcode",0);
				resultMap.put("server",uuid);
				resultMap.put("version",version);
				resultMap.put("msg","监测探针升级成功");
			}else {
				resultMap.put("errcode",1);
				resultMap.put("server",uuid);
				resultMap.put("msg","监测探针升级失败");
			}
		} catch(Exception e) {
			resultMap.put("errcode",1);
			resultMap.put("server",uuid);
			resultMap.put("msg","监测探针升级异常");
		}
		for (Object userIdObj : userIdSet) {
			String userId = (String)userIdObj;
			Session session = WebSocketPushMessage.getWebSocketMap().get(userId);
			try {
				WebSocketPushMessage.sendMessage(session,JSONObject.toJSONString(resultMap));
			} catch(Exception e) {
				logger.error(uuid + "服务器升级返回结果失败",e);
			}
		}
	}

	/**
	 * 删除服务器探针
	 * @param msgObj
	 * @return
	 */
	private Map<String,Object> resDelServer(JSONObject msgObj) {
		Map<String,Object> resultMap = new HashMap<>();
		String serverUnique = msgObj.getString("uuid");
		String msg = msgObj.getString("msg");
		resultMap.put("uuid",serverUnique);
		try {
			String ret = msgObj.getString("ret");
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				ServerBasics serverBasics = serverService.getServerBasicsByServerUnique(serverUnique);
				String userId = msgObj.getString("userId");
				probeService.removeWatchProbe(serverBasics.getId(),userId);
				resultMap.put("errcode",0);
				resultMap.put("msg","服务器删除成功");
				
			}else {
				resultMap.put("errcode",1);
				resultMap.put("msg","服务器删除失败");
			}
		} catch(Exception e) {
		    logger.error("删除服务器异常",e);
			resultMap.put("errcode",1);
			resultMap.put("msg","服务器删除异常");
		}
		return resultMap;
	}
	/**
	 * 重启进程
	 * @param msgObj
	 * @return
	 */
	private Map<String,Object> processRestart(JSONObject msgObj) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			String ret = msgObj.getString("ret");
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				resultMap.put("errcode",0);
			    resultMap.put("msg","进程重启成功");
			}else {
				resultMap.put("errcode",1);
				resultMap.put("msg","进程重启失败");
			}
		} catch(Exception e) {
			logger.error("进程重启异常",e);
			resultMap.put("errcode",1);
			resultMap.put("msg","进程重启异常");
		}
		return resultMap;
	}

	/**
	 * 处理探针最新版本信息。
	 * @param message
	 */
	private void handleRecentVersion(String message) throws Exception{
		JSONObject msgObj = JSONObject.parseObject(message);
		String ret = msgObj.getString("ret");
		String type = msgObj.getString("type");
		Map<String,Object> retMap = new HashMap<String,Object>();
		retMap.put("func", ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC);
		if(ProbeManagerMsgUtil.TCP_LAUNCH_WEB.equals(type)) {

			Set<String> keys = jRedisDao.keys(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC+"*");
			for (String key : keys) {
				jRedisDao.del(key);
			}
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				String localVersion = probeService.getLocalRecentVersion(TransferType.V2V.getTransferType());
				String version = msgObj.getString("version");
				if(StringUtil.isNull(localVersion) || IPProbeReqUtil.compareProbeVersionSupportDifferentLength(version,localVersion) > 0) {
					retMap.put("errcode",3);
					retMap.put("msg","检测到更新版本");
					retMap.put("version",version);
				}else {
					retMap.put("errcode",0);
					retMap.put("msg","当前版本为最新版本");
					retMap.put("version",version);
				}
			}else {
				logger.error("探针管理获取最新的探针版本失败");
				retMap.put("errcode",1);
				retMap.put("msg","检测最新版本失败");
			}
			for (String key : keys) {
				String userId = key.split(":")[5];
				Session session = WebSocketPushMessage.getWebSocketMap().get(userId);
				if(session != null) {
					WebSocketPushMessage.sendMessage(session,JSONObject.toJSONString(retMap));
				}
			}

		}else {
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				String localVersion = probeService.getLocalRecentVersion(TransferType.V2V.getTransferType());
				String version = msgObj.getString("version");
				if(StringUtil.isNull(localVersion) || IPProbeReqUtil.compareProbeVersionSupportDifferentLength(version,localVersion) > 0) {
					boolean retFlag = ProbeManagerMsgUtil.syncV2vProbeInstallPac(ProbeManagerMsgUtil.TCP_LAUNCH_BACK, "");
					if(retFlag) {
						logger.info("通知探针管理服务同步最新的监测探针成功，版本："+version);
					}else {
						logger.error("通知探针管理服务同步最新的监测探针失败");
					}
				}
			} else {
				logger.error("后台同步安装包失败");
			}
		}

	}

	//处理接受的服务器硬件信息，封装到ServerHardwareVO对象中
	private ServerHardwareVO encapsulateDeviceInfo(JSONObject devInfo) {
		ServerHardwareVO serverHardwareVO = new ServerHardwareVO();
		serverHardwareVO.setServerUnique(devInfo.getString("uuid"));
		
		//cpu信息
		JSONObject cpuObj = devInfo.getJSONObject("cpu");
		serverHardwareVO.setServerCPUType(cpuObj.getString("cpuModel"));
		serverHardwareVO.setServerCPUnumber(Integer.valueOf(cpuObj.getString("cpuCores")));
		serverHardwareVO.setServerCPUSumRate(cpuObj.getString("cpuTotal"));
		JSONArray cpuArray = cpuObj.getJSONArray("eachCpu");
		StringBuffer eachCpuRateBuffer = new StringBuffer();
		for (Object eachCpuObj : cpuArray) {
			JSONObject cpu = (JSONObject)eachCpuObj;
			eachCpuRateBuffer.append(cpu.getString("rate")).append(",");
		}
		serverHardwareVO.setServerCPUEveryRate(StringUtil.removeSuffix(eachCpuRateBuffer.toString(), ","));
		
		//内存信息
		JSONObject ddrObj = devInfo.getJSONObject("memory");
		serverHardwareVO.setServerDDRGross(Long.valueOf(ddrObj.getString("memoryTotal")));
		serverHardwareVO.setServerDDRUsable(Long.valueOf(ddrObj.getString("memoryAvail")));
		serverHardwareVO.setServerDDRRate(ddrObj.getString("memoryRate"));
		
		//硬盘信息
		JSONObject hddObj = devInfo.getJSONObject("disk");
		Long diskTotal = Long.valueOf(hddObj.getString("diskTotal"));
		serverHardwareVO.setServerHDDGross(diskTotal);
		JSONArray hddArray = hddObj.getJSONArray("disk");

		StringBuffer driveNameBuf = new StringBuffer();
		StringBuffer everyDriveTotalBuf = new StringBuffer();
		StringBuffer everyDriveUseBuf = new StringBuffer();
		StringBuffer everyDriveUseRateBuf = new StringBuffer();
		Long diskUse = 0L;
		for (Object eachHddObj : hddArray) {
			JSONObject hdd = (JSONObject)eachHddObj;
			driveNameBuf.append(hdd.getString("drive")).append(",");
			String eachTotal = hdd.getString("total");
			String eachUse = hdd.getString("use");
			String eachRate = String.format("%.2f", Double.parseDouble(eachUse) * 100 / Double.parseDouble(eachTotal));
			everyDriveTotalBuf.append(eachTotal).append(",");
			everyDriveUseBuf.append(eachUse).append(",");
			everyDriveUseRateBuf.append(eachRate).append(",");
			diskUse += Long.valueOf(eachUse);
		}
		serverHardwareVO.setServerHDDName(StringUtil.removeSuffix(driveNameBuf.toString(), ","));
		serverHardwareVO.setServerHDDVolume(StringUtil.removeSuffix(everyDriveTotalBuf.toString(), ","));
		serverHardwareVO.setServerHDDUsage(StringUtil.removeSuffix(everyDriveUseBuf.toString(), ","));
		serverHardwareVO.setServerHDDRate(StringUtil.removeSuffix(everyDriveUseRateBuf.toString(), ","));
		serverHardwareVO.setServerHDDAllRate(String.format("%.2f", diskUse.doubleValue() * 100 / diskTotal.doubleValue()));
	
		//网卡信息
		JSONObject netObj = devInfo.getJSONObject("net");
		JSONArray netArray = netObj.getJSONArray("net");

		StringBuffer everyNetNameBuf = new StringBuffer();
		StringBuffer everyNetMacBuf = new StringBuffer();
		StringBuffer everyIpBuf = new StringBuffer();
		StringBuffer everyUpBuf = new StringBuffer();
		StringBuffer everyDownBuf = new StringBuffer();
		for (Object eachNet : netArray) {
			JSONObject net = (JSONObject)eachNet;
			everyNetNameBuf.append(net.getString("name")).append(",");
			everyNetMacBuf.append(net.getString("mac")).append(",");
			everyIpBuf.append(net.getString("ip")).append(",");
			everyUpBuf.append(net.getString("up")).append(",");
			everyDownBuf.append(net.getString("down")).append(",");
		}
		serverHardwareVO.setNetCardName(StringUtil.removeSuffix(everyNetNameBuf.toString(), ","));
		serverHardwareVO.setNetWorkMacAddr(StringUtil.removeSuffix(everyNetMacBuf.toString(), ","));
		serverHardwareVO.setServerNETIP(StringUtil.removeSuffix(everyIpBuf.toString(), ","));
		serverHardwareVO.setServerNETUpData(StringUtil.removeSuffix(everyUpBuf.toString(), ","));
		serverHardwareVO.setServerNETDownData(StringUtil.removeSuffix(everyDownBuf.toString(), ","));
		serverHardwareVO.setCreateTime(new Date());
		return serverHardwareVO;
	}

	/**
	 * 返回添加平台结果信息
	 * @param addPlatformJson
	 */
	private Map<String,Object> resAddPlatform(JSONObject addPlatformJson) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String ret = addPlatformJson.getString("ret");
		String platid = addPlatformJson.getString("platid");
		try {
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				platformService.recoverPlatform(platid);
				resultMap.put("errcode",0);
				resultMap.put("msg","添加平台成功");
			}else {
				resultMap.put("errcode",1);
				resultMap.put("msg","添加平台失败");
			}
		} catch(Exception e) {
			logger.error("添加平台异常", e);
			resultMap.put("errcode",1);
			resultMap.put("msg","添加平台异常");
		}
		resultMap.put("tposRegisterid",platid);
		return resultMap;
	}

	/**
	 * 返回修改平台结果
	 * @param modifyPlatformJson
	 * @return
	 */
	private Map<String,Object> resModifyPlatform(JSONObject modifyPlatformJson) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String ret = modifyPlatformJson.getString("ret");
		String platid = modifyPlatformJson.getString("platid");
		String userId = modifyPlatformJson.getString("userId");
		try {
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				String platformStr = jRedisDao.get(GlobalConstants.V2V_FUNC_REDIS_PREFIX + ProbeManagerMsgUtil.MODIFY_PLATFORM + userId);
				PlatformVO platformVO = JSONObject.parseObject(platformStr,PlatformVO.class);
				platformService.updatePlatform(platformVO);
				resultMap.put("errcode",0);
				resultMap.put("msg","修改平台成功");
			}else {
				resultMap.put("errcode",1);
				resultMap.put("msg","修改平台失败");
			}
		} catch(Exception e) {
			logger.error("修改平台异常", e);
			resultMap.put("errcode",1);
			resultMap.put("msg","修改平台异常");
		}
		resultMap.put("tposRegisterid",platid);
		return resultMap;
	}

	/**
	 * 删除平台
	 * @param delPlatformJson
	 * @return
	 */
	private Map<String,Object> resDelPlatform(JSONObject delPlatformJson) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String ret = delPlatformJson.getString("ret");
		String platid = delPlatformJson.getString("platid");
		resultMap.put("tposRegisterid",platid);
		try {
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				platformService.deletePlatform(platid);
				resultMap.put("errcode",0);
				resultMap.put("msg","删除平台成功");
				return resultMap;
			}
			resultMap.put("errcode",1);
			resultMap.put("msg","删除平台失败");
		} catch(Exception e) {
			logger.error("删除平台异常",e);
			resultMap.put("errcode",1);
			resultMap.put("msg","删除平台异常");
		}
		return resultMap;
	}

	/**
	 * 添加进程
	 * @param addProcessJson
	 * @return
	 */
	private Map<String,Object> resAddProcess(JSONObject addProcessJson) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String processIds = addProcessJson.getString("processIds");
		resultMap.put("procId",processIds);
		try {
			String ret = addProcessJson.getString("ret");
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				processService.recoverProcessStateByIds(processIds);
				resultMap.put("errcode",0);
				resultMap.put("msg","添加监测进程成功");
				return resultMap;
			}
			resultMap.put("errcode",1);
			resultMap.put("msg","添加监测进程失败");
		} catch(Exception e) {
			resultMap.put("errcode",1);
			resultMap.put("msg","添加监测进程异常");
		}
		return resultMap;
	}

	/**
	 * 修改进程
	 * @param modifyProcessJson
	 * @return
	 */
	private Map<String,Object> resModifyProcess(JSONObject modifyProcessJson) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String userId = modifyProcessJson.getString("userId");
		String processIds = modifyProcessJson.getString("processIds");
		resultMap.put("procId",processIds);
		try {
			String ret = modifyProcessJson.getString("ret");
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				String processStr = jRedisDao.get(GlobalConstants.V2V_FUNC_REDIS_PREFIX + ProbeManagerMsgUtil.MODIFY_PROC + userId);
				PlatformProcess process = JSONObject.parseObject(processStr,PlatformProcess.class);
				processService.updatePlatformProcess(process);
				resultMap.put("errcode",0);
				resultMap.put("msg","修改监测进程成功");
				return resultMap;
			}
			resultMap.put("errcode",1);
			resultMap.put("msg","修改监测进程失败");
		} catch(Exception e) {
			resultMap.put("errcode",1);
			resultMap.put("msg","修改监测进程异常");
		}
		return resultMap;
	}

	/**
	 * 删除平台进程
	 * @param delProcessJson
	 * @return
	 */
	private Map<String,Object> resDelProcess(JSONObject delProcessJson) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		resultMap.put("func","del_proc");
		String processIds = delProcessJson.getString("processIds");
		resultMap.put("procId",processIds);
		try {
			String ret = delProcessJson.getString("ret");
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret)) {
				processService.deleteProcessByIds(processIds);
				resultMap.put("errcode",0);
				resultMap.put("msg","删除进程成功");
				return resultMap;
			}
			resultMap.put("errcode",1);
			resultMap.put("msg","删除进程失败");
		} catch(Exception e) {
			logger.error("删除进程异常",e);
			resultMap.put("errcode",1);
			resultMap.put("msg","删除进程异常");
		}
		return resultMap;
	}

	/**
	 * 进程处理结果
	 * @param msgObj
	 * @return
	 */
	private Map<String,Object> processHandle(JSONObject msgObj) {
		Map<String,Object> resultMap =  new HashMap<String,Object>();
		String ret = msgObj.getString("ret");
        String userId = msgObj.getString("userId");
		String registerid = msgObj.getString("registerid");
		String procIdStr = msgObj.getString("processId");
		if(StringUtil.isNotNull(registerid)) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PLATFORM_HANDLE
                    +"::"+registerid+"::"+userId);
        } else if(StringUtil.isNotNull(procIdStr)) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PROCESS_HANDLE
                    +":::"+procIdStr+":"+userId);
        }
		try {
			String method = msgObj.getString("method");
			String handleName = method.equals("start")? "启动" : method.equals("stop") ? "关闭" :method.equals("restart") ? "重启": "";
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret) && StringUtil.isNotNull(registerid)) {
				if("start".equals(method) || "restart".equals(method)) {
					processService.recoverProcessStatusByIds(procIdStr);
				}
				resultMap.put("errcode",0);
				resultMap.put("msg","平台"+handleName+"操作命令执行成功");
				resultMap.put("registerId",registerid);
				return resultMap;
			}
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(ret) && StringUtil.isNotNull(procIdStr)) {
				resultMap.put("errcode",0);
				resultMap.put("msg","进程"+handleName+"操作命令执行成功");
				resultMap.put("procId",Integer.parseInt(procIdStr));
				return resultMap;
			}
			resultMap.put("errcode",1);
			resultMap.put("msg","平台进程操作失败");
			if(StringUtil.isNotNull(registerid)) {
				resultMap.put("registerId",registerid);
			}else {
				resultMap.put("procId",Integer.parseInt(procIdStr));
			}
		} catch(Exception e) {
			resultMap.put("errcode",1);
			resultMap.put("msg","平台进程操作异常");
			if(StringUtil.isNotNull(registerid)) {
				resultMap.put("registerId",registerid);
			}else {
				resultMap.put("procId",Integer.parseInt(procIdStr));
			}
		}
		return resultMap;
	}

}
