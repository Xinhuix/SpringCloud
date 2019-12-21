/*package com.visionvera.netty;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.slweoms.AlarmInfo;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.bean.slweoms.ServerHardwareVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.ywcore.SlweomsDao;
import com.visionvera.service.AlarmService;
import com.visionvera.service.ServerBasicsService;
import com.visionvera.service.ServersHardwareService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.ProbeDisplayUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Sharable
public class NettyHeartBeatHandler extends ChannelInboundHandlerAdapter{
	
	private static Logger logger = LoggerFactory.getLogger(NettyHeartBeatHandler.class);
	
	private static final String connectFlag = "Netty Client Connected";
	
	private static final String HEART_BEAT_SUCCESS = "heart_beat_success";
	
	private static final String HEART_BEAT_PREFIX = "$start$"; //心跳开始标志
	
	private static final String HEART_BEAT_SUFFIX = "$end$";	//心跳结束标志
	
	private static final String HEART_BEAT = "heartBeat#";   //心跳标志
	
	private static final String HARDWARE = "hardWare#";		//硬件信息
	
	private static final String PROC_MSG = "proc#";   //进程信息

	private static final String CONF_CHECK = "confCheck#";   //配置检测
	
	@Autowired
	private JRedisDao jRedisDao;
	@Autowired
	private SlweomsDao slweomsDao;
	
	@Autowired
	private ServerBasicsService serverBasicsService;
	
	@Autowired
	private AlarmService alarmService;
	
	@Autowired
	private ServersHardwareService serversHardwareService;
	

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if( msg instanceof String) {
				String message = (String)msg;
				if(message.contains(HEART_BEAT)) {
					handleHeartBeat(ctx,message);
				}else if(message.contains(HARDWARE)) {
					logger.info("接收硬件信息："+message);
					handleHardware(ctx,message);
				}else if(message.contains(PROC_MSG)) {
					handleProcMsg(ctx,message);
				}else if(message.contains(CONF_CHECK)) {
					handleConfCheck(ctx,message);
				}
			}
		} catch(Exception e) {
			logger.error("心跳异常===",e);
		}
	}

	private void handleConfCheck(ChannelHandlerContext ctx, String message) {
		message = message.replace(HEART_BEAT_PREFIX+CONF_CHECK,"").replace(HEART_BEAT_SUFFIX,"");
		logger.info("接收到配置文件检测信息"+ message);
		alarmService.insertAlarmConf(message);
	}

	private void handleProcMsg(ChannelHandlerContext ctx, String message) {
		message = message.replace(HEART_BEAT_PREFIX+PROC_MSG, "")
				.replace(HEART_BEAT_SUFFIX, "");
		logger.info("接收到服务器进程信息"+message);
		alarmService.insertAlarmPlatform(message);
	}

	private void handleHardware(ChannelHandlerContext ctx, String message) throws Exception {
		//CommonConstant.NETTY_PREFIX + "hardWare#" + JSONObject.toJSONString(serversHardware) + CommonConstant.NETTY_SUFFIX
		try {
			message = message.replace(HEART_BEAT_PREFIX + HARDWARE, "")
					.replace(HEART_BEAT_SUFFIX, "");
			ServerHardwareVO serverHardwareVO = JSONObject.parseObject(message, ServerHardwareVO.class);
			if(serverHardwareVO==null){
				logger.warn("NettyHeartBeatHandler===handleHardware===ServerHardwareVO为空："+message);
				return;
			}
			ServerBasics serverBasics = serverBasicsService.getServerBasicsByServerUnique(serverHardwareVO.getServerUnique());
			if(serverBasics==null){
				logger.warn("NettyHeartBeatHandler===handleHardware===应用服务器不存在："+serverHardwareVO.getServerUnique());
				return;
			}
			if(ProbeDisplayUtil.PROBE_STATE_STOP.equals(serverBasics.getOpenState()) 
					|| ProbeDisplayUtil.PROBE_STATE_DELETE.equals(serverBasics.getState())){
				return;
			}
			serversHardwareService.insertServerHardwareVO(serverHardwareVO);
			alarmService.checkServerHardwareVO(serverHardwareVO);
		} catch (Exception e) {
			logger.error("NettyHeartBeatHandler===handleHardware===处理硬件信息异常：",e);
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("netty客户端出现异常", cause);
        if(cause instanceof java.io.IOException) {
            ctx.close();
            return;
        }
        super.exceptionCaught(ctx, cause);
        ctx.close();
	}
	
	*//**
	 * 处理心跳
	 * @param ctx
	 * @param msg
	 *//*
	private void handleHeartBeat(ChannelHandlerContext ctx, String message) {
		try {
			*//**
			 * 第一次连接时发送的信息
			 *//*
			if(message.contains(connectFlag)) {
				ctx.writeAndFlush(HEART_BEAT_SUCCESS);
				return;
			}
			
			*//**
			 * 心跳格式  HEART_BEAT_PREFIX + heartBeat# +服务器唯一标识 + HEART_BEAT_SUFFIX
			 *//*
			if(!message.contains(HEART_BEAT_PREFIX)) {
				return;
			}
			
			String serverUnique = message.replace(HEART_BEAT_PREFIX + "heartBeat#", "")
					.replace(HEART_BEAT_SUFFIX, "");
			
			handleMessage(serverUnique);
			ctx.writeAndFlush(HEART_BEAT_SUCCESS);
		} catch(Exception e) {
			logger.error("心跳处理异常", e);
		}
	}
	
	*//**
	 * 处理心跳
	 * @param serverUnique
	 *//*
	private void handleMessage(String serverUnique) {
		ServerBasics serverBasics = serverBasicsService.getServerBasicsByServerUnique(serverUnique);
		if(serverBasics == null || ProbeDisplayUtil.PROBE_STATE_STOP.equals(serverBasics.getOpenState()) 
				|| ProbeDisplayUtil.PROBE_STATE_DELETE.equals(serverBasics.getState())) {
			return;
		}
		
		Object serverOnlineObj = jRedisDao.getObject(GlobalConstants.SERVER_ONLINE_STATUS_PREFIX + serverUnique);
		if(serverOnlineObj != null) {
			Integer onLineStatus = (Integer)serverOnlineObj;
			if(GlobalConstants.SERVER_OFFLINE_STATE.equals(onLineStatus)) {
				//设置服务器为在线状态
				serverBasics.setServerOnLine(GlobalConstants.SERVER_ONLINE_STATE);
				serverBasics.setOnLineStartTime(new Date());
				serverBasicsService.updateServerOnLine(serverBasics);
				
				//redis设置服务器为在线状态
				jRedisDao.setObject(GlobalConstants.SERVER_ONLINE_STATUS_PREFIX + serverUnique, 
						GlobalConstants.SERVER_ONLINE_STATE, 604800); //604800=7*24*60*60
				generateRecoverAlarm(serverBasics);
				
			}
		}else {
			if(GlobalConstants.SERVER_OFFLINE_STATE.equals(serverBasics.getServerOnLine()) || serverBasics.getServerOnLine() == null) {
				serverBasics.setServerOnLine(GlobalConstants.SERVER_ONLINE_STATE);
				serverBasics.setOnLineStartTime(new Date());
				serverBasicsService.updateServerOnLine(serverBasics);
				
				generateRecoverAlarm(serverBasics);
			}
			//redis设置服务器为在线状态
			jRedisDao.setObject(GlobalConstants.SERVER_ONLINE_STATUS_PREFIX + serverUnique, 
					GlobalConstants.SERVER_ONLINE_STATE, 604800); //604800=7*24*60*60
		}
		
		Date currentDate = new Date();
		jRedisDao.setObject(GlobalConstants.REDIS_HEARTBEAT_PREFIX+serverUnique, currentDate.getTime(), 604800);//604800=7*24*60*60
		logger.info("设置"+serverUnique+"心跳接收时间"+DateUtil.date2String(currentDate));
	}
	
	private void generateRecoverAlarm(ServerBasics serversBasics) {
		String serverProvince = serversBasics.getServerProvince();
		RegionVO regionb = slweomsDao.getRegionVOById(serverProvince);
		String provinceRegionbName = regionb.getName();
		
    	String note = serversBasics.getServerName() + "离线已恢复,"+provinceRegionbName +","+serversBasics.getServerManageIp();
    	AlarmInfo alarm = new AlarmInfo();
    	alarm.setTimestamp(System.currentTimeMillis());
        alarm.setStatus("OK");
    	alarm.setRegisterid(serversBasics.getServerUnique());
    	alarm.setNote(note);
    	alarm.setMetric(serversBasics.getServerName());
    	alarm.setKindType(1);
    	alarm.setEndpoint(serversBasics.getServerHostname());
    	alarm.setAlarmType(6);
    	alarm.setAlarmLevel(1);
    	alarm.setTreatmentStates(0);
    	alarm.setState(0);
    	alarm.setCreateTime(new Date());
    	
        logger.info(note.toString());
        alarmService.insertAlarm(alarm);
	}

}
*/