/**  
 * @Title: UtilController.java
 * @Package com.visionvera.union.restController
 * @Description: TODO
 * @author 谢程算
 * @date 2018年5月30日
 */
package com.visionvera.web.controller.rest;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.BandWidthVO;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DepartmentVO;
import com.visionvera.bean.cms.EmployeeVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.SummaryFormVO;
import com.visionvera.bean.cms.TaskConditionVO;
import com.visionvera.bean.cms.TaskVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.common.api.operation.ActivitiAPI;
import com.visionvera.config.SysConfig;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.operation.ActivitiDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.feign.UserService;
import com.visionvera.service.ActivitiService;
import com.visionvera.service.SysConfigService;
import com.visionvera.util.RandomUtil;
import com.visionvera.util.StringUtil;
import com.visionvera.util.TimeUtil;

@RestController
public class ActivitiController extends BaseReturn implements ActivitiAPI{
	private static final Logger logger = LogManager
			.getLogger(ActivitiController.class);
	@Autowired
	private ActivitiService activitiService;
	@Resource
	private ActivitiDao activitiDao;
	@Resource
	ProcessEngine engine;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private SysConfigService sysConfigService;
	
	@Autowired
	private SysConfig sysConfig;
	
	@Autowired
	private JRedisDao jedisDao;

	/**
	 * 
	 * TODO 根据用户名获取代办列表
	 */
	@RequestMapping(value = "getTodoList/{name}", method = RequestMethod.GET)
	public Map<String, Object> getTodoList(@PathVariable("name") String name) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
		try {
			if (StringUtils.isBlank(name)) {
				resultMap.put("errmsg", "用户名不能为空");
				return resultMap;
			}
			resultMap = activitiService.getTodoList(name);
		} catch (Exception e) {
			logger.error("获取待办事项失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 获取流程跟踪图（标志所有已办节点和之间的连线，优点：已结束流程也可查看流程图）
	 */
	@RequestMapping(value = "graphics/{processId}", method = RequestMethod.GET)
	public void graphics(@PathVariable("processId") String processId,
			HttpServletResponse response) {
		try {
			HistoricProcessInstance pi = engine.getHistoryService()
					.createHistoricProcessInstanceQuery()
					.processInstanceId(processId).singleResult();
			String definitionId = pi.getProcessDefinitionId();
			// 该流程实例的所有节点审批记录
			Map<String, List<String>> assignees = new HashMap<String, List<String>>();
			List<HistoricActivityInstance> hisActInstList = getHisUserTaskActivityInstanceList(processId);
			List<String> notifyList = new ArrayList<String>();
			for (Iterator<HistoricActivityInstance> iterator = hisActInstList
					.iterator(); iterator.hasNext();) {
				HistoricActivityInstance hai = iterator.next();
				if (StringUtils.isBlank(hai.getAssignee())) {
					continue;
				}
				notifyList.addAll(str2List(hai.getAssignee()));
				assignees.put(hai.getActivityId(), str2List(hai.getAssignee()));
			}

			// 设置各节点审批人名称
			Map<String, String> assigneeNames = new HashMap<String, String>();
			if (notifyList.size() > 0) {
				String names;
				List<EmployeeVO> ev = activitiService.getInfoId(notifyList);
				Map<String, String> evMap = new HashMap<String, String>();
				for (EmployeeVO e : ev) {
					evMap.put(e.getId(), e.getName());
				}
				for (String key : assignees.keySet()) {
					names = "";
					List<String> ids = assignees.get(key);
					for (String id : ids) {
						names += evMap.get(id) + ",";
					}
					if (StringUtils.isBlank(names)) {
						names = "无";
					} else if (names.endsWith(",")) {
						names = names.substring(0, names.lastIndexOf(","));
					}
					assigneeNames.put(key, names);
				}
			}

			// 修改流程各节点的名称
			BpmnModel bpmnModel = engine.getRepositoryService().getBpmnModel(
					definitionId);
			// 因为我们这里只定义了一个Process 所以获取集合中的第一个即可
			org.activiti.bpmn.model.Process process = bpmnModel.getProcesses()
					.get(0);
			// 获取流程关联的表单信息
			ScheduleFormVO sf = new ScheduleFormVO();
			sf.setProcessId(processId);
			List<ScheduleFormVO> sfList = activitiService.getForm(sf);
			Map<String, String> defaultNames = new HashMap<String, String>();// 各节点的默认名字
			if (sfList != null && sfList.size() > 0) {
				defaultNames.put("usertask1", sfList.get(0).getCreator());
			} else {
				defaultNames.put("usertask1", "发起申请");
			}
			if (defaultNames.get("usertask2") == null) {
				defaultNames.put("usertask2", "网络部");
			}
			defaultNames.put("usertask3", "网络部");
			defaultNames.put("usertask5", "会议总结");
			Collection<FlowElement> flowElements = process.getFlowElements();
			for (FlowElement flowElement : flowElements) {
				// 如果是任务节点
				if (flowElement instanceof UserTask) {
					UserTask userTask = (UserTask) flowElement;
					if (defaultNames.containsKey(userTask.getId())) {
						userTask.setName(defaultNames.get(userTask.getId()));
					} else {
						userTask.setName("");
					}
				}
			}
			// 再重新设置各节点审批人信息
			for (FlowElement flowElement : flowElements) {
				// 如果是任务节点
				if (flowElement instanceof UserTask) {
					UserTask userTask = (UserTask) flowElement;
					if (assigneeNames.containsKey(userTask.getId())) {
						userTask.setName(assigneeNames.get(userTask.getId()));
					}
				}
			}
			response.setContentType("image/png");
			List<HistoricActivityInstance> highLightedActivitList = engine
					.getHistoryService().createHistoricActivityInstanceQuery()
					.processInstanceId(processId).list();
			// 高亮环节id集合
			List<String> highLightedActivitis = new ArrayList<String>();
			// 高亮线路id集合
			ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) engine
					.getRepositoryService().getProcessDefinition(
							pi.getProcessDefinitionId());
			List<String> highLightedFlows = getHighLightedFlows(
					definitionEntity, highLightedActivitList);

			for (HistoricActivityInstance tempActivity : highLightedActivitList) {
				String activityId = tempActivity.getActivityId();
				highLightedActivitis.add(activityId);
			}
			DefaultProcessDiagramGenerator ddg = new DefaultProcessDiagramGenerator();
			InputStream is = ddg.generateDiagram(bpmnModel, "png",
					highLightedActivitis, highLightedFlows, "宋体", "宋体", null,
					1.0);

			int len = 0;
			byte[] b = new byte[1024 * 10];
			while ((len = is.read(b, 0, 1024)) != -1) {
				response.getOutputStream().write(b, 0, len);
			}
		} catch (Exception e) {
			logger.error("获取流程跟踪图失败：" + e);
		}
	}

	/**
	 * 
	 * 获取需要高亮的线
	 */
	private List<String> getHighLightedFlows(
			ProcessDefinitionEntity processDefinitionEntity,
			List<HistoricActivityInstance> historicActivityInstances) {
		List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
		for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
			ActivityImpl activityImpl = processDefinitionEntity
					.findActivity(historicActivityInstances.get(i)
							.getActivityId());// 得到节点定义的详细信息
			List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
			ActivityImpl sameActivityImpl1 = processDefinitionEntity
					.findActivity(historicActivityInstances.get(i + 1)
							.getActivityId());
			// 将后面第一个节点放在时间相同节点的集合里
			sameStartTimeNodes.add(sameActivityImpl1);
			for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
				HistoricActivityInstance activityImpl1 = historicActivityInstances
						.get(j);// 后续第一个节点
				HistoricActivityInstance activityImpl2 = historicActivityInstances
						.get(j + 1);// 后续第二个节点
				if (activityImpl1.getStartTime().equals(
						activityImpl2.getStartTime())) {
					// 如果第一个节点和第二个节点开始时间相同保存
					ActivityImpl sameActivityImpl2 = processDefinitionEntity
							.findActivity(activityImpl2.getActivityId());
					sameStartTimeNodes.add(sameActivityImpl2);
				} else {
					// 有不相同跳出循环
					break;
				}
			}
			List<PvmTransition> pvmTransitions = activityImpl
					.getOutgoingTransitions();// 取出节点的所有出去的线
			for (PvmTransition pvmTransition : pvmTransitions) {
				// 对所有的线进行遍历
				ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
						.getDestination();
				// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
				if (sameStartTimeNodes.contains(pvmActivityImpl)) {
					highFlows.add(pvmTransition.getId());
				}
			}
		}
		return highFlows;
	}

	/**
	 * 
	 * TODO 在 ACT_HI_ACTINST 表中找到对应流程实例的userTask节点 不包括startEvent
	 */
	private List<HistoricActivityInstance> getHisUserTaskActivityInstanceList(
			String processId) {
		List<HistoricActivityInstance> hisActivityInstanceList = ((HistoricActivityInstanceQuery) engine
				.getHistoryService().createHistoricActivityInstanceQuery()
				.processInstanceId(processId).activityType("userTask")
				./* finished(). */orderByHistoricActivityInstanceEndTime()
				.desc()).list();
		return hisActivityInstanceList;
	}

	/**
	 * TODO 将字符串分割，转换成list
	 */
	private List<String> str2List(String str) {
		List<String> strList = new ArrayList<String>();
		String[] strArr = str.split(",");
		for (String s : strArr) {
			strList.add(s);
		}
		return strList;
	}

	/**
	 * 获得某个流程实例各个节点的审批记录
	 */
	@RequestMapping(value = "getApproveHistory/{processId}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getApproveHistory(
			@PathVariable("processId") String processId) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		List<TaskVO> taskList = new ArrayList<TaskVO>();
		TaskConditionVO taskCondition = new TaskConditionVO();
		List<TaskConditionVO> state = null;
		// 该流程实例的所有节点审批记录
		List<HistoricActivityInstance> hisActInstList = getHisUserTaskActivityInstanceList(processId);
		for (Iterator<HistoricActivityInstance> iterator = hisActInstList
				.iterator(); iterator.hasNext();) {
			// 需要转换成HistoricActivityInstance
			HistoricActivityInstance hai = iterator.next();
			// 如果还没结束则不放里面
			if ("".equals(hai.getEndTime()) || hai.getEndTime() == null) {
				continue;
			}
			TaskVO task = new TaskVO();
			task.setId(hai.getTaskId());
			task.setTitle(hai.getActivityName());
			// 获得审批人名称 。Assignee存放的是审批用户ID，需要转换成用户名
			List<String> notifyList = new ArrayList<String>();
			notifyList.add(hai.getAssignee());
			List<EmployeeVO> userInfo = activitiService.getInfoId(notifyList);
			if (userInfo != null && userInfo.size() > 0) {
				task.setApproveUser(userInfo.get(0).getName());
			}
			// 获取流程节点开始时间
			task.setCreateTime(hai.getStartTime() == null ? "" : TimeUtil
					.dateToString(hai.getStartTime()));
			// 获取流程节点结束时间
			task.setEndTime(hai.getEndTime() == null ? "" : TimeUtil
					.dateToString(hai.getEndTime()));
			// 获取处理意见
			List<Comment> comments = engine.getTaskService().getTaskComments(
					task.getId());
			// List<Comment> comments2 =
			// engine.getTaskService().getProcessInstanceComments(processId);
			if (comments != null && comments.size() > 0) {
				task.setComment(engine.getTaskService()
						.getTaskComments(task.getId()).get(0).getFullMessage());
			}
			if (StringUtils.isBlank(task.getComment())) {
				task.setComment("无");
			}
			// 设置审批状态。1拒绝，2同意
			taskCondition.setTaskId(task.getId());
			state = activitiService.getTaskCondition(taskCondition);
			if (state != null && state.size() > 0) {
				task.setState(state.get(0).getState());
				if (task.getState().equals(0)) {// 如果是发起人节点，则将“审批人”设置为发起人
					task.setApproveUser(getUserName(engine.getHistoryService()
							.createHistoricProcessInstanceQuery()
							.processInstanceId(hai.getProcessInstanceId())
							.singleResult().getStartUserId()));
				}
			}
			taskList.add(task);
		}
		resultMap.put("errcode", 0);
		resultMap.put("errmsg", "获取流程节点的审批记录成功");
		resultMap.put("access_token",null);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("items",taskList);
		data.put("extra",null);
		resultMap.put("data",data);
		return resultMap;
	}

	/**
	 * 会议保障申请单查询 TODO 根据表单ID或者流程ID获取表单
	 */
	@RequestMapping(value = "getForm/{processId}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getForm(@PathVariable("processId") String processId) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			ScheduleFormVO sf = new ScheduleFormVO();
			sf.setProcessId(processId);
			List<ScheduleFormVO> sfList = activitiService.getForm(sf);
			List<ScheduleFormVO> list = new ArrayList<ScheduleFormVO>();
			for (ScheduleFormVO sfv : sfList) {
				changeId2Name(sfv);
				list.add(sfv);
			}
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "获取会议保障申请单成功");
			resultMap.put("access_token",null);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("items",list);
			data.put("extra",null);
			resultMap.put("data",data);
		} catch (Exception e) {
			logger.error("获取失败", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "系统内部异常");
			resultMap.put("access_token",null);
			resultMap.put("data",null);
		}
		return resultMap;
	}

	/**
	 * TODO 获取表单时，将知会人的用户ID转换为用户名
	 */
	private void changeId2Name(ScheduleFormVO sf) throws Exception {
		String[] methodArr = { "CommanderHq", "CommanderOut", "SwitchHq",
				"SwitchOut", "ProblemHq", "ProblemOut", "FeedbackHq",
				"FeedbackOut", "ResponseHq", "ResponseOut", "AssistHq",
				"AssistOut", "PrepareHq", "PrepareOut" };
		String value = null;
		List<String> notifyList = new ArrayList<String>();// 要通知的用户ID列表
		// 设置知会人
		List<EmployeeVO> infoList = null;// 用户信息列表（根据用户ID获取）
		String names = "";
		for (String method : methodArr) {
			value = reflectCall(sf, "get" + method);
			if (StringUtils.isNotBlank(value)) {
				names = "";// 清空
				notifyList.clear();
				notifyList.addAll(str2List(value));
				// 根据用户ID获取用户信息
				infoList = activitiService.getInfoId(notifyList);
				if (infoList != null && infoList.size() > 0) {
					for (EmployeeVO ev : infoList) {
						names += ev.getName() + "、";
					}
				}
				if (StringUtils.isBlank(names)) {
					names = "无";
				} else if (names.endsWith("、")) {
					names = names.substring(0, names.lastIndexOf("、"));
				}
				Method setMethod = sf.getClass().getMethod("set" + method,
						String.class);// 获得属性set方法
				setMethod.invoke(sf, names);
			}
		}
	}

	/**
	 * 通过反射调用bean方法
	 */
	private String reflectCall(Object instance, String methodName)
			throws Exception {
		java.lang.reflect.Method m = instance.getClass().getDeclaredMethod(
				methodName);
		return (String) m.invoke(instance);
	}

	private String getUserName(String loginName) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		if (StringUtils.isBlank(loginName)) {
			return "";
		}
		paramsMap.put("loginName", loginName);
		List<UserVO> list = activitiService.getUserbyLoginName(paramsMap);
		if (list != null && list.size() > 0) {
			return list.get(0).getName();
		}
		return "";
	}

	/**
	 * 
	 * TODO 根据流程ID获取总结表单
	 */
	@RequestMapping(value = "getSumForm", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getSumForm(@RequestBody SummaryFormVO sf) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			List<SummaryFormVO> sfList = activitiService.getSumForm(sf);
			if (sfList.size() < 1) {
				ScheduleFormVO sfv = new ScheduleFormVO();
				sfv.setProcessId(sf.getProcessId());
				List<ScheduleFormVO> sfvList = activitiService.getForm(sfv);
				sfList.add(new SummaryFormVO());
				sfList.get(0).setCreator(sfvList.get(0).getCreator());
				sfList.get(0).setCreateTime(sfvList.get(0).getCreateTime());
				sfList.get(0).setDept(sfvList.get(0).getDept());
				sfList.get(0).setRank(sfvList.get(0).getRank());
				sfList.get(0).setFormNo(sfvList.get(0).getFormNo());
				sfList.get(0).setName(sfvList.get(0).getName());
				sfList.get(0).setScheduleId(sfvList.get(0).getScheduleId());
			}
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "获取总结表单成功");
			resultMap.put("access_token",null);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("items",sfList);
			data.put("extra",null);
			resultMap.put("data",data);
		} catch (Exception e) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "系统内部异常");
			resultMap.put("access_token",null);
			resultMap.put("data",null);
		}
		return resultMap;
	}

	// 同意-获取审批人 333
	/**
	 * 
	 * TODO 获取下一级审批人
	 */
	@RequestMapping(value = "getNextUser", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getNextUser() {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			ConstDataVO cd = new ConstDataVO();
			cd.setValueType(2);
			List<ConstDataVO> sfList = activitiService.getConstData(cd);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "获取总结表单成功");
			resultMap.put("access_token",null);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("items",sfList);
			data.put("extra",null);
			resultMap.put("data",data);
		} catch (Exception e) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "系统内部异常");
			resultMap.put("access_token",null);
			resultMap.put("data",null);
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 审批通过
	 * 
	 * @author 谢程算
	 * @date 2017年5月3日
	 * @version 1.0.0
	 * @param names
	 *            下一级审批人员的用户ID
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "complete/{loginUser}/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> complete(
			@PathVariable("loginUser") String loginUser,
			@PathVariable("userId") String userId,
			@RequestBody ScheduleFormVO sf) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			String url = this.sysConfig.getHgUrl() + String.format(WsConstants.URL_CMS_COMPLETE, loginUser, userId);
			
			Map<String, Object> mapWithoutNull = this.getMapWithoutNull(sf);//将参数去空操作
			
			resultMap = RestTemplateUtil.postForObject(url, mapWithoutNull, Map.class);
			
			/*resultMap = activitiService.complete(sf.getAdvice(), sf.getNames(),
					sf.getTaskId(), sf, loginUser, userId);*/
		} catch (Exception e) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "审批失败，内部异常");
			resultMap.put("access_token",null);
			resultMap.put("data",null);
			logger.error("审批失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 驳回 直接结束流程
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "reject/{loginUser}/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> reject(
			@PathVariable("loginUser") String loginUser,
			@PathVariable("userId") String userId,
			@RequestBody ScheduleFormVO sf) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			String url = this.sysConfig.getHgUrl() + String.format(WsConstants.URL_CMS_REJECT, loginUser, userId);
			
			Map<String, Object> mapWithoutNull = this.getMapWithoutNull(sf);//将参数去空操作
			
			resultMap = RestTemplateUtil.postForObject(url, mapWithoutNull, Map.class);
			
			/*resultMap = activitiService.reject(loginUser, userId,
					sf.getComment(), sf.getTaskId(), sf.getProcessId());*/
		} catch (Exception e) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "拒绝失败，内部异常");
			resultMap.put("access_token",null);
			resultMap.put("data",null);
			logger.error("拒绝失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 获取会议编号
	 * 
	 * @author 谢程算
	 * @date 2017年5月15日
	 * @version 1.0.0
	 * @param sf
	 * @return
	 */
	@RequestMapping(value = "getScheduleId/{industryCode}/{areaCode}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getScheduleId(
			@PathVariable("industryCode") String industryCode,
			@PathVariable("areaCode") String areaCode) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			Map<String, Object> mapId = new LinkedHashMap<String, Object>();
			ArrayList<Map<String, Object>> scheduleid = new ArrayList<Map<String, Object>>();
			Map<String, Object> data = new HashMap<String, Object>();
			mapId.put("scheduleId",RandomUtil.getScheduleNum(industryCode, areaCode));
			scheduleid.add(mapId);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "获取会议编号成功");
			resultMap.put("access_token",null);
			data.put("items",scheduleid);
			data.put("extra",null);
			resultMap.put("data",data);
		} catch (Exception e) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "内部异常");
			resultMap.put("access_token",null);
			resultMap.put("data",null);
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 获取表单编号
	 * 
	 * @author 谢程算
	 * @date 2017年5月16日
	 * @version 1.0.0
	 * @param sf
	 * @return
	 */
	@RequestMapping(value = "getFormNo", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getFormNo() {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			Map<String, Object> mapId = new LinkedHashMap<String, Object>();
			ArrayList<Map<String, Object>> scheduleid = new ArrayList<Map<String, Object>>();
			Map<String, Object> data = new HashMap<String, Object>();
			mapId.put("formNo",RandomUtil.getRandomChar(9));
			scheduleid.add(mapId);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "获取表单编号成功");
			resultMap.put("access_token",null);
			data.put("items",scheduleid);
			data.put("extra",null);
			resultMap.put("data",data);
		} catch (Exception e) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "内部异常");
			resultMap.put("access_token",null);
			resultMap.put("data",null);
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 获取已发事项
	 * 
	 * @author 谢程算
	 * @date 2017年5月4日
	 * @version 1.0.0
	 * @param mav
	 * @param id
	 *            用户ID
	 * @return
	 */
	@RequestMapping("getSentList/{loginName}")
	@ResponseBody
	public ReturnData getSentList(@PathVariable(value = "loginName") String loginName) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			resultMap = activitiService.getSentList(loginName);
			return super.returnResult(0, "查询成功", null, resultMap.get("list"));
		} catch (Exception e) {
			logger.error("获取已建预约失败", e);
			return super.returnError("查询失败");
		}
	}
	
	/**
	 * 
	 * TODO 获取已审、待审、已建列表（简化版，给预约列表跳转提供依据）
	 * @author 谢程算
	 * @date 2017年9月27日  
	 * @version 1.0.0 
	 * @param session
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, List<String>> getProcIdLists(String loginUser) {
		Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
		List<HistoricTaskInstance> doneList = new ArrayList<HistoricTaskInstance>();
		try {
			resultMap.put("done", new ArrayList<String>());
			resultMap.put("todo", new ArrayList<String>());
			resultMap.put("sent", new ArrayList<String>());
			List<EmployeeVO> user = activitiService.getInfo(loginUser);
			if(user == null || user.size() < 1){
				return resultMap;
			}
			// 获取历史数据服务对象
			HistoryService service = engine.getHistoryService();
			// 获取已审列表
			List<String> processIds = new ArrayList<String>();
			
			List<Object> doneListObj = this.jedisDao.getList(CommonConstrant.REDIS_CACHE_TASK_DONE_LIST);//查询缓存
			doneList = (List<HistoricTaskInstance>)(Object)doneListObj;
			if (doneListObj == null || doneListObj.size() <= 0) {
				doneList = service.createHistoricTaskInstanceQuery().finished().list();
			}
			
			for (HistoricTaskInstance task : doneList) {
				if(StringUtils.isBlank(task.getAssignee()) || !task.getAssignee().contains(user.get(0).getId())){
					continue;
				}
				if(!processIds.contains(task.getProcessInstanceId())){
					processIds.add(task.getProcessInstanceId());
				}else{//去除重复数据
					continue;
				}
			}
			resultMap.put("done", processIds);
			// 获取待审列表
			processIds = new ArrayList<String>();
			List<Task> todoList = engine.getTaskService().createTaskQuery().taskCandidateOrAssigned(user.get(0).getId())
					.list();
			for (Task task : todoList) {
				if(!processIds.contains(task.getProcessInstanceId())){
					processIds.add(task.getProcessInstanceId());
				}else{//去除重复数据
					continue;
				}
			}
			resultMap.put("todo", processIds);
			// 获取已建列表
			processIds = new ArrayList<String>();
			List<HistoricProcessInstance> sentList = service
					.createHistoricProcessInstanceQuery().startedBy(user.get(0).getId())
					.list();
			for (HistoricProcessInstance task : sentList) {
				if(!processIds.contains(task.getId())){
					processIds.add(task.getId());
				}else{//去除重复数据
					continue;
				}
			}
			resultMap.put("sent", processIds);
		} catch (Exception e) {
			logger.error("获取已办、待办、已发事项失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * TODO 获取已办事项
	 * 
	 * @author 谢程算
	 * @date 2017年5月4日
	 * @version 1.0.0
	 * @param mav
	 * @param name
	 *            用户名称
	 * @return
	 */
	@RequestMapping(value = "/getDoneList", method = RequestMethod.GET)
	public ReturnData getDoneList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestParam("access_token") String token, String title, String startCreateTime, String endCreateTime) {
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
			
			if (StringUtil.isNotNull(title)) {
				paramsMap.put("title", title);
			}
			
			if (StringUtil.isNotNull(startCreateTime)) {
				paramsMap.put("startCreateTime", startCreateTime);
			}
			
			if (StringUtil.isNotNull(endCreateTime)) {
				paramsMap.put("endCreateTime", endCreateTime);
			}
			
			paramsMap.put("name", user.getLoginName());
			
			return this.activitiService.getDoneList(paramsMap);
		} catch (Exception e) {
			logger.error("获取已办事项失败", e);
			return super.returnError("系统内部异常");
		}
	}
	
	/**
	 * getBaseUserInfo(创建表单时获取填单人员信息)
	 * @param loginName 用户登录名  
	 * @return
	 */
	@RequestMapping(value = "/{loginName}/getBaseUserInfo", method = RequestMethod.GET)
	public ReturnData getBaseUserInfo(@PathVariable("loginName") String loginName) {
		try {
			// 将用户登录名作为参数传至数据库查询用户信息
			List<EmployeeVO> list = this.activitiService.getInfo(loginName);
			if(list != null && list.size() > 0){
				if(list.get(0).getEmpPost() == null){
					list.get(0).setEmpPost("");
				}
				if(list.get(0).getEmpUnit() == null){
					list.get(0).setEmpUnit("");
				}
			}
			return super.returnResult(0, "获取成功", null, list);
		} catch (Exception e) {
			logger.error("ActivitiController ===== getBaseUserInfo ===== 获取填单人员基本信息失败 =====> ", e);
			return super.returnError("获取员工信息失败");
		}
	}
	
	/**
	 * 动态获取OA人员信息树节点
	 * @param originalPath 父级节点的path属性值
	 * @return
	 */
	@RequestMapping(value = "/getDepRoot", method = RequestMethod.GET)
	public ReturnData getDepRoot(@RequestParam(value = "originalPath", required = false)String originalPath) {
		try {
			List<DepartmentVO> list = activitiService.getDepRoot(originalPath);
			
			return super.returnResult(0, "获取成功", null, list);
		} catch (Exception e) {
			logger.error("ActivitiController ===== getDepRoot ===== 动态获取OA人员信息树节点失败 =====> ", e);
			return super.returnError("获取失败");
		}
	}
	
	/**
	 * <pre>
	 * getDepEmployee(获取当前部门节点员工（不包含子节点）)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月17日 上午11:19:18    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月17日 上午11:19:18    
	 * 修改备注： 
	 * @return
	 * </pre>
	 */
	@RequestMapping(value = "/getDepEmployee", method = RequestMethod.GET)
	public ReturnData getDepEmployee(@RequestParam(value = "depId") String depId) {
		try {
			List<EmployeeVO> list = activitiService.getDepEmployee(depId);
			
			return super.returnResult(0, "获取成功", null, list);
		} catch (Exception e) {
			logger.error("ActivitiController ===== getDepEmployee ===== 获取当前部门节点员工（不包含子节点）信息失败 =====> ", e);
			return super.returnError("获取部门节点员工信息失败");
		}
	}
	
	/**
	 * <pre>
	 * getDepEmployees(根据节点信息获取员工信息)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月15日 上午11:25:11    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月15日 上午11:25:11    
	 * 修改备注： 
	 * @param departmentVO
	 * @return
	 * </pre>
	 */
	@RequestMapping("/getDepEmployees")
	@ResponseBody
	public ReturnData getDepEmployees(String depId) {// 参数为当前点击的树节点部门对象
		try {
			List<EmployeeVO> list = activitiService.getDepRootAll(depId);
			
			return super.returnResult(0, "获取成功", null, list);
		} catch (Exception e) {
			logger.error("ActivitiController ===== getDepEmployee ===== 获取当前部门节点员工（包含子节点）信息失败 =====> ", e);
			return super.returnError("获取部门节点员工信息失败");
		}
	}
	
	/**
	 * 
	 * TODO 创建总结表单
	 * 
	 * @author 谢程算
	 * @date 2017年5月3日
	 * @version 1.0.0
	 * @param id
	 * @param mav
	 * @return
	 */
	@RequestMapping("genSumForm")
	@ResponseBody
	public Map<String, Object> genSumForm(@ModelAttribute SummaryFormVO sf, String taskId,
			HttpSession session) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 中文字符解码
			if (StringUtils.isNotBlank(sf.getTitle())) {// 表单标题
				sf.setTitle(URLDecoder.decode(sf.getTitle(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getCreator())) {// 填表人
				sf.setCreator(URLDecoder.decode(sf.getCreator(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getDept())) {// 部门
				sf.setDept(URLDecoder.decode(sf.getDept(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getRank())) {// 岗位职级
				sf.setRank(URLDecoder.decode(sf.getRank(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getName())) {// 会议名称
				sf.setName(URLDecoder.decode(sf.getName(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getIncharge())) {// 负责人
				sf.setIncharge(URLDecoder.decode(sf.getIncharge(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getStartUnit())) {// 会议发起单位
				sf.setStartUnit(URLDecoder.decode(sf.getStartUnit(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getMainDept())) {// 主要与会部门
				sf.setMainDept(URLDecoder.decode(sf.getMainDept(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getMainLeader())) {// 主要与会领导
				sf.setMainLeader(URLDecoder.decode(sf.getMainLeader(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getOptName())) {// 会议软件操作人
				sf.setOptName(URLDecoder.decode(sf.getOptName(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getFuncName())) {// 相关功能实现人员
				sf.setFuncName(URLDecoder.decode(sf.getFuncName(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getRespName())) {// 群消息响应反馈人员
				sf.setRespName(URLDecoder.decode(sf.getRespName(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getMonName())) {// 流量监控人员
				sf.setMonName(URLDecoder.decode(sf.getMonName(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getProbName())) {// 问题收集及整理人员
				sf.setProbName(URLDecoder.decode(sf.getProbName(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getSumName())) {// 会议总结人员
				sf.setSumName(URLDecoder.decode(sf.getSumName(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getSummary())) {// 问题总结
				sf.setSummary(URLDecoder.decode(sf.getSummary(), "utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getCustomerAdvice())) {// 客户意见
				sf.setCustomerAdvice(URLDecoder.decode(sf.getCustomerAdvice(),
						"utf-8"));
			}
			if (StringUtils.isNotBlank(sf.getRelated())) {// 与我司相关内容
				sf.setRelated(URLDecoder.decode(sf.getRelated(), "utf-8"));
			}
			resultMap = activitiService.genSumForm(sf, taskId);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("提交失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 获取所有已通过审批的本平台跨域会议
	 * @param processId 流程ID
	 * @return
	 */
	@RequestMapping(value = "/processId/{processId}/getCrossDisciplinaryList", method = RequestMethod.GET)
	public ReturnData getCrossDisciplinaryList(@PathVariable("processId") String processId) {
		Map<String, Object> regionBandwidthMap = new HashMap<String, Object>();//点位覆盖信息Map，key对应前端的点位数值，value对应所剩带宽
		List<Map<String, Object>> regionBandwidthMapList = new ArrayList<Map<String, Object>>();//点位覆盖信息List，key对应前端的点位数值，value对应所剩带宽
		Map<String, Object> paramsMap = new HashMap<String, Object>();//查询已通过审批的本平台跨域会议条件
		List<ScheduleFormVO> crossDisciplinaryList = new ArrayList<ScheduleFormVO>();//所有已通过审批的本平台跨域会议
		List<ScheduleFormVO> scheduleResultList = new ArrayList<ScheduleFormVO>();//返回给前端的会议列表
		ScheduleFormVO scheduleFormResult = null;//返回给前端的会议信息
		try {
			ScheduleFormVO scheduleFormParams = new ScheduleFormVO();//查询条件
			scheduleFormParams.setProcessId(processId);
			List<ScheduleFormVO> currentScheduleList = this.activitiService.getForm(scheduleFormParams);//通过流程ID查询会议信息,即当前点击的会议
			if (currentScheduleList != null && currentScheduleList.size() > 0) {
				//将知会人的ID(第三个节点)转换为对应的用户名 Start
				for (ScheduleFormVO scheduleForm : currentScheduleList) {
					this.changeId2Name(scheduleForm);
				}
				//将知会人的ID(第三个节点)转换为对应的用户名 End
				
				scheduleFormResult = currentScheduleList.get(0);//当前会议信息
				paramsMap.put("startTime", scheduleFormResult.getStartTime());
				paramsMap.put("endTime", scheduleFormResult.getEndTime());
				
				if (StringUtil.isNotNull(scheduleFormResult.getDevices())) {
					String[] deviceArr = scheduleFormResult.getDevices().split(",");//以逗号分隔的会议覆盖点位
					if (deviceArr != null && deviceArr.length > 0) {
						for (int i = 0; i < deviceArr.length; i++) {
							paramsMap.put("coverage",deviceArr[i]);//点位条件
							crossDisciplinaryList = this.activitiService.getCrossDisciplinaryList(paramsMap);//获取所有已通过审批的本平台跨域会议列表
							
							if (scheduleResultList == null || scheduleResultList.size() == 0) {
								scheduleResultList.addAll(crossDisciplinaryList);
							} else {
								for (ScheduleFormVO crossDisciplinary : crossDisciplinaryList) {//循环本平台跨域会议
									int h = 0;
									for (ScheduleFormVO currentSchedule : scheduleResultList) {//当前点击的会议
										if (currentSchedule.getUuid().equals(crossDisciplinary.getUuid())) {
											h++;
										}
									}
									if (h == 0) {
										scheduleResultList.add(crossDisciplinary);
									}
								}
							}
							
							BandWidthVO bandwidth = new BandWidthVO();
							bandwidth.setId((deviceArr[i]));
							List<BandWidthVO> bandwidthList = sysConfigService.getBandwidth(bandwidth);//通过点位覆盖的ID查询国干带宽
							Integer bandwidthValue = bandwidthList.get(0).getBandwidth();//国干带宽
							regionBandwidthMap.put(deviceArr[i], bandwidthValue);
							for (ScheduleFormVO scheduleVO : crossDisciplinaryList) {						
								String[] devices = scheduleVO.getDevices().split(",");
								for (int j = 0; j < devices.length; j++) {
									if (devices[j].equals(deviceArr[i])) {
										int  bw = (Integer) regionBandwidthMap.get(deviceArr[i]);
										regionBandwidthMap.put(deviceArr[i], bw - (Double.valueOf(scheduleVO.getMainBw()).intValue()+Double.valueOf(scheduleVO.getAuxiliaryBw()).intValue()));
									}
								}
							}
						}
					}
				}
				
				if (regionBandwidthMap != null && regionBandwidthMap.size() > 0) {
					Iterator<Entry<String, Object>> iter = regionBandwidthMap.entrySet().iterator();//获得Map的Iterator
					while (iter.hasNext()) {
						Map<String, Object> regionBandwidthResultMap = new HashMap<String, Object>();
						Entry<String, Object> entry = (Entry<String, Object>)iter.next();
						regionBandwidthResultMap.put("regionId", entry.getKey());
						regionBandwidthResultMap.put("bandwidthValue", entry.getValue());
						regionBandwidthMapList.add(regionBandwidthResultMap);
					}
					
				}
				
				scheduleFormResult.setRegionMap(regionBandwidthMapList);
			}
			return super.returnResult(0, "获取成功", null, scheduleResultList, scheduleFormResult);
		} catch (BusinessException e) {
			logger.error("ActivitiController ===== getCrossDisciplinaryList ===== 获取失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			logger.error("ActivitiController ===== getCrossDisciplinaryList ===== 获取失败 =====> ", e);
			return super.returnError("获取失败");
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
}
