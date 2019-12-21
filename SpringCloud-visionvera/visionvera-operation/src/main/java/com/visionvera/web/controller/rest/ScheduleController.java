package com.visionvera.web.controller.rest;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.restful.client.RestClient;
import com.visionvera.config.SysConfig;
import com.visionvera.constrant.WsConstants;
import com.visionvera.feign.UserService;
import com.visionvera.service.ScheduleService;
import com.visionvera.util.SpringContextUtil;
import com.visionvera.util.StringUtil;


@RequestMapping("/rest/schedule")
@RestController
public class ScheduleController extends BaseReturn {
	
	@Autowired
	SysConfig sysConfig;

	@Autowired
	private ScheduleService scheduleService;
	
	@Autowired
	private UserService userService;
	
	private static final Logger logger = LogManager.getLogger(ScheduleController.class);
	/**
	 * 
	 * @Title: getScheduleListExt
	 * @Description: TODO 预约列表查询（附带返回已办、待办、已发列表）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/scheduleListExt", method = RequestMethod.GET)
	public ReturnData getScheduleListExt(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestParam(value = "access_token") String token,
			String scheduleName, String startTime, String endTime,String svrRegionId,
			String creatorId, String creatorName, Integer status, String devId) {
		Map<String, Object> extraMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		UserVO user = new UserVO();
		try {
			/** 从缓存中获取用户信息 Start */
			ReturnData userData = this.userService.getUser(token);
			if (!userData.getErrcode().equals(0)) {
				return super.returnError("用户服务不可用, 请稍后再试");
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>)userData.getData();
			user = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
			/** 从缓存中获取用户信息 End */
			
			
			
			paramsMap.put("pageNum", pageNum);
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("name", scheduleName == null ? scheduleName
					: URLDecoder.decode(scheduleName, "utf-8"));
			paramsMap.put("start_time", startTime);
			paramsMap.put("end_time", endTime);
			paramsMap.put("svrRegionId", svrRegionId);
			
			ActivitiController ac = (ActivitiController) SpringContextUtil.getBean("activitiController"); 
			Map<String, List<String>> procIdLists = ac.getProcIdLists(user.getLoginName());
			
			if (StringUtil.isNull(creatorId) && !user.getLoginName().equals("admin")) {//除了超级管理员，其他角色用户都只能看自己的预约
				List<String> tasks = new ArrayList<String>();
				tasks.addAll(procIdLists.get("done"));// 4.已办事项
				tasks.addAll(procIdLists.get("todo"));// 3.需要自己审批的预约
				
				// 1.自己创建的预约
				creatorId = user.getUuid();
				// 2.自己创建的预约以及别人授权给自己的预约（都根据用户名来获取）
				paramsMap.put("userName", user.getName());
				
				// 3.需要自己审批的预约  4.已办事项
				List<String> processIds = new ArrayList<String>();
				for (String task : tasks) {
					if(!processIds.contains(task)){
						processIds.add(task);//审批流程ID
					}else{//去除重复数据
						continue;
					}
				}
				paramsMap.put("processIds", processIds);
			}
			
			paramsMap.put("creatorId", creatorId);
			paramsMap.put("creatorName", creatorName == null ? creatorName
					: URLDecoder.decode(creatorName, "utf-8"));
			paramsMap.put("status", status);
			paramsMap.put("devId", devId);
			PageInfo<ScheduleVO> scheduleInfo = this.scheduleService.getScheduleList(paramsMap);
			extraMap.put("pageNum", pageNum);
			extraMap.put("totalPage", scheduleInfo.getPages());
			return super.returnResult(0, "查询成功", null, scheduleInfo.getList(), extraMap);
		} catch (Exception e) {
			logger.error("获取预约列表失败, msg: ", e);
			return super.returnError("系统内部异常");
		}
	}
	
	/**
	 * 
	 * @Title: getAccessorDetail
	 * @Description: TODO 预约审批详情
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/getAccessorDetail", method = RequestMethod.GET)
	public ReturnData getAccessorDetail(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestParam(value = "access_token") String token,
			String scheduleName, String createtimeStart, String createtimeEnd, String accessortimeStart, String accessortimeEnd,
			String creatorId, String creatorName, String accessor, String devId, String svrRegionId,
			String aprExc,String aprTime,String status) {
		Map<String, Object> extraMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		UserVO user = new UserVO();
		try {
			/** 从缓存中获取用户信息 Start */
			ReturnData userData = this.userService.getUser(token);
			if (!userData.getErrcode().equals(0)) {
				return super.returnError("用户服务不可用, 请稍后再试");
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>)userData.getData();
			user = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
			/** 从缓存中获取用户信息 End */
			
			paramsMap.put("pageNum", pageNum);
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("svrRegionId", svrRegionId);
			paramsMap.put("name", scheduleName == null ? scheduleName
					: URLDecoder.decode(scheduleName, "utf-8"));
			paramsMap.put("createtimeStart", StringUtils.isBlank(createtimeStart) ? createtimeStart : createtimeStart + " 00:00:00");
			paramsMap.put("createtimeEnd", StringUtils.isBlank(createtimeEnd) ? createtimeEnd : createtimeEnd + " 23:59:59");
			paramsMap.put("accessortimeStart", StringUtils.isBlank(accessortimeStart) ? accessortimeStart : accessortimeStart + " 00:00:00");
			paramsMap.put("accessortimeEnd", StringUtils.isBlank(accessortimeEnd) ? accessortimeEnd : accessortimeEnd + " 23:59:59");
			if (StringUtils.isBlank(aprTime)) {
				return super.returnError("参数错误");
			}
			if (StringUtils.isBlank(aprExc)){
				return super.returnError("参数错误");
			}
			paramsMap.put("aprExc",Integer.valueOf(aprExc)*60*1000);
			paramsMap.put("aprTime",Integer.valueOf(aprTime)*60*1000);
			paramsMap.put("status",status);
			ActivitiController ac = (ActivitiController) SpringContextUtil.getBean("activitiController"); 
			Map<String, List<String>> procIdLists = ac.getProcIdLists(user.getLoginName());
			if(StringUtils.isBlank(creatorId) && !"admin".equals(user.getLoginName())){//除了超级管理员，其他角色用户都只能看自己的预约
				List<String> tasks = new ArrayList<String>();
				tasks.addAll(procIdLists.get("done"));// 4.已办事项
				tasks.addAll(procIdLists.get("todo"));// 3.需要自己审批的预约
				// 1.自己创建的预约
				creatorId = user.getUuid();
				// 2.自己创建的预约以及别人授权给自己的预约（都根据用户名来获取）
				paramsMap.put("userName", user.getName());
				// 3.需要自己审批的预约  4.已办事项
				List<String> processIds = new ArrayList<String>();
				for (String task : tasks) {
					if(!processIds.contains(task)){
						processIds.add(task);//审批流程ID
					}else{//去除重复数据
						continue;
					}
				}
				paramsMap.put("processIds", processIds);
			}
			paramsMap.put("creatorId", creatorId);
			paramsMap.put("creatorName", creatorName == null ? creatorName
					: URLDecoder.decode(creatorName, "utf-8"));
			paramsMap.put("accessor", accessor == null ? accessor
					: URLDecoder.decode(accessor, "utf-8"));
			paramsMap.put("devId", devId);
			
			System.out.println("传递的参数:" + JSONObject.toJSONString(paramsMap));
			
			PageInfo<ScheduleVO> scheduleInfo = scheduleService
					.getAccessorDetail(paramsMap);
			
			extraMap.put("pageNum", pageNum);
			extraMap.put("totalPage", scheduleInfo.getPages());
			return super.returnResult(0, "获取成功", null, scheduleInfo.getList(), extraMap);
		} catch (Exception e) {
			logger.error("获取预约列表失败, msg: ", e);
			return super.returnError("系统内部异常");
		}
	}
	
	/**
	 * 
	 * @Title: getScheduleServers
	 * @Description: TODO 预约所属平台查询
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/getDetailServers", method = RequestMethod.GET)
	public ReturnData getDetailServers() {
		try {
			PageInfo<RegionVO> regionInfo = this.scheduleService.getScheduleServers(false, null, null);
			return super.returnResult(0, "获取成功", null, regionInfo.getList());
		} catch (Exception e) {
			logger.error("获取预约平台列表失败, msg: ", e);
			return super.returnError("系统内部异常");
		}
	}
	
	/**
	 * 删除预约会议
	 * @param uuids 预约会议ID，多个ID使用英文逗号","分隔
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "/delSchedule", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData delSchedule(@RequestBody ScheduleVO schedule, @RequestParam("access_token") String token) {
		try {
			if (StringUtil.isNull(schedule.getUuid())) {
				return super.returnError("预约UUID不能为空");
			}
//			return this.scheduleService.delSchedule(schedule.getUuid());
			if(scheduleService.isOK2Delete(schedule.getUuid()) > 0){
				return super.returnError("正在开会，不允许删除");
			}
			UserVO user = new UserVO();
			user.setOperatorId(schedule.getOperatorId());
//			Map<String, Object> userMapWithoutNull = this.getMapWithoutNull(user);
//	    	String url = sysConfig.getHgUrl() + String.format(WsConstants.URL_CMS_SCHEDULE_DELETE,schedule.getUuid());
//	    	Map<String, Object> result = RestClient.post(url,token, userMapWithoutNull);
	    	ReturnData result = this.scheduleService.delSchedule(schedule.getUuid());
	    	
	    	if(result.getErrcode() != null && (Integer)result.getErrcode() == 1){
	    		return super.returnResult(1, result.getErrmsg() == null ? "删除预约失败" : result.getErrmsg().toString());
	    	}
	    	logger.info("删除预约成功：预约ID：" + schedule.getUuid() + ", 用户ID：" + schedule.getOperatorId());
	    	return super.returnResult(0, "删除预约成功");
		} catch (Exception e) {
			logger.error("ScheduleController ===== delSchedule ===== 删除预约失败 =====> ", e);
			return super.returnError("删除预约失败");
		}
	}
	
	/**
     * 将对象去掉空值后转成Map
     * @param args
     * @return
     */
    @SuppressWarnings({ "unchecked"})
	private Map<String, Object> getMapWithoutNull(Object args) {
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	
    	if (args != null) {
    		String jsonStr = JSONObject.toJSONString(args);//去掉NULL
    		resultMap = JSONObject.parseObject(jsonStr, Map.class);
    	}
    	
    	return resultMap;
    }
	
	/**
	 * 
	 * @Title: getScheduleListByDate
	 * @Description: TODO 根据指定日期查询预约列表
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/getScheduleListByDate/{startTime}", method = RequestMethod.GET)
	public Map<String,Object> getScheduleListByDate(@PathVariable(value = "startTime") String startTime) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("startTime", startTime);
			paramsMap.put("endTime", startTime);
			List<ScheduleFormVO> list = scheduleService.getScheduleListByDate(paramsMap);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("items",list);
			data.put("extra",null);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "获取会议预约列表成功");
			resultMap.put("access_token",null);
			resultMap.put("data",data);
			
		} catch (Exception e) {
			logger.error("系统异常",e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "系统内部异常");
			resultMap.put("access_token",null);
			resultMap.put("data",null);
		}
		return resultMap;
	}
}

