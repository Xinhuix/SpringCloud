package com.visionvera.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visionvera.bean.cms.LogVO;
import com.visionvera.bean.cms.MeetRecordVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.restful.DataInfo;
import com.visionvera.bean.restful.ResponseInfo;
import com.visionvera.constrant.VSMeet64;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.operation.LogDao;
import com.visionvera.dao.operation.MeetDao;
import com.visionvera.dao.operation.ScheduleDao;
import com.visionvera.service.MeetService;
import com.visionvera.service.SysConfigService;
import com.visionvera.util.SocketClient;
import com.visionvera.util.TimeUtil;
import com.visionvera.util.Util;
import com.visionvera.util.VSMeet;

/**
 * 
 * @ClassName: MeetServiceImpl
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xiechs
 * @date 2016年11月1日
 * 
 */
@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class MeetServiceImpl implements MeetService {
	private final Logger logger = LogManager.getLogger(this.getClass());
	@Resource
	private MeetDao meetDao;
	@Resource
	private ScheduleDao scheduleDao;
	@Resource
	private LogDao logDao;
	@Resource
	private SysConfigService sysConfigService;

	/**
	 * 
	 * @Title: getMeetList 
	 * @Description: TODO 获取会议列表 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleBean>    返回类型 
	 * @throws
	 */
	public List<ScheduleVO> getMeetList(Map<String, Object> map) {
		return meetDao.getMeetList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/***
	 * 
	 * @Description: 获取会议列表总条数
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 */
	public int getMeetListCount(Map<String, Object> map) {
		return meetDao.getMeetListCount(map);
	}
	
	/**
	 * 
	 * @Title: getLogList
	 * @Description: 根据会议ID查询会议操作日志（不分页）
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	public List<LogVO> getLogList(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return logDao.getLogList(map, new RowBounds());
		}
		return logDao.getLogList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: getRecordList
	 * @Description: 根据会议ID查询会议纪要列表
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	public List<MeetRecordVO> getRecordList(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return logDao.getRecordList(map, new RowBounds());
		}
		return logDao.getRecordList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	/***
	 * 
	 * @Description: 添加会议记录
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 */
	public int addMeeting(ScheduleVO schedule) {
		return meetDao.addMeeting(schedule);
	}

	/***
	 * 
	 * @Description: 更新会议记录
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 */
	public int updateMeeting(ScheduleVO schedule) {
		int i = 0;
		try{
			//scheduleDao.updateScheduleStatus(schedule);
			meetDao.updateMeeting(schedule);
		} catch (Exception e) {
			logger.error("Webservice可视电话更新会议状态失败", e);
			throw new RuntimeException("运行时出错！");// 为了使事务回滚
		}
		return i;
	}

	/**
	 * 
	 * @Title: stopMeeting
	 * @Description: TODO 根据会议ID停止会议
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> stopMeeting(Map<String, Object> paramsMap) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", false);
		result.put("msg","停止会议失败");
		try {
			
			//wqb 为了兼容16位(16位和64位合并)
			String systemType = Util.getSysProp("cmsweb.systemType");
			if (systemType.equals("64")){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("scheduleId", paramsMap.get("scheduleId"));
				// 具体的停会请求发送给会控中心
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> jsonRoot = new HashMap<String, Object>();
				Map<String, Object> jsonContent = new HashMap<String, Object>();
				jsonRoot.put("cmd", VSMeet64.VSM_STOPMEETING);
				jsonRoot.put("userId", paramsMap.get("userId"));
				jsonRoot.put("uuid", paramsMap.get("scheduleId"));
				jsonRoot.put("content", jsonContent);
				String json = objectMapper.writeValueAsString(jsonRoot);
				// 获取调度服务配置信息（会管服务器的vc_server表中查找）
				paramsMap.put("type", 1);
				ServerVO ser = sysConfigService.getServer(paramsMap);
				if (null == ser || StringUtils.isBlank(ser.getIp())
						|| StringUtils.isBlank(ser.getPort())
						|| Integer.parseInt(ser.getPort()) == 0) {
					result.put("msg","停会失败：未找到调度服务，请手动退出当前会议");
					return result;
				}
				logger.info("web页面停会|param="+json) ;
				// 连接调度服务器
				SocketClient client = new SocketClient(ser.getIp(),
						Integer.parseInt(ser.getPort()));
				if(client.start() == -1){
					result.put("msg","停会失败：无法连接调度服务器");
					return result;
				}
				client.sendData(VSMeet64.VSM_STOPMEETING, json.getBytes("utf-8"));
				map = client.recvData(JSONObject.class);
				client.close();
				logger.info("SDK调度返回的消息:|cmd="+VSMeet64.VSM_STOPMEETING+"|desc=web页面停会|SDKresult="+map);
				if (null == map) {
					result.put("msg","停会失败：调度服务器无响应，请手动退出当前会议");
					return result;
				}
				// 处理返回数据
				if (null == map.get("cmd")) {
					result.put("msg","停会失败：调度服务器返回消息为空，请手动退出当前会议");
					return result;
				}
				if (Integer.parseInt(map.get("cmd").toString()) != VSMeet64.VSM_STOPMEETING) {
					result.put("msg","停会失败：调度服务器返回消息异常，请手动退出当前会议");
					return result;
				}
				if (Integer.parseInt(map.get("result").toString()) != 0) {
					result.put("msg","停会失败:" + map.get("description").toString());
					return result;
				}
				
				Integer resultCode = Integer.parseInt(map.get("result").toString()) ;
				if (resultCode != 0) {
					String errorDesc = Util.getErrDescProp(String.valueOf(resultCode)) ;
					if(StringUtils.isBlank(errorDesc)){
						errorDesc = "未知错误码 "+map.get("result").toString() ;
						logger.error("调度返回的错误码没有查询到对应的错误描述，code="+map.get("result").toString()) ;
					}
					result.put("msg","停会失败:" + errorDesc);
					return result;
				}
			}else if(systemType.equals("16")){
				// 具体的停会请求发送给会控中心
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> jsonRoot = new HashMap<String, Object>();
				Map<String, Object> jsonContent = new HashMap<String, Object>();
				jsonRoot.put("cmd", VSMeet.VSM_STOPMEETING);
				jsonRoot.put("userId", paramsMap.get("userId"));
				jsonContent.put("meetingId", paramsMap.get("scheduleId"));
				jsonRoot.put("content", jsonContent);
				String json = objectMapper.writeValueAsString(jsonRoot);
				// 获取调度服务配置信息（会管服务器的vc_server表中查找）
				paramsMap.put("type", 1);
				ServerVO ser = sysConfigService.getServer(paramsMap);
				if (null == ser || StringUtils.isBlank(ser.getIp())
						|| StringUtils.isBlank(ser.getPort())
						|| Integer.parseInt(ser.getPort()) == 0) {
					result.put("msg","停止会议失败：未找到调度服务，请手动退出当前会议");
					return result;
				}
				// 连接调度服务器
				SocketClient client = new SocketClient(ser.getIp(),
						Integer.parseInt(ser.getPort()));
				if(client.start() == -1){
					result.put("msg","停止会议失败：无法连接调度服务器");
					return result;
				}
				client.sendData(VSMeet.VSM_STOPMEETING, json.getBytes("utf-8"));
				Map<String, Object> map = client.recvData(JSONObject.class);
				client.close();
				if (null == map) {
					result.put("msg","停止会议失败：调度务器无响应，请手动退出当前会议");
					return result;
				}
				// 处理返回数据
				if (null == map.get("cmd")) {
					result.put("msg","停止会议失败：调度服务器返回消息为空，请手动退出当前会议");
					return result;
				}
				if (Integer.parseInt(map.get("cmd").toString()) != VSMeet.VSM_STOPMEETING + 1) {
					result.put("msg","停止会议失败：调度服务器返回消息异常，请手动退出当前会议");
					return result;
				}
				if (Integer.parseInt(map.get("result").toString()) != 0) {
					result.put("msg","停止会议失败:" + map.get("description").toString());
					return result;
				}
			}
			
			result.put("result", true);
			result.put("msg","停止会议成功");
		} catch (Exception e) {
			logger.error("通过会管停止会议失败：", e);
			result.put("msg","停止会议失败：系统内部异常");
		}
		return result;
	}
	
	/***
	 * 获取地区ID下拉
	 */
	public List<RegionVO> getConferenceSelectionArea() {
		return meetDao.getConferenceSelectionArea();
	}

	/* **
	 * 终端入会    
	 */
	/*public Map<String, Object> devAttMeeting(Map<String, Object> paramsMap) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", false);
		result.put("msg","终端入会失败");
		try {
			// 具体的停会请求发送给会控中心
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> jsonRoot = new HashMap<String, Object>();
			Map<String, Object> jsonContent = new HashMap<String, Object>();
			jsonRoot.put("cmd", VSMeet.VSM_DEVICEMEETING);
			jsonRoot.put("userId", paramsMap.get("userId"));
			jsonContent.put("meetingId", paramsMap.get("scheduleId"));
			jsonRoot.put("content", jsonContent);
			String json = objectMapper.writeValueAsString(jsonRoot);
			// 获取调度服务配置信息（会管服务器的vc_server表中查找）
			paramsMap.put("type", 1);
			ServerVO ser = sysConfigService.getServer(paramsMap);
			if (null == ser || StringUtils.isBlank(ser.getIp())
					|| StringUtils.isBlank(ser.getPort())
					|| Integer.parseInt(ser.getPort()) == 0) {
				result.put("msg","终端入会失败：未找到调度服务");
				return result;
			}
			// 连接调度服务器
			SocketClient client = new SocketClient(ser.getIp(),
					Integer.parseInt(ser.getPort()));
			if(client.start() == -1){
				result.put("msg","终端入会失败：无法连接调度服务器");
				return result;
			}
			client.sendData(VSMeet.VSM_DEVICEMEETING, json.getBytes("utf-8"));
			Map<String, Object> map = client.recvData(JSONObject.class);
			client.close();
			if (null == map) {
				result.put("msg","终端入会失败：调度务器无响应");
				return result;
			}
			// 处理返回数据
			if (null == map.get("cmd")) {
				result.put("msg","终端入会失败：调度服务器返回消息为空");
				return result;
			}
			if (Integer.parseInt(map.get("cmd").toString()) != VSMeet.VSM_STOPMEETING + 1) {
				result.put("msg","终端入会失败：调度服务器返回消息异常");
				return result;
			}
			if (Integer.parseInt(map.get("result").toString()) != 0) {
				result.put("msg","终端入会失败:" + map.get("description").toString());
				return result;
			}
			result.put("result", true);
			result.put("msg","终端入会成功");
		} catch (Exception e) {
			logger.error("通过会管终端入会失败：", e);
			result.put("msg","终端入会失败：系统内部异常");
		}
		return result;
	}*/

	/** <pre>devDelMeeting(终端退会)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月4日 下午2:40:55    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月4日 下午2:40:55    
	 * 修改备注： 
	 * @param paramsMap


	 * @return</pre>    
	 */
	/*public Map<String, Object> devDelMeeting(Map<String, Object> paramsMap) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", false);
		result.put("msg","终端退会失败");
		try {
			// 具体的停会请求发送给会控中心
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> jsonRoot = new HashMap<String, Object>();
			Map<String, Object> jsonContent = new HashMap<String, Object>();
			jsonRoot.put("cmd", VSMeet.VSM_DELETEDEVICE);
			jsonRoot.put("userId", paramsMap.get("userId"));
			jsonContent.put("meetingId", paramsMap.get("scheduleId"));
			jsonRoot.put("content", jsonContent);
			String json = objectMapper.writeValueAsString(jsonRoot);
			// 获取调度服务配置信息（会管服务器的vc_server表中查找）
			paramsMap.put("type", 1);
			ServerVO ser = sysConfigService.getServer(paramsMap);
			if (null == ser || StringUtils.isBlank(ser.getIp())
					|| StringUtils.isBlank(ser.getPort())
					|| Integer.parseInt(ser.getPort()) == 0) {
				result.put("msg","终端退会失败：未找到调度服务");
				return result;
			}
			// 连接调度服务器
			SocketClient client = new SocketClient(ser.getIp(),
					Integer.parseInt(ser.getPort()));
			if(client.start() == -1){
				result.put("msg","终端退会失败：无法连接调度服务器");
				return result;
			}
			client.sendData(VSMeet.VSM_DELETEDEVICE, json.getBytes("utf-8"));
			Map<String, Object> map = client.recvData(JSONObject.class);
			client.close();
			if (null == map) {
				result.put("msg","终端退会失败：调度务器无响应");
				return result;
			}
			// 处理返回数据
			if (null == map.get("cmd")) {
				result.put("msg","终端退会失败：调度服务器返回消息为空");
				return result;
			}
			if (Integer.parseInt(map.get("cmd").toString()) != VSMeet.VSM_STOPMEETING + 1) {
				result.put("msg","终端退会失败：调度服务器返回消息异常");
				return result;
			}
			if (Integer.parseInt(map.get("result").toString()) != 0) {
				result.put("msg","终端退会失败:" + map.get("description").toString());
				return result;
			}
			result.put("result", true);
			result.put("msg","终端退会成功");
		} catch (Exception e) {
			logger.error("通过会管终端退会失败：", e);
			result.put("msg","终端退会失败：系统内部异常");
		}
		return result;
	}*/
	
	/* @param paramsMap
	 * 	businessType 业务类型。
	 * 		0表示兼容老版本的视联汇.
	 * 		1表示实时会议（状态为4，并且不超时、不掉线）
	 * 		2表示预约会议（状态为2，并且在有效时间之内）
	 * 		3表示历史会议（状态为5）
	 * 		4表示故障会议（状态为4，并且超时或者掉线的）
	 * @return
	 */
	@Override
	public ResponseInfo<DataInfo<Map<String, Object>>> getAllMeetingList(Map<String, Object> paramsMap) {
		ResponseInfo<DataInfo<Map<String, Object>>> result = new ResponseInfo<DataInfo<Map<String, Object>>>();
		result.setErrcode(WsConstants.ERROR);
		result.setErrmsg("获取会议列表失败");
		int pageSize = Integer.parseInt(paramsMap.get("pageSize").toString()) ;
		
		int pageNum = null == paramsMap.get("pageNum") ? 1 : Integer.parseInt(paramsMap.get("pageNum").toString()) ;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int total = 0 ;
		
		if (paramsMap.get("businessType").equals("0")) {//兼容老版本的视联汇
			//list = this.scheduleDao.getAllMeetingList(paramsMap);
			if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
				list = scheduleDao.getAllMeetingList(paramsMap, new RowBounds());
				total = scheduleDao.getAllMeetingCount(paramsMap);
			}else{
				list = scheduleDao.getAllMeetingList(paramsMap, new RowBounds((Integer)pageSize * (pageNum - 1),(Integer)paramsMap.get("pageSize")));
				total = scheduleDao.getAllMeetingCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;
			}
		} else {//实时会议或者预约会议或者历史会议或者故障会议(SQL中通过businessType进行判断了)
			//计算预约查询时间(开始时间和结束时间) Start
			Integer scheduleHours = (Integer)paramsMap.get("scheduleHours");
			if (scheduleHours != null && scheduleHours != 0) {
				if (scheduleHours < 0) {//为负数,表示历史
					String historyEndTime = this.getAllMeetingTimeParams(-24, 23, 59, 59);//过去一天，并将时间设置成23:59:59
					String historyStartTime = this.getAllMeetingTimeParams(scheduleHours, 00, 00, 00);//过去scheduleHours小时
					paramsMap.put("historyStartTime", historyStartTime);
					paramsMap.put("historyEndTime", historyEndTime);
				} else {//为正数，表示预约
					String scheduleStartTime = this.getAllMeetingTimeParams(24, 00, 00, 00);//未来一天，并将时间设置成00:00:00
					String scheduleEndTime = this.getAllMeetingTimeParams(scheduleHours, 23, 59, 59);//未来scheduleHours小时
					paramsMap.put("scheduleStartTime", scheduleStartTime);
					paramsMap.put("scheduleEndTime", scheduleEndTime);
				}
			}
			//计算预约查询时间(开始时间和结束时间) End
			
			//计算获取日历的数据时间条件 Start
			Integer year = (Integer)paramsMap.get("year");//年
			Integer month = (Integer)paramsMap.get("month");//月
			Integer dayOfMonth = (Integer)paramsMap.get("dayOfMonth");//日
			String calendarStartTime = "";//获取日历数据的开始时间
			String calendarEndTime = "";//获取日历数据的结束时间
			
			if ((year != null && year > 0) && (month == null || month == 0) && (dayOfMonth == null || dayOfMonth == 0)) {//按年份查询
				calendarStartTime = TimeUtil.getSpecifiedTime(year, 1, 1, 0, 0, 0);//计算获取日历数据的开始时间
				calendarEndTime = TimeUtil.getSpecifiedTime(year, 12, 21, 23, 59, 59);//计算获取日历数据的结束时间
			} else if ((year != null && year > 0) && (month != null && month > 0) && (dayOfMonth == null || dayOfMonth == 0)) {//按年月查询
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, month - 1, 1);
				calendarStartTime = TimeUtil.getSpecifiedTime(year, month, 1, 0, 0, 0);//计算获取日历数据的开始时间:当月第一天
				calendarEndTime = TimeUtil.getSpecifiedTime(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);//计算获取日历数据的结束时间:当月最后一天
			} else if ((year != null && year > 0) && (month != null && month > 0) && (dayOfMonth != null && dayOfMonth > 0)) {//按日期查询
				calendarStartTime = TimeUtil.getSpecifiedTime(year, month, dayOfMonth, 0, 0, 0);//计算获取日历数据的开始时间
				calendarEndTime = TimeUtil.getSpecifiedTime(year, month, dayOfMonth, 23, 59, 59);//计算获取日历数据的结束时间
			}
			paramsMap.put("calendarStartTime", calendarStartTime);
			paramsMap.put("calendarEndTime", calendarEndTime);
			//计算获取日历的数据时间条件 End
			
			if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
				list = scheduleDao.selectScheduleListByBusinessType(paramsMap, new RowBounds());
			}else{
				list = scheduleDao.selectScheduleListByBusinessType(paramsMap, new RowBounds((Integer)pageSize * (pageNum - 1),(Integer)paramsMap.get("pageSize")));
			}
			
			if (paramsMap.get("businessType").equals("4")) {//故障会议
				if (list != null){
					SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					long now = System.currentTimeMillis();
					for (Map<String, Object> map : list) {
						if(map.get("endTime") != null){
							long end = 0;
							try {
								end = simpleFormat.parse(map.get("endTime").toString()).getTime();
								if ((now - end)/(1000 * 60 * 60) >= 1){
									map.put("faultStatus", "会议超时");
								}
								Object createType = map.get("createType");
								Object operatorStatus = map.get("operatorStatus");
								if(createType == null || operatorStatus == null){
									continue;
								}
								if (operatorStatus.toString().equals("0") && 
										(createType.toString().equals("1") || 
												createType.toString().equals("7") || 
												createType.toString().equals("8"))){
									map.put("faultStatus", "PAMIR掉线");
								}
							} catch (ParseException e) {
								logger.error("获取故障会议列表，时间转换出现异常：", e);
								throw new RuntimeException("时间转换出现异常");
							}
						}
					}
				}
			}
			if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
				total = scheduleDao.selectScheduleListByBusinessTypeCount(paramsMap);
			}else{
				total = scheduleDao.selectScheduleListByBusinessTypeCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;
			}
			
		}
		
		HashMap<String, Object> extraMap = new HashMap<String, Object>();
		extraMap.put("totalPage", total);
		extraMap.put("businessType", paramsMap.get("businessType"));
		extraMap.put("currentTime", System.currentTimeMillis());
		DataInfo<Map<String, Object>> dataMap = new DataInfo<Map<String, Object>>();
		dataMap.setExtra(extraMap); 
		dataMap.setItems(list);
		result.setData(dataMap);
		result.setErrmsg("获取会议列表成功");
		result.setErrcode(WsConstants.OK);
		return result ;
	}
	/**
	 * 获取视联汇会议列表的时间查询条件
	 * @param amountHours 步进的时间长度，单位是小时
	 * @param hours 设置小时
	 * @param minute 设置分钟
	 * @param second 设置秒
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	private String getAllMeetingTimeParams(int amountHours, int hours, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, amountHours);//过去scheduleHours小时
		calendar.set(Calendar.HOUR_OF_DAY, hours);//设置小时
		calendar.set(Calendar.MINUTE, minute);//设置分钟
		calendar.set(Calendar.SECOND, second);//设置秒
		return TimeUtil.dateToString(calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
	}
}
