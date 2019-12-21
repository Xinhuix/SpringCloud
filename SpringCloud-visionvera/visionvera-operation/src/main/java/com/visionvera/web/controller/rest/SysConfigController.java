package com.visionvera.web.controller.rest;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.BandWidthVO;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DeviceLogVO;
import com.visionvera.bean.cms.LogKeyValueVO;
import com.visionvera.bean.cms.ServerTypeVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.cms.SmsVO;
import com.visionvera.common.api.operation.SysConfigAPI;
import com.visionvera.constrant.LogType;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.SysConfigService;
import com.visionvera.util.LogWritter;

/**
 * 系统配置Controller
 *
 */
@RestController
public class SysConfigController extends BaseReturn implements SysConfigAPI {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SysConfigService sysConfigService;
	
	/**
	 * 修改告警阈值
	 * @param meetCount 频繁开会次数阈值
	 * @param voiceCount 音频丢包率阈值
	 * @param viewCount 视频丢包率阈值
	 * @return
	 */
	@RequestMapping(value = "/edit/alarm/threshold", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData editAlarmThreshold(@RequestParam("meetCount")String meetCount,
			@RequestParam("voiceCount") String voiceCount, @RequestParam("viewCount") String viewCount) {
		try {
			ConstDataVO alarmMeetConstData = new ConstDataVO();//频繁开会
			alarmMeetConstData.setValue(meetCount);
			alarmMeetConstData.setConstId("alarm_meet_count");
			
			ConstDataVO alarmVoiceConstData = new ConstDataVO();//音频丢包
			alarmVoiceConstData.setValue(voiceCount);
			alarmVoiceConstData.setConstId("alarm_voice_count");
			
			
			ConstDataVO alarmViewConstData = new ConstDataVO();//视频丢包
			alarmViewConstData.setValue(viewCount);
			alarmViewConstData.setConstId("alarm_view_count");
			List<ConstDataVO> list = new ArrayList<ConstDataVO>();
			list.add(alarmMeetConstData);
			list.add(alarmVoiceConstData);
			list.add(alarmViewConstData);
			this.sysConfigService.updateAprTime(list);
			
		} catch (Exception e) {
			this.LOGGER.error("SysConfigController ===== editAlarmThreshold ===== 更新报警阈值失败 =====> ", e);
			return super.returnError("更新报警阈值失败");
		}
		
		return super.returnResult(0, "更新报警阈值成功");
	}
	
	/** <pre>getAlarmMeetCount(获取告警阈值)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月16日 下午2:37:48    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月16日 下午2:37:48    
	 * 修改备注： 
	 * @return</pre>    
	 */
	public ReturnData getAlarmMeetCount() {
		List<Integer> list = new ArrayList<Integer>();
		try {
			list.add(6);
			list.add(7);
			list.add(8);
			List<ConstDataVO> constDataList = this.sysConfigService.getShold(list);
			
			return super.returnResult(0, "获取成功", null, constDataList);
		} catch (Exception e) {
			this.LOGGER.error("SysConfigController ===== getAlarmMeetCount ===== 获取报警阈值失败 =====> ", e);
			return super.returnError("获取报警阈值失败");
		}
	}
	
	/**
	 * 根据类型获取服务器配置信息
	 * @param serverType 类型.1会管中心服务 2网管服务 3调度服务 4消息转发服务器
	 * @return
	 */
	public ReturnData getServer(@PathVariable("serverType") String serverType) {
		try {
			ServerVO server = this.sysConfigService.getServerByType(serverType);
			return super.returnResult(0, "获取服务器配置信息成功", null, null, server);
		} catch (Exception e) {
			this.LOGGER.error("SysConfigController ===== getServer ===== 获取服务器配置信息失败 =====> ", e);
			return super.returnError("获取服务器配置信息失败");
		}
	}
	
	
	/** <pre>updateApr(更新审批流程配置数据)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年7月24日 下午2:21:39    
	 * 修改人：周逸芳        
	 * 修改时间：2017年7月24日 下午2:21:39    
	 * 修改备注： 
	 * @param appOne
	 * @param appTwo
	 * @param appThree
	 * @return</pre>    
	 */
	@RequestMapping("updateApr")
	
	public Map<String, Object> updateApr(String appOne,String appTwo,String appThree,
			String constIdOne,String constIdTwo,String constIdThree,
			String value,String excellentTime, String intelligent){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			resultMap.put("result", false);
			resultMap.put("errmsg", "修改失败：数据不全");
			List<ConstDataVO> list = new ArrayList<ConstDataVO>();
			ConstDataVO dataVO = null;
			if (StringUtils.isNotBlank(appOne) && StringUtils.isNotBlank(appTwo)&& 
					StringUtils.isNotBlank(appThree)) {
				//为审批人赋valueType属性值
				String [] appOne1 = appOne.split(",");
				String [] appTwo2 = appTwo.split(",");
				String [] appThree3 = appThree.split(",");
				String [] constIdOne1 = constIdOne.split(",");
				String [] constIdTwo2 = constIdTwo.split(",");
				String [] constIdThree3 = constIdThree.split(",");
				for (int i = 0; i < appOne1.length; i++) {
					dataVO = new ConstDataVO();
					dataVO.setDisplay(appOne1[i]);
					dataVO.setValueType(1);
					dataVO.setConstId(constIdOne1[i]);
					list.add(dataVO);
				}
				for (int i = 0; i < appTwo2.length; i++) {
					dataVO = new ConstDataVO();
					dataVO.setDisplay(appTwo2[i]);
					dataVO.setValueType(2);
					dataVO.setConstId(constIdTwo2[i]);
					list.add(dataVO);
				}
				for (int i = 0; i < appThree3.length; i++) {
					dataVO = new ConstDataVO();
					dataVO.setDisplay(appThree3[i]);
					dataVO.setValueType(3);
					dataVO.setConstId(constIdThree3[i]);
					list.add(dataVO);
				}
				sysConfigService.updateApr(list);
				List<ConstDataVO> listApr = new ArrayList<ConstDataVO>();
				ConstDataVO datavo = new ConstDataVO();
				datavo.setConstId("approve_time_out");
				datavo.setValue(value);
				listApr.add(datavo);
				ConstDataVO datavo1 = new ConstDataVO();
				datavo1.setConstId("approve_time_excellent");
				datavo1.setValue(excellentTime);
				listApr.add(datavo1);
				ConstDataVO datavo2 = new ConstDataVO();
				datavo2.setConstId("intelligent_approval");
				datavo2.setValue(intelligent);
				listApr.add(datavo2);
				sysConfigService.updateAprTime(listApr);
				resultMap.put("result", true);
				resultMap.put("errmsg", "修改成功");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "修改失败：内部异常");
			LOGGER.error("修改审批人配置失败", e);
		}
		return resultMap;
	}
	
	
	/** <pre>getApproval(获取各节点审批人信息)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年7月21日 上午10:14:11    
	 * 修改人：周逸芳        
	 * 修改时间：2017年7月21日 上午10:14:11    
	 * 修改备注： 
	 * @return</pre>    
	 */
	@RequestMapping("getApproval")
	public ReturnData getApproval(){
		try {
			List<ConstDataVO> list = sysConfigService.getApproval();
			return super.returnResult(0, "获取成功", null, list);
		} catch (Exception e) {
			return super.returnError("获取报警阈值失败");
		}
	}
	
	/**
	 * 
	 * @Title: sms 
	 * @Description: TODO 短信配置
	 * @param @return  参数说明  
	 * @return Map<String,Object>    返回类型 
	 * @throws
	 */
	@RequestMapping("sms")
	public Map<String, Object> statistics(){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			SmsVO sms = sysConfigService.getSms();
			resultMap.put("data", sms);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			LOGGER.error("获取短信配置数据失败, msg: ", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: updateSms 
	 * @Description: TODO 更新短信配置
	 * @param @return  参数说明  
	 * @return Map<String,Object>    返回类型 
	 * @throws
	 */
	@RequestMapping("updateSms")
	public Map<String, Object> updateSms(@RequestBody SmsVO sms,
			HttpSession session){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			if (null != sms) {
				if (StringUtils.isNotBlank(sms.getName())) {
					sms.setName(URLDecoder.decode(sms.getName(), "utf-8"));
				}

				if (StringUtils.isNotBlank(sms.getLoginName())) {
					sms.setLoginName(URLDecoder.decode(sms.getLoginName(),
							"utf-8"));
				}
				if (StringUtils.isNotBlank(sms.getOwner())) {
					sms.setOwner(URLDecoder.decode(
							sms.getOwner(), "utf-8"));
				}
				if (StringUtils.isNotBlank(sms.getUrl())) {
					sms.setUrl(URLDecoder.decode(
							sms.getUrl(), "utf-8"));
				}
				int result = sysConfigService.updateSms(sms);
				if (result > 0) {
					resultMap.put("result", true);
				} else {
					resultMap.put("result", false);
				}
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			LOGGER.error("更新短信配置失败, msg: ", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: server 
	 * @Description: TODO 服务配置
	 * @param @return  参数说明  
	 * @return Map<String,Object>    返回类型 
	 * @throws
	 */
	@RequestMapping("server")
	
	public Map<String, Object> server(Integer type){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		Map<String, Object> paramsMap = new HashMap<String,Object>();
		try {
			paramsMap.put("type", type);
			ServerVO server = sysConfigService.getServer(paramsMap);
			resultMap.put("data", server);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			LOGGER.error("获取服务配置数据失败, msg: ",e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: updateServer 
	 * @Description: TODO 更新服务配置
	 * @param @return  参数说明  
	 * @return Map<String,Object>    返回类型 
	 * @throws
	 */
	@RequestMapping("updateServer")
	
	public Map<String, Object> updateService(@ModelAttribute ServerVO server,
			HttpSession session){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			if (null != server) {
				if (StringUtils.isNotBlank(server.getName())) {
					server.setName(URLDecoder.decode(server.getName(), "utf-8"));
				}
				int result = sysConfigService.updateServer(server);
				if (result > 0) {
					resultMap.put("result", true);
				} else {
					resultMap.put("result", false);
				}
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			LOGGER.error("更新服务配置失败, msg: ", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.EDIT_SYS_CONFIG, session, "修改服务配置："+server.getName(), "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.EDIT_SYS_CONFIG, session, "修改服务配置："+server.getName(), "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: getServerType 
	 * @Description: 获取服务器类型列表
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	@RequestMapping("getServerType")
	
	public Map<String, Object> getServerType(){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			List<ServerTypeVO> list = sysConfigService.getServerType();
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			LOGGER.error("获取服务器类型列表失败, msg: ", e);
		}
		return resultMap;
	}
	
	
	/** <pre>getConfigUpdatePerson(获取可上传文件升级配置人员)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月25日 下午5:34:04    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月25日 下午5:34:04    
	 * 修改备注： 
	 * @return</pre>    
	 */
	@RequestMapping("getConfigUpdatePerson")
	
	public Map<String, Object> getConfigUpdate(){
		Map<String, Object> paramsMap = new HashMap<String,Object>();
		try {
			List<ConstDataVO> list = sysConfigService.getConfigUpdate();
			paramsMap.put("result", true);
			paramsMap.put("list", list);
		} catch (Exception e) {
			paramsMap.put("result", false);
			LOGGER.error("获取可上传文件升级配置人员失败：", e);
		}
		return paramsMap;
	}
	
	
	/** <pre>updateConfigPerson(修改升级管理可操作人员)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月25日 下午5:41:55    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月25日 下午5:41:55    
	 * 修改备注： 
	 * @return</pre>    
	 */
	@RequestMapping("updateConfigPerson")
	
	public Map<String, Object> updateConfigPerson(String configOne,String constId){
		Map<String, Object> paramsMap = new HashMap<String,Object>();
		try {
			List<ConstDataVO> list = new ArrayList<ConstDataVO>();
			ConstDataVO dataVO = null;
			if (StringUtils.isNotBlank(configOne)) {
				//为审批人赋valueType属性值
				String [] configOne1 = configOne.split(",");
				String [] constId1 = constId.split(",");
				for (int i = 0; i < configOne1.length; i++) {
					dataVO = new ConstDataVO();
					dataVO.setDisplay(configOne1[i]);
					dataVO.setValueType(5);
					dataVO.setConstId(constId1[i]);
					list.add(dataVO);
				}
				sysConfigService.updateConfigPerson(list);
				paramsMap.put("result", true);
				paramsMap.put("errmsg", "修改成功");
			}
		} catch (Exception e) {
			paramsMap.put("result", false);
			LOGGER.error("修改可上传文件升级配置人员失败：", e);
		}
		return paramsMap;
	}
	
	/** <pre>getAprTime(获取当前审批超时时间)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年11月15日 上午11:12:22    
	 * 修改人：周逸芳        
	 * 修改时间：2017年11月15日 上午11:12:22    
	 * 修改备注： 
	 * @return</pre>    
	 */
	@RequestMapping("getAprTime")
	
	public Map<String, Object> getAprTime(){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			ConstDataVO dataVO = new ConstDataVO();
			dataVO.setValueType(4);
			List<ConstDataVO> list= sysConfigService.getAprTime(dataVO);
			for (ConstDataVO constDataVO : list) {
				if (constDataVO.getConstId().equals("approve_time_out")) {
					resultMap.put("aprTime", constDataVO.getValue());
				}else if (constDataVO.getConstId().equals("approve_time_excellent")) {
					resultMap.put("aprTimeExcellent", constDataVO.getValue());
				}
			}
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			LOGGER.error("获取基础服务状态失败, msg: ",e);
		}
		return resultMap;
	}

	/** <pre>getServer(视联汇获取权限ip端口号)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月23日 上午11:58:20    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月23日 上午11:58:20    
	 * 修改备注： 
	 * @param type
	 * @return</pre>    
	 */
	@RequestMapping(value = "/getPerServer", method = RequestMethod.GET)
	public ReturnData getPerServer() {
		List<ServerVO> serverList = new ArrayList<>();
		try {
			List<ServerVO> perServerList = this.sysConfigService.getPerServer();

		/*	Stream<ServerVO> stuStream = perServerList.stream()
					.filter((i) -> i.getType() == 6 || i.getType()==7 ||
							i.getType()== 8 || i.getType()==9999);
			serverList = stuStream.collect(Collectors.toCollection(ArrayList::new));
			*/

			for (ServerVO serverVO : perServerList) {
				if (serverVO.getType() == 6 || serverVO.getType() == 7 ||
					serverVO.getType() == 8 ||serverVO.getType() == 9 || serverVO.getType() == 9999) {
					serverList.add(serverVO);
				}
			}
			return super.returnResult(0, "获取成功", null, serverList);
		} catch (Exception e) {
			this.LOGGER.error("SysConfigController ===== getPerServer ===== 获取视联会IP和端口失败 =====> ", e);
			return super.returnError("获取失败");
		}
	}
	
	/**
	 * 获取各省份的带宽信息
	 * @param bandWidth 查询条件
	 * @return
	 */
	@RequestMapping(value = "/getRegionBandwidth", method = RequestMethod.GET)
	public ReturnData getRegionBandWidth(@RequestBody(required = false) BandWidthVO bandWidth) {
		try {
			List<BandWidthVO> bandWidthList = this.sysConfigService.getBandwidth(bandWidth);
			return super.returnResult(0, "获取成功", null, bandWidthList);
		} catch (BusinessException e) {
			this.LOGGER.error("SysConfigController ===== getRegionBandwidth ===== 获取国干带宽失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("SysConfigController ===== getRegionBandwidth ===== 获取国干带宽失败 =====> ", e);
			return super.returnError("获取国干带宽失败");
		}
	}
	
	/**
	 * 更新国干带宽
	 * @param bandWidthList
	 * @return
	 */
	@RequestMapping(value = "/editRegionBandwidth", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData editRegionBandwidth(@RequestBody(required = true) List<BandWidthVO> bandWidthList) {
		try {
			return this.sysConfigService.updateBandwidthBatch(bandWidthList);
		} catch (BusinessException e) {
			this.LOGGER.error("SysConfigController ===== editRegionBandwidth ===== 更新国干带宽失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("SysConfigController ===== editRegionBandwidth ===== 更新国干带宽失败 =====> ", e);
			return super.returnError("更新国干带宽失败");
		}
	}
	
	/**
	 * 获取常量信息
	 * @param type 类型。11表示智能审批的开关
	 * @return
	 */
	@RequestMapping(value = "/type/{type}/getConstData")
	public ReturnData getConstData(@PathVariable(name = "type") String type) {
		Map<String, Object> extraMap = new HashMap<String, Object>();
		try {
			String constDataValue = this.sysConfigService.getConstDataByType(type);
			extraMap.put("constDataValue", constDataValue);
			return super.returnResult(0, "获取成功", null, null, extraMap);
		} catch (BusinessException e) {
			this.LOGGER.error("SysConfigController ===== getConstData ===== 获取配置失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("SysConfigController ===== getConstData ===== 获取配置失败 =====> ", e);
			return super.returnError("获取配置失败");
		}
	}
	
	/**
	 * 
	 * TODO 获取终端日志
	 * @author 周逸芳
	 * @date 2019年3月5日  
	 * @version 1.0.0 
	 * @param pageNum
	 * @param pageSize
	 * @param name
	 * @return
	 */
	@RequestMapping("getDevLog")
	public Map<String, Object> getDevLog(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String uuid,String devno,String type) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("devno", devno);
			paramsMap.put("type", type);
			paramsMap.put("uuid", uuid);
			//获取终端日志列表
			List<DeviceLogVO> list = sysConfigService.getDevLog(paramsMap);
			//遍历集合将日志大小进行转换
			for (DeviceLogVO deviceLogVO : list) {
				DecimalFormat df=new DecimalFormat("0.00");
				if (deviceLogVO.getFiletype() == 1) {//日志文件
					if (deviceLogVO.getFilesize() != null && deviceLogVO.getFilesize() != "") {
						String filesize = df.format(Double.valueOf(deviceLogVO.getFilesize())/1024/1024);
						deviceLogVO.setFilesize(filesize);
					}
					if (deviceLogVO.getDownloadsize() != null && deviceLogVO.getDownloadsize() != "") {
						String dlFilesize = df.format(Double.valueOf(deviceLogVO.getDownloadsize())/1024/1024);
						deviceLogVO.setDownloadsize(dlFilesize);
					}
				}else if (deviceLogVO.getFiletype() == 2) {//配置文件
					if (deviceLogVO.getFilesize() != null && deviceLogVO.getFilesize() != "") {
						String filesize = df.format(Double.valueOf(deviceLogVO.getFilesize())/1024);
						deviceLogVO.setFilesize(filesize);
					}
					if (deviceLogVO.getDownloadsize() != null && deviceLogVO.getDownloadsize() != "") {
						String dlFilesize = df.format(Double.valueOf(deviceLogVO.getDownloadsize())/1024);
						deviceLogVO.setDownloadsize(dlFilesize);
					}
				}
				
			}
			int total = sysConfigService.getDevLogCount(paramsMap);
			total = total % pageSize == 0 ? total / pageSize
					: (total / pageSize) + 1;
			resultMap.put("pageNum", pageNum);
			resultMap.put("pageTotal", total);
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
		}
		return resultMap;
	}
	
	
	/**
	 * 获取终端日志解析参数
	 * @author 谢程算
	 * @date 2019年6月3日  
	 * @version 1.0.0 
	 * @return
	 */
	@RequestMapping("deviceLogKeyList")
	public Map<String,Object> deviceLogKeyList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String uuid,Integer deviceType,String logKey,String logValue){
		Map<String, Object> paramsMap = new HashMap<String,Object>();
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("type", deviceType);//log.getDeviceType()
			paramsMap.put("logKey", logKey);//log.getLogKey()
			paramsMap.put("logValue", logValue);//log.getLogValue()
			paramsMap.put("uuid", uuid);
			List<LogKeyValueVO> list = sysConfigService.getDeviceLogKeyList(paramsMap);
			int total= sysConfigService.getDeviceLogKeyListCount(paramsMap);
			if(pageSize != -1){
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;	
			}
			resultMap.put("pageNum", pageNum);
			resultMap.put("pageTotal", total);
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取终端日志失败");
		}
		return resultMap;
	}
	
	
	/**
	 * 获取终端日志解析参数
	 * @author 周逸芳
	 * @date 2019年6月3日  
	 * @version 2.26.0 
	 * @return
	 */
	@RequestMapping(value = "/addLogKey", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String,Object> addLogKey(@RequestBody(required = true)LogKeyValueVO log){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			if (log.getDeviceType() == 0) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "终端类型不可为空");
				return resultMap;
			}
			if (StringUtils.isBlank(log.getLogKey())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "终端分析原始值不可为空");
				return resultMap;
			}
			if (StringUtils.isBlank(log.getLogValue())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "翻译不可为空");
				return resultMap;
			}
			//判断是否有相同key值
			int count = sysConfigService.checkKey(log);
			if (count > 0) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "该日志分析参数已存在");
				return resultMap;
			}
			int i = sysConfigService.addLogKey(log);
			if (i > 0 ) {
				resultMap.put("result", true);
				resultMap.put("errmsg", "添加成功");
			}else{
				resultMap.put("result", false);
				resultMap.put("errmsg", "添加失败");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "添加失败");
		}
		return resultMap;
	}
	
	/**
	 * 修改终端日志解析参数
	 * @author 周逸芳
	 * @date 2019年6月3日  
	 * @version 2.26.0 
	 * @return
	 */
	@RequestMapping(value = "/updateLogKey", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String,Object> updateLogKey(@RequestBody(required = true)LogKeyValueVO log){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			if (StringUtils.isBlank(log.getUuid())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "uuid不可为空");
				return resultMap;
			}
			if (log.getDeviceType() == 0) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "终端类型不可为空");
				return resultMap;
			}
			if (StringUtils.isBlank(log.getLogKey())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "终端分析原始值不可为空");
				return resultMap;
			}
			if (StringUtils.isBlank(log.getLogValue())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "翻译不可为空");
				return resultMap;
			}
			int i = sysConfigService.updateLogKey(log);
			if (i > 0 ) {
				resultMap.put("result", true);
				resultMap.put("errmsg", "更新成功");
			}else{
				resultMap.put("result", false);
				resultMap.put("errmsg", "更新失败");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "更新失败");
		}
		return resultMap;
	}
	
	/**
	 * 删除终端日志解析参数
	 * @author 周逸芳
	 * @date 2019年6月3日  
	 * @version 2.26.0 
	 * @return
	 */
	@RequestMapping(value = "/deleteLogKey", method = RequestMethod.GET)
	public Map<String,Object> deleteLogKey(@RequestParam("uuid") String uuid){
		Map<String, Object> paramMap = new HashMap<String,Object>();
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			if (StringUtils.isBlank(uuid)) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "uuid不可为空");
				return resultMap;
			}
			paramMap.put("uuid", uuid);
			int i = sysConfigService.deleteLogKey(paramMap);
			if (i > 0 ) {
				resultMap.put("result", true);
				resultMap.put("errmsg", "删除成功");
			}else{
				resultMap.put("result", false);
				resultMap.put("errmsg", "删除失败");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "删除失败");
		}
		return resultMap;
	}

	@RequestMapping("updateSes")
	public Map<String, Object> updateSes(String sessionTime){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			if (sessionTime != null && sessionTime != "") {
				ConstDataVO dataVO = new ConstDataVO();
				dataVO.setDisplay("session失效时间");
				dataVO.setValue(sessionTime);
				dataVO.setConstId(sessionTime);
//				sysConfigService.deleteSes(dataVO);
				sysConfigService.updateSes(dataVO);
				resultMap.put("result", true);
			}
		} catch (Exception e) {
			resultMap.put("result", false);
		}
		return resultMap;
	}
	
}
