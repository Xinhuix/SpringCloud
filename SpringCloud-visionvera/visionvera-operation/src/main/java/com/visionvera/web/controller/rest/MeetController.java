package com.visionvera.web.controller.rest;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.EmployeeVO;
import com.visionvera.bean.cms.LogVO;
import com.visionvera.bean.cms.MeetRecordVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.TRoleVO;
import com.visionvera.feign.UserService;
import com.visionvera.service.ActivitiService;
import com.visionvera.service.MeetService;
import com.visionvera.service.ScheduleService;

/**
 * 
 * @ClassName: MeetController
 * @Description: TODO 预约列表查询(这里用一句话描述这个类的作用)
 * @author xiechengsuan
 * @date 2016年11月1日
 * 
 */
@RestController
@RequestMapping("/rest/meet")
public class MeetController {

	@Resource
	private MeetService meetService;
	@Resource
	ProcessEngine engine;
	@Resource
	private ActivitiService activitiService;
	@Resource
	private ScheduleService scheduleService;
	@Resource
	private UserService userService;
	/*@Resource
	private RegisterService registerService;*/
	
	private static final Log logger = LogFactory
			.getLog(MeetController.class);

	/***
	 * 跳转到列表页面
	 * 
	 * @return
	 */
	@RequestMapping("showList")
	public String goList() {

		return "meet/meetList";
	}

	
	
	/** <pre>goMaster(跳转至会议统计页面)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年5月7日 下午5:40:44    
	 * 修改人：周逸芳        
	 * 修改时间：2018年5月7日 下午5:40:44    
	 * 修改备注： 
	 * @return</pre>    
	 */
	@RequestMapping("masterList")
	public String goMaster() {

		return "meet/masterList";
	}
	
	/**
	 * 
	 * @Title: getMeetList
	 * @Description: TODO 预约列表查询
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/meetList", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> getMeetList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestParam(value = "access_token") String token,
			@RequestBody ScheduleVO scheduleVO, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		UserVO userData = new UserVO();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("name", scheduleVO.getName() == null ? scheduleVO.getName()
					: URLDecoder.decode(scheduleVO.getName(), "utf-8"));
			paramsMap.put("creatorName", scheduleVO.getCreatorName() == null ? scheduleVO.getCreatorName()
					: URLDecoder.decode(scheduleVO.getCreatorName(), "utf-8"));
			paramsMap.put("start_time", scheduleVO.getStartTime());
			paramsMap.put("end_time", scheduleVO.getEndTime());
			paramsMap.put("svrRegionId", scheduleVO.getSvrRegionId());
			ReturnData data = userService.getUser(token);
			if (!data.getErrcode().equals(0)) {
				resultMap.put("result", false);
				resultMap.put("errmsg", data.getErrmsg());
				return resultMap;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>)data.getData();
			userData = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
			int i = 0;
			for (TRoleVO roleList : userData.getRoleList()) {
				if("495e7d3899f111e8980fac1f6b6c76c6".equals(roleList.getUuid()) || "38549368797b46d7ad9e7066d36c25f7".equals(roleList.getUuid())){
					i++;
					break;
				}
			}
			if(i > 0){//除了超级管理员，其他角色用户都只能看自己的预约
				// 1.自己创建的预约
				String creatorId = userData.getUuid();
				// 2.别人授权给自己的预约
				paramsMap.put("userName", userData.getName());
				// 3.需要自己审批的预约
				List<EmployeeVO> user = activitiService.getInfo(userData.getLoginName());
				if(user.size() == 0){
					resultMap.put("result", false);
					resultMap.put("errmsg", "该用户不存在");
				}
				List<Task> list = engine.getTaskService().createTaskQuery().taskAssignee(user.get(0).getId())
						.list();
				List<String> processIds = new ArrayList<String>();
				for (Task task : list) {
					processIds.add(task.getProcessInstanceId());//审批流程ID
				}
				// 4.已办事项
				HistoryService service = engine.getHistoryService();
				List<HistoricTaskInstance> doneList = service
						.createHistoricTaskInstanceQuery().finished().list();
				for (HistoricTaskInstance task : doneList) {
					if(StringUtils.isBlank(task.getAssignee()) || !task.getAssignee().contains(user.get(0).getId())){
						continue;
					}
					if(!processIds.contains(task.getProcessInstanceId())){
						processIds.add(task.getProcessInstanceId());//审批流程ID
					}else{//去除重复数据
						continue;
					}
				}
				paramsMap.put("processIds", processIds);
				paramsMap.put("creatorId", creatorId);
			}
			List<ScheduleVO> meetList = meetService
					.getMeetList(paramsMap);
			int total = meetService.getMeetListCount(paramsMap);
			total = total % pageSize == 0 ? total / pageSize
					: (total / pageSize) + 1;

			resultMap.put("pageNum", pageNum);
			resultMap.put("pageTotal", total);
			resultMap.put("result", true);
			resultMap.put("list", meetList);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取会议列表失败, msg: ", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getMeetList
	 * @Description: TODO 根据会议ID查询会议操作日志（不分页）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("logList")
	public Map<String, Object> getLogList(String scheduleId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("scheduleId", scheduleId);
			List<LogVO> log_list = meetService.getLogList(paramsMap);
			resultMap.put("list", log_list);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("查询会议操作日志列表失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getRecordList
	 * @Description: TODO 根据会议ID查询会议纪要列表
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("recordList")
	public Map<String, Object> getRecordList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String scheduleId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("scheduleId", scheduleId);
			List<MeetRecordVO> list = meetService.getRecordList(paramsMap);
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("查询会议纪要表失败", e);
		}
		return resultMap;
	}
/*
	*//**
	 * 
	 * @Title: stopMeeting
	 * @Description: TODO 根据会议ID停止会议
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 *//*
	@RequestMapping("stopMeeting")

	public Map<String, Object> stopMeeting(String scheduleId, String reason, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		ScheduleFormVO sf = new ScheduleFormVO();
		sf.setUuid(scheduleId);
		try {
			paramsMap.put("scheduleId", scheduleId);
			// 获取会议状态
			List<ScheduleVO> list = scheduleService.getSchedule(paramsMap);
			if(list == null || list.size() == 0){
				resultMap.put("result", false);
				resultMap.put("errmsg", "停会失败,会议不存在");
				return resultMap;
			}
//			if(StringUtils.isNotBlank(list.get(0).getSvrRegionId())){
//				resultMap.put("result", false);
//				resultMap.put("errmsg", "暂不支持停止子系统的会议");
//				return resultMap;
//			}
			paramsMap.put("userId", list.get(0).getCreatorId());
			ScheduleVO scheduleVO = new ScheduleVO();
			scheduleVO.setUuid(scheduleId);
			scheduleVO.setStatus("5");
			scheduleVO.setStopReason(reason == null ? "正常" : URLDecoder.decode(reason, "utf-8"));
			scheduleVO.setStopStatus(3);//实时监测页面停会状态为手动停会
			RegionVO region = scheduleService.selectRegionId(scheduleVO);
			if (region!=null) {
				//获取该系统的所有子系统
				ServerSyncVO sv = new ServerSyncVO();
				sv.setType(2);//1上级会管服务 2下级会管服务器
				sv.setAreaId(region.getId());
				List<ServerSyncVO> svList = registerService.getSyncServers(sv);
				if (null != svList && svList.size() > 0) {
					//获取基础url
					String baseUrl = String.format(WsConstants.CMS_HOST_URL, svList.get(0).getIp(),svList.get(0).getPort());
					//获得token
					Map<String, Object> args = new HashMap<String, Object>();
					args.put(WsConstants.KEY_CMS_USER, svList.get(0).getAccount());//账号
					args.put(WsConstants.KEY_CMS_PWD, svList.get(0).getPassword());//密码
					String token = null;
					try{
						logger.info("停止子系统"+svList.get(0).getIp()+"的会议，时间：" + System.currentTimeMillis());
						Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_CMS_LOGIN, null, args);
						if(data.get(WsConstants.KEY_TOKEN) == null){
							resultMap.put("result", false);
							resultMap.put("errmsg", data.get("errmsg"));
							logger.error("停止子系统会议失败：" + data.get("errmsg"));
							return resultMap;
						}
						token = data.get(WsConstants.KEY_TOKEN).toString();
					}catch(Exception e){
						resultMap.put("result", false);
						resultMap.put("errmsg", "停止子系统会议失败：获取token失败");
						logger.error("停止子系统会议失败：获取token失败");
						return resultMap;
					}
					//调用子系统停会接口停会
					Map<String, Object> data = RestClient.post(baseUrl+String.format(WsConstants.URL_CMS_STOP_MEETING, list.get(0).getCreatorId(),scheduleId),token, null);
					if(data!=null && data.get("errmsg") != null){
						resultMap.put("result", false);
						resultMap.put("errmsg", data.get("errmsg"));
						return resultMap;
					}else{
						int i = scheduleService.updateScheduleStatus(scheduleVO);
						if (i>0) {
							resultMap.put("result", true);
							resultMap.put("errmsg", "停会成功");
							Util.reportData(sf, "stopMeeting");
						}else{
							resultMap.put("result", false);
							resultMap.put("errmsg", "停会失败");
						}
						int j = scheduleService.updateStopStatus(scheduleVO);
					}
				}else{
					resultMap.put("result", false);
					resultMap.put("errmsg", "已与该子系统解除级联");
					logger.error("已与该子系统解除级联");
				}
			}else{
				Map<String, Object> ret = meetService.stopMeeting(paramsMap);
				resultMap.put("result", ret.get("result"));
				resultMap.put("errmsg", ret.get("errmsg"));
				if ((boolean)ret.get("result")) {
					int j = scheduleService.updateStopStatus(scheduleVO);
				}
				Util.reportData(sf, "stopMeeting");
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "停会失败，系统内部异常");
			logger.error("查询会议纪要表失败", e);
		}
		if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.STOP_MEETING, session, "会管停会：" + scheduleId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.STOP_MEETING, session, "会管停会：" + scheduleId, "", LogType.OPERATE_ERROR);
		}
		return resultMap;
	}
	*/
	/**
	 * 
	 * @Title: stopMeeting
	 * @Description: TODO 根据会议ID停止会议
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	/*@RequestMapping("stopMeetingWithSubSystem")

	public Map<String, Object> stopMeeting(String scheduleId, String reason) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("scheduleId", scheduleId);
			// 获取会议状态
			List<ScheduleVO> list = scheduleService.getSchedule(paramsMap);
			if(list == null || list.size() == 0){
				resultMap.put("result", false);
				resultMap.put("errmsg", "停会失败,会议不存在");
				return resultMap;
			}
//			if(StringUtils.isNotBlank(list.get(0).getSvrRegionId())){
//				resultMap.put("result", false);
//				resultMap.put("errmsg", "暂不支持停止子系统的会议");
//				return resultMap;
//			}
			paramsMap.put("userId", list.get(0).getCreatorId());
			ScheduleVO scheduleVO = new ScheduleVO();
			scheduleVO.setUuid(scheduleId);
			scheduleVO.setStatus("5");
			scheduleVO.setStopStatus(4);
			scheduleVO.setStopReason(reason == null ? "正常" : URLDecoder.decode(reason, "utf-8"));
			RegionVO region = scheduleService.selectRegionId(scheduleVO);
			if (region!=null) {
				//获取该系统的所有子系统
				ServerSyncVO sv = new ServerSyncVO();
				sv.setType(2);//1上级会管服务 2下级会管服务器
				sv.setAreaId(region.getId());
				List<ServerSyncVO> svList = registerService.getSyncServers(sv);
				if (null != svList && svList.size() > 0) {
					//获取基础url
					String baseUrl = String.format(WsConstants.CMS_HOST_URL, svList.get(0).getIp(),svList.get(0).getPort());
					//获得token
					Map<String, Object> args = new HashMap<String, Object>();
					args.put(WsConstants.KEY_CMS_USER, svList.get(0).getAccount());//账号
					args.put(WsConstants.KEY_CMS_PWD, svList.get(0).getPassword());//密码
					String token = null;
					try{
						logger.info("停止子系统"+svList.get(0).getIp()+"的会议，时间：" + System.currentTimeMillis());
						Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_CMS_LOGIN, null, args);
						if(data.get(WsConstants.KEY_TOKEN) == null){
							resultMap.put("result", false);
							resultMap.put("errmsg", data.get("errmsg"));
							logger.error("停止子系统会议失败：" + data.get("errmsg"));
							return resultMap;
						}
						token = data.get(WsConstants.KEY_TOKEN).toString();
					}catch(Exception e){
						resultMap.put("result", false);
						resultMap.put("errmsg", "停止子系统会议失败：获取token失败");
						logger.error("停止子系统会议失败：获取token失败");
						return resultMap;
					}
					//调用子系统停会接口停会
					Map<String, Object> data = RestClient.post(baseUrl+String.format(WsConstants.URL_CMS_STOP_MEETING, list.get(0).getCreatorId(),scheduleId),token, null);
					if(data!=null && data.get("errmsg") != null){
						resultMap.put("result", false);
						resultMap.put("errmsg", data.get("errmsg"));
						return resultMap;
					}else{
						int i = scheduleService.updateScheduleStatus(scheduleVO);
						if (i>0) {
							resultMap.put("result", true);
							resultMap.put("errmsg", "停会成功");
							scheduleService.updateStopStatus(scheduleVO);
							ScheduleFormVO sf = new ScheduleFormVO();
							sf.setUuid(scheduleId);
							Util.reportData(sf, "stopMeeting");
						}else{
							resultMap.put("result", false);
							resultMap.put("errmsg", "停会失败");
						}
					}
				}else{
					resultMap.put("result", false);
					resultMap.put("errmsg", "已与该子系统解除级联");
					logger.error("已与该子系统解除级联");
				}
			}else{
				Map<String, Object> ret = meetService.stopMeeting(paramsMap);
				resultMap.put("result", ret.get("result"));
				resultMap.put("errmsg", ret.get("errmsg"));
				if ((Boolean)resultMap.get("result")) {
					scheduleService.updateStopStatus(scheduleVO);
					ScheduleFormVO sf = new ScheduleFormVO();
					sf.setUuid(scheduleId);
					Util.reportData(sf, "stopMeeting");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "停会失败，系统内部异常");
			logger.error("停会失败", e);
		}
		return resultMap;
	}*/
	
	/***
	 * 获取地区ID下拉
	 */
	@RequestMapping("getConferenceSelectionArea")

	public Map<String, Object> getConferenceSelectionArea(){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try{
			resultMap.put("result", true);
			List<RegionVO> list=meetService.getConferenceSelectionArea();
			resultMap.put("list", list);
		}catch(Exception e){
			resultMap.put("result", false);
			logger.error("获取区域失败", e);
		}
		return resultMap;
	}
	
	
	/** <pre>devAttMeeting(终端入会入口)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月4日 下午2:39:45    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月4日 下午2:39:45    
	 * 修改备注： 
	 * @param scheduleId
	 * @param devId
	 * @param reason
	 * @param session
	 * @return</pre>    
	 */
	/*@RequestMapping("devAttMeeting")

	public Map<String, Object> devAttMeeting(String scheduleId, String devId,String reason, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		List<ScheduleVO> scheduleList = new ArrayList<ScheduleVO>();
		try {
			paramsMap.put("scheduleId", scheduleId);
			// 获取会议状态
			List<ScheduleVO> list = scheduleService.getSchedule(paramsMap);
			if(list == null || list.size() == 0){
				resultMap.put("result", false);
				resultMap.put("errmsg", "终端入会失败,会议不存在");
				return resultMap;
			}
			paramsMap.put("userId", list.get(0).getCreatorId());
			ScheduleVO scheduleVO = new ScheduleVO();
			scheduleVO.setUuid(scheduleId);
			//scheduleVO.setStatus("5");
			//scheduleVO.setStopReason(reason == null ? "正常" : URLDecoder.decode(reason, "utf-8"));
			RegionVO region = scheduleService.selectRegionId(scheduleVO);
			if (region!=null) {
				//获取该系统的所有子系统
				ServerSyncVO sv = new ServerSyncVO();
				sv.setType(2);//1上级会管服务 2下级会管服务器
				sv.setAreaId(region.getId());
				List<ServerSyncVO> svList = registerService.getSyncServers(sv);
				if (null != svList && svList.size() > 0) {
					//获取基础url
					String baseUrl = String.format(WsConstants.CMS_HOST_URL, svList.get(0).getIp(),svList.get(0).getPort());
					//获得token
					Map<String, Object> args = new HashMap<String, Object>();
					args.put(WsConstants.KEY_CMS_USER, svList.get(0).getAccount());//账号
					args.put(WsConstants.KEY_CMS_PWD, svList.get(0).getPassword());//密码
					String token = null;
					try{
						logger.info("操作会管"+svList.get(0).getIp()+"终端入会，时间：" + System.currentTimeMillis());
						Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_CMS_LOGIN, null, args);
						if(data.get(WsConstants.KEY_TOKEN) == null){
							resultMap.put("result", false);
							resultMap.put("errmsg", data.get("errmsg"));
							logger.error("终端入会失败：" + data.get("errmsg"));
							return resultMap;
						}
						token = data.get(WsConstants.KEY_TOKEN).toString();
					}catch(Exception e){
						resultMap.put("result", false);
						resultMap.put("errmsg", "终端入会失败：获取token失败");
						logger.error("终端入会失败：获取token失败");
						return resultMap;
					}
					
					//查询终端是否已在会议中
					paramsMap.put("devId", devId);
					scheduleList = scheduleService.getDevStatus(paramsMap);
					//若终端已在现有正在开会的会议中，则删除它
					if (scheduleList != null && scheduleList.size() > 0) {
						//调用终端退会接口
						Map<String, Object> data = RestClient.post(baseUrl+String.format(WsConstants.URL_CMS_DEVICE_MEETING, list.get(0).getCreatorId(),scheduleId),token, null);
						if(data!= null){
							resultMap.put("result", false);
							resultMap.put("errmsg", data.get("errmsg"));
							return resultMap;
						}else{
							//删除原有的会议关联
							Map<String, Object> schedule = new HashMap<String, Object>();
							schedule.put("scheduleId", scheduleList.get(0).getUuid());
							schedule.put("devId", devId);
							int i = scheduleService.delScheduleDev(schedule);
							if (i>0) {
								resultMap.put("result", true);
								resultMap.put("errmsg", "终端退会成功");
							}else{
								resultMap.put("result", false);
								resultMap.put("errmsg", "终端退会失败");
							}
						}
					}
					//调用终端入会接口
					Map<String, Object> data = RestClient.post(baseUrl+String.format(WsConstants.URL_CMS_DEVICE_MEETING, list.get(0).getCreatorId(),scheduleId),token, null);
					if(data!= null){
						resultMap.put("result", false);
						resultMap.put("errmsg", data.get("errmsg"));
						return resultMap;
					}else{
						Map<String, Object> schedule = new HashMap<String, Object>();
						schedule.put("scheduleId", scheduleId);
						schedule.put("devId", devId);
						int i = scheduleService.addScheduleDev(schedule);
						if (i>0) {
							resultMap.put("result", true);
							resultMap.put("errmsg", "终端入会成功");
						}else{
							resultMap.put("result", false);
							resultMap.put("errmsg", "终端入会失败");
						}
					}
				}else{
					resultMap.put("result", false);
					resultMap.put("errmsg", "已与该子系统解除级联");
					logger.error("已与该子系统解除级联");
				}
			}else{
				//如果该设备已在别的会议中，先退会
				if (scheduleList != null && scheduleList.size() > 0) {
					Map<String, Object> ret = meetService.devDelMeeting(paramsMap);
				}
				Map<String, Object> ret = meetService.devAttMeeting(paramsMap);
				resultMap.put("result", ret.get("result"));
				resultMap.put("errmsg", ret.get("errmsg"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "终端入会失败，系统内部异常");
			logger.error("终端入会失败", e);
		}
		if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.DEVICE_MEETING, session, "终端入会：" + scheduleId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.DEVICE_MEETING, session, "终端入会：" + scheduleId, "", LogType.OPERATE_ERROR);
		}
		return resultMap;
	}*/
	
}
