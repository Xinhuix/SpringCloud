package com.visionvera.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DepartmentVO;
import com.visionvera.bean.cms.EmployeeVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.SmsVO;
import com.visionvera.bean.cms.SummaryFormVO;
import com.visionvera.bean.cms.TaskConditionVO;
import com.visionvera.bean.cms.TaskVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.operation.ActivitiDao;
import com.visionvera.dao.operation.ConstDataDao;
import com.visionvera.dao.operation.ScheduleDao;
import com.visionvera.dao.operation.SmsAgent;
import com.visionvera.dao.operation.SysConfigDao;
import com.visionvera.dao.operation.TaskConditionDao;
import com.visionvera.dao.operation.UserDao;
import com.visionvera.service.ActivitiService;
import com.visionvera.util.TimeUtil;

/**
 * 中心数据库的业务
 *
 */
@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class ActivitiServiceImpl implements ActivitiService {
	private static final Logger logger = LogManager.getLogger(ActivitiServiceImpl.class);
	@Resource
	private ActivitiDao activitiDao;
	@Resource
	ProcessEngine engine;
	@Resource
	private UserDao userDao;
	@Resource
	private TaskConditionDao taskConditionDao;
	@Resource
	private ConstDataDao constDataDao; 
	@Resource
	private ScheduleDao scheduleDao; 
	@Resource
	private SysConfigDao sysConfigDao;
	
	@Resource
	private JRedisDao jedisDao;
	
	public Map<String, Object> getTodoList(String name) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		resultMap.put("errcode", 0);
		resultMap.put("errmsg", "获取待办事项成功");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
		try {
			List<EmployeeVO> user = activitiDao.getInfo(name);
			if(user == null || user.size() < 1){
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "该用户不存在");
				return resultMap;
			}
			// 获取任务服务对象
			TaskService service = engine.getTaskService();
			// 根据接受人获取该用户的任务
			List<Task> list = service.createTaskQuery().taskCandidateOrAssigned(user.get(0).getId())
					.orderByTaskCreateTime().desc().list();
			List<TaskVO> tasks = new ArrayList<TaskVO>();
			TaskVO t = null;
			//遍历任务节点
			List<String> procIdList = new ArrayList<String>();
			for (Task task : list) {
				t = new TaskVO();
				t.setProcessId(task.getProcessInstanceId());// 流程ID
				t.setId(task.getId());// 任务ID
				t.setName(task.getName());// 任务名
				t.setCreateTime(task.getCreateTime() == null ? "" : TimeUtil
						.dateToString(task.getCreateTime()));
				procIdList.add(t.getProcessId());
				//设置是否需要选择下一节点审批人
				if(task.getTaskDefinitionKey().equals("usertask2")){//第一个审批节点
					t.setChooseNext(GlobalConstants.CHOOSE_NEXT_YES);//需要选择下一节点审批人
				}else{
					t.setChooseNext(GlobalConstants.CHOOSE_NEXT_NO);//不需要选择下一节点审批人
				}
				//设置表单编辑权限
				if(task.getTaskDefinitionKey().equals("usertask3")){//第二个审批节点（网络部）
					t.setEditForm(GlobalConstants.EDIT_FORM_APPLY);//可以编辑申请表的“网络部填写”部分
				}else if(task.getTaskDefinitionKey().equals("usertask5")){//会议总结 节点
					t.setEditForm(GlobalConstants.EDIT_FORM_SUMMARY);//可以编辑总结表
				}else{
					t.setEditForm(GlobalConstants.EDIT_FORM_NO);//只能查看表单
				}
				tasks.add(t);
			}
			//批量获取表单数据（减少查库次数，提高效率）
			List<ScheduleFormVO> sfList = activitiDao.getForms(procIdList);
			//数据重组，方便后续使用
			Map<String, ScheduleFormVO> sfMap = new HashMap<String, ScheduleFormVO>();
			for(ScheduleFormVO sfv : sfList){
				sfMap.put(sfv.getProcessId(), sfv);
			}
			Iterator<TaskVO> it = tasks.iterator();
			while(it.hasNext()){
				TaskVO task = it.next();
			    if(sfMap.get(task.getProcessId()) == null){
			    	it.remove();
					continue;
				}
				task.setStartUser(sfMap.get(task.getProcessId()).getCreator());
				task.setFormId(sfMap.get(task.getProcessId()).getFormNo());// 表单编号
				task.setTitle(sfMap.get(task.getProcessId()).getTitle());// 表单标题
				//设置审批状态
				task.setState(sfMap.get(task.getProcessId()).getState());
			}
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("items",tasks);
			data.put("extra",null);
			resultMap.put("data",data);
		} catch (Exception e) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "获取待办事项失败");
			logger.error("获取待办事项失败", e);
		}
		return resultMap;
	}
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getForm
	 * @Description: TODO 获取预约表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	public List<ScheduleFormVO> getForm(ScheduleFormVO sf) {
		return activitiDao.getForm(sf);
	}
	
	/**
	 * <pre>
	 * getInfoId(根据id获取员工信息)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月17日 上午9:56:07    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月17日 上午9:56:07    
	 * 修改备注： 
	 * @return
	 * </pre>
	 */
	public List<EmployeeVO> getInfoId(List<String> idsList) {
		return activitiDao.getInfoId(idsList);
	}

	@Override
	public List<UserVO> getUserbyLoginName(Map<String, Object> paramsMap) {

		return userDao.getUserbyLoginName(paramsMap);
	}

	@Override
	public List<TaskConditionVO> getTaskCondition(TaskConditionVO taskCondition) {
		// TODO Auto-generated method stub
	    return taskConditionDao.getTaskCondition(taskCondition);
	}
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getSumForm
	 * @Description: TODO 获取总结表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	@Override
	public List<SummaryFormVO> getSumForm(SummaryFormVO sf) {
		return activitiDao.getSumForm(sf);
	}

	@Override
	public List<ConstDataVO> getConstData(ConstDataVO cd) {
		return constDataDao.getConstData(cd);
	}
	/**
	 * 
	 * TODO 预约审批-同意
	 * @author 谢程算
	 * @date 2017年9月12日  
	 * @version 1.0.0 
	 * @param session
	 * @param comment
	 * @param taskId
	 * @return
	 */
	@Override
	public Map<String, Object> complete(String advice,
			String names, String taskId, ScheduleFormVO sf, String loginUser, String loginUserId) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		Map<String, Object> variables = new HashMap<String, Object>();
		resultMap.put("errcode", 0);
		resultMap.put("errmsg", "审批通过");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
		try {
			if(StringUtils.isBlank(taskId)){ //判断是否已经有预约占用主席点位
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "审批失败，参数错误");
				logger.error("审批预约参数错误，taskId为空");
				return resultMap;
			}
			List<ScheduleFormVO> sfv = activitiDao.getForm(sf);//从数据库中获取表单信息
			if(sfv == null || sfv.size() < 1){ //预约不存在
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "预约不存在");
				return resultMap;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("masterNo", sfv.get(0).getChairman());
			map.put("startTime", sfv.get(0).getStartTime());
			map.put("endTime", sfv.get(0).getEndTime());
			map.put("scheduleId", sfv.get(0).getUuid());
			if(scheduleDao.chkConflictByMaster(map) > 0){ //判断是否已经有预约占用主席点位
				resultMap.put("errcode", 1);
				resultMap.put("errmsg","主席点位冲突");
				return resultMap;
			}
			if (StringUtils.isNotBlank(names)) {//第一节点
				// 设置审批人ID
				sf.setAccessorId(loginUserId);
				sf.setAccessorTime("accessorTime");
			}
			advice = (advice == null ? "无" : advice);
			TaskService taskService = engine.getTaskService();
			// 添加批注信息
			TaskEntity task = (TaskEntity) taskService.createTaskQuery()
					.taskId(taskId).singleResult();
			String processId = task.getProcessInstanceId();
			if(StringUtils.isBlank(sf.getProcessId())){//设置审批流程ID信息
				sf.setProcessId(processId);
			}
			taskService.addComment(taskId, processId, advice);
			if (StringUtils.isBlank(names)) {
				if (task.getTaskDefinitionKey().equals("usertask3")) {// 当前节点是网络部
					// 执行审批操作
//					String userId = engine.getHistoryService()
//							.createHistoricProcessInstanceQuery()
//							.processInstanceId(processId).singleResult()
//							.getStartUserId();
					// 使流程直接跳到会议总结
					variables.put("msg", "同意");
//					variables.put("inputUser", userId);
					// 将表单状态修改为“待总结”
					sf.setState(GlobalConstants.COMPLETE_SUMMARY);//5：待总结
					activitiDao.updateForm(sf);
//					taskService.complete(taskId);
					// 通知知会人、发起人流程已审批完成
					String phones = getNoticeInfo(sf, variables);//要知会的人的手机号
					// 执行审批流程
					taskService.complete(taskId, variables);
//					String msg="0";
//					msgMap.put(names, msg);
//					linkedBlockingQueue.put(msgMap);
					// 知会节点自动完成审批
					autoComplete(processId, variables);
					// 发送短信
					if(StringUtils.isNotBlank(phones)){
						phones = phones.substring(0, phones.lastIndexOf(","));
//						int ret = SmsAgent.post(phones, WsConstants.SMS_SIGNATURE + "会议保障申请单[" + sfv.get(0).getTitle() + "]已通过审批。");
						new SmsAgent(phones, WsConstants.SMS_SIGNATURE + "级别为" + sfv.get(0).getScheduleLevel() +"的会议保障申请单[" + sfv.get(0).getTitle() + "]已通过审批。").start();
//						logger.error("第二审批节点发送短信，手机号：" + phones + "，返回值：" + ret);
					}
				} else {// 当前节点是会议总结节点
					// 将表单状态修改为“已完成”
					sf.setState(GlobalConstants.COMPLETE_END);//4：流程结束
					activitiDao.updateForm(sf);
					// 将反馈结果添加到数据库 并提交
					if (task.getTaskDefinitionKey().equals("usertask5")) {
						engine.getRuntimeService().setVariables(processId,
								variables);
						taskService.complete(taskId, variables);
					}
				}

			} else {// 第一审批人节点
				List<ScheduleFormVO> list = activitiDao.getIsApproved(sf);
				if(list == null || list.size() < 1){
					resultMap.put("errcode", 1);
					resultMap.put("errmsg","预约不存在");
					return resultMap;
				}
				if(list.get(0).getState() != null && list.get(0).getState().equals(5)){//如果是待总结节点，则不提示审批状况
					
				}else if(StringUtils.isNotBlank(list.get(0).getAccessorId())){//已经被审批
					resultMap.put("errcode", 1);
					resultMap.put("errmsg","预约已被" + list.get(0).getAccessorName() + "审批");
					return resultMap;
				}
//				if (task.getTaskDefinitionKey().equals("usertask2")) {
				// 将表单状态修改为“审批中”
				sf.setState(GlobalConstants.COMPLETE_AGREE);// 2:审批中
//				activitiDao.updateForm(sf);
				// 认领该任务（设置该节点的审批人为自己，否则流程图取不到该节点审批人）
				List<EmployeeVO> user = activitiDao.getInfo(loginUser);
				if (user == null || user.size() < 1) {
					resultMap.put("errcode", 1);
					resultMap.put("errmsg","该用户不存在");
					return resultMap;
				}
				engine.getTaskService()
						.setAssignee(taskId, user.get(0).getId());
				// 设置第二审核人
				variables.put("msg", "同意");
				variables.put("inputUser", names);

				// 添加批注信息
				taskService.addComment(taskId, processId, advice);
				engine.getRuntimeService().setVariables(processId, variables);
				// 设置节点审批状态为同意
				taskService.complete(taskId, variables);
				// 将表单状态修改为“待总结”
				sf.setStatus(2);// 通过
				// 设置审批人ID
				activitiDao.updateForm(sf);
				//将审批消息推送给运维平台
//				List<ScheduleFormVO> schList = activitiDao.getForm(sf);
//				InformVO inform = new InformVO();
//				inform.setInformTitle(schList.get(0).getTitle());
//				inform.setInformCreatorId(schList.get(0).getTitle());
//				inform.setInformType(1);//会议固定为1
//				Integer createType = schList.get(0).getCreateType();
//				//创建类型，1：pamir；2：会易通；3 Gis; 4 会控助手；5 调度服务控制的会议 0 会管
//				if(createType == null){
//					inform.setInformDetailType(0);
//				}else{
//					switch(schList.get(0).getCreateType()){
//					case 1:
//						inform.setInformDetailType(1);
//						break;
//					case 2:
//						inform.setInformDetailType(2);
//						break;
//					case 3:
//						inform.setInformDetailType(3);
//						break;
//					case 4:
//						inform.setInformDetailType(4);
//						break;
//					}
//				}
//				inform.setInformStatus(2);//创建预约通知状态:1预约中，2审批通过，3审批驳回4:其他
//				inform.setInformCreateTime(new Date());
//				inform.setInformRank(schList.get(0).getScheduleLevel());
//				DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//				inform.setInformStartTime(format.parse(schList.get(0).getStartTime()));
//				inform.setInformEndTime(format.parse(schList.get(0).getEndTime()));
//				//根据主席查询主席 regionId与主席地点
//				args.put("devId", schList.get(0).getChairman());
//				List<RegionVO> region = scheduleDao.getRegionInfo(args);
//				args.clear();
//				inform.setInformRegionbids(region.get(0).getId());
//				inform.setInformSource(region.get(0).getName());
//				map.clear();
//				map.put("processId", sf.getProcessId());
//				List<ScheduleFormVO> sch = scheduleDao.getSchedulesInUUidList(map);
//				inform.setInformContent(region.get(0).getName()+sch.get(0).getName()+"会管服务端");
//				List<InformVO> inList=new ArrayList<InformVO>();
//				Map<String ,Object> rt = new HashMap<String,Object>();
//				rt = Util.getBaseUrl(filePath);
//				inform.setRegisterId((String) rt.get("registerId"));
//				inList.add(inform);
//				args.put("data", inList);
				
//				RestClient.post((String) rt.get("slweomsServerUrl")+WsConstants.INFORMATION+WsConstants.ADDINFORMATION, args,WsConstants.INFORMATIONPARAM);
//				// 推送消息
//				List<String> idsList = new ArrayList<String>();
//				String[] idsSplit = names.split(",");
//				for (String string : idsSplit) {
//					idsList.add(string);
//				}
//				List<String> userIdList = activitiDao
//						.getUserIdByOaId(idsList);
//				for (String id : userIdList) {
//					WebSocketPushMessage.sendToUser(id, "0");// 有新的预约消息
//				}
				// 通知发起人审批完成
				List<String> notifyList = new ArrayList<String>();
				String userId = engine.getHistoryService()
						.createHistoricProcessInstanceQuery()
						.processInstanceId(processId).singleResult()
						.getStartUserId();
				notifyList.add(userId);
				List<EmployeeVO> infoList = activitiDao.getInfoId(notifyList);
				if(infoList != null & infoList.size() > 0){
					if(StringUtils.isNotBlank(infoList.get(0).getPhoneNumber())){
//						int ret = SmsAgent.post(infoList.get(0).getPhoneNumber(), WsConstants.SMS_SIGNATURE + "会议保障申请单[" + sfv.get(0).getTitle() + "]已通过审批。");
						new SmsAgent(infoList.get(0).getPhoneNumber(), WsConstants.SMS_SIGNATURE + "级别为" + sfv.get(0).getScheduleLevel() +"的会议保障申请单[" + sfv.get(0).getTitle() + "]已通过审批。").start();
//						logger.error("第一审批节点给发起人发送短信，手机号：" + infoList.get(0).getPhoneNumber() + "，返回值：" + ret);
					}
				}
				// 发短信通知下一级审批人
				notifyList.clear();
				notifyList.add(names);
				//根据用户ID获取用户信息
				infoList = activitiDao.getInfoId(notifyList);
				if(infoList != null && infoList.size() > 0){
					if(StringUtils.isNotBlank(infoList.get(0).getPhoneNumber())){
//						int ret = SmsAgent.post(infoList.get(0).getPhoneNumber(), WsConstants.SMS_SIGNATURE + "您有一个会议保障申请单[" + sfv.get(0).getTitle() + "]需要审批，请登录会管进行审批操作。");
						new SmsAgent(infoList.get(0).getPhoneNumber(), WsConstants.SMS_SIGNATURE + "您有一个级别为" + sfv.get(0).getScheduleLevel() +"的会议保障申请单[" + sfv.get(0).getTitle() + "]需要审批，请登录会管进行审批操作。").start();
//						logger.error("第一审批节点给下级审批人发送短信，手机号：" + infoList.get(0).getPhoneNumber() + "，返回值：" + ret);
					}
				}
			}
			//设置任务（节点）审批状态为“同意”-给历史审批记录展示用
			TaskConditionVO taskCondition = new TaskConditionVO();
			taskCondition.setTaskId(taskId);
			taskCondition.setState(GlobalConstants.COMPLETE_AGREE);
			taskConditionDao.addTaskCondition(taskCondition);
		} catch (Exception e) {
			logger.error("审批失败", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg","审批失败，内部异常");
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}
	/**
	 * 
	 * TODO 获取知会人的信息
	 * @author 谢程算
	 * @date 2017年5月22日  
	 * @version 1.0.0 
	 * @param sf
	 * @param userId 
	 * @param variables 
	 * @param notifyList
	 * @param infoList
	 * @return
	 * @throws Exception
	 */
	private String getNoticeInfo(ScheduleFormVO sf/*, String userId*/, Map<String, Object> variables) throws Exception{
		String[] methodArr = {"getCommanderHq","getCommanderOut","getSwitchHq","getSwitchOut",
				"getProblemHq","getProblemOut","getFeedbackHq","getFeedbackOut","getResponseHq",
				"getResponseOut","getAssistHq","getAssistOut","getPrepareHq","getPrepareOut"};
		String value = null;
		List<String> notifyList = new ArrayList<String>();//要通知的用户ID列表
		//设置知会人
		String phones = "";//要知会的人的手机号
		List<EmployeeVO> infoList = null;//用户信息列表（根据用户ID获取）
		for(String method : methodArr){
			value = reflectCall(sf, method);
			if(StringUtils.isNotBlank(value)){
				variables.put(method, value);// 设置知会节点的审批人
				notifyList.clear();
				notifyList.addAll(str2List(value));
				//根据用户ID获取用户信息
				infoList = activitiDao.getInfoId(notifyList);
				for(EmployeeVO info : infoList){
					if(StringUtils.isNotBlank(info.getPhoneNumber())
							&& phones.indexOf(info.getPhoneNumber()) < 0){
						phones += info.getPhoneNumber() + ",";
					}
				}
			}else{
				variables.put(method, "");// 设置知会节点的审批人
			}
		}
		
		// 除了要知会的人之外，还要通知发起人
//		notifyList.clear();
//		notifyList.add(userId);
//		infoList = activitiDao.getInfoId(notifyList);
//		if(infoList != null & infoList.size() > 0){
//			if(StringUtils.isNotBlank(infoList.get(0).getPhoneNumber())
//					&& phones.indexOf(infoList.get(0).getPhoneNumber()) < 0){
//				phones += infoList.get(0).getPhoneNumber() + ",";
//			}
//		}
		// 还要通知三位固定的知会人
		ConstDataVO cd = new ConstDataVO();
		cd.setValueType(3); //固定知会人
		List<ConstDataVO> userInfo = constDataDao.getConstData(cd);
		List<String> ids = new ArrayList<String>();
		for(ConstDataVO info : userInfo){
			if(! ids.contains(info.getConstId())){
				ids.add(info.getConstId());
			}
		}
		String tmpId = null;
		variables.put("notify1", "");// 设置固定知会节点的审批人
		variables.put("notify2", "");// 设置固定知会节点的审批人
		variables.put("notify3", "");// 设置固定知会节点的审批人
		for(int i=1; i<=userInfo.size(); i++){
			tmpId = userInfo.get(i-1).getConstId();
			if(! ids.contains(tmpId)){
				ids.add(tmpId);
			}
			if(i <= 3){//只支持设置3个固定知会人
				variables.put("notify"+i, tmpId);// 设置固定知会节点的审批人
			}
		}
		infoList = activitiDao.getInfoId(ids);
		for(EmployeeVO info :infoList){
			if(StringUtils.isNotBlank(info.getPhoneNumber())
					&& phones.indexOf(info.getPhoneNumber()) < 0){
				phones += info.getPhoneNumber() + ",";
			}
		}
		return phones;
	}
	/**
	 * 通过反射调用bean方法
	 */
	private String reflectCall(Object instance, String methodName) throws Exception {
		java.lang.reflect.Method m = instance.getClass().getDeclaredMethod(
				methodName);
		return (String) m.invoke(instance);
	}
	/**
	 * TODO 将字符串分割，转换成list
	 * @author 谢程算
	 * @date 2017年5月22日  
	 * @version 1.0.0 
	 * @param str
	 * @return
	 */
	private List<String> str2List(String str){
		List<String> strList = new ArrayList<String>();
		String[] strArr = str.split(",");
		for(String s : strArr){
			strList.add(s);
		}
		return strList;
	}
	/**
	 * 
	 * TODO 知会节点自动完成审批
	 * @author 谢程算
	 * @date 2017年5月25日  
	 * @version 1.0.0 
	 * @param sf
	 * @param processId 
	 * @param variables 
	 * @return
	 * @throws Exception
	 */
	private void autoComplete(String processId, Map<String, Object> variables) {
//		Map<String, String> msgMap = new HashMap<String, String>();//缓存
		String[] taskIdArr = { "receivetask15", "receivetask16",
				"receivetask17", "receivetask1", "receivetask2",
				"receivetask3", "receivetask4", "receivetask5", "receivetask6",
				"receivetask7", "receivetask8", "receivetask9",
				"receivetask10", "receivetask11", "receivetask12",
				"receivetask13", "receivetask14" };
		for (String taskId : taskIdArr) {
			// 查询执行对象表,使用流程实例ID和当前活动的名称（receivetask1）
			Task task = engine.getTaskService().createTaskQuery()
					.processInstanceId(processId).taskDefinitionKey(taskId)
					.singleResult();
			// 执行流程
			if(taskId.equals("receivetask14")){
				// 执行审批操作
				String userId = engine.getHistoryService()
						.createHistoricProcessInstanceQuery()
						.processInstanceId(processId).singleResult()
						.getStartUserId();
				// 使流程直接跳到会议总结
				variables.put("msg", "同意");
				variables.put("inputUser", userId);
				engine.getTaskService().complete(task.getId(), variables);//将发起人设置为总结人
				/*//推送消息
				List<String> idsList = new ArrayList<String>();
				idsList.add(userId);
				List<String> userIdList = activitiDao
						.getUserIdByOaId(idsList);
				for (String id : userIdList) {
					WebSocketPushMessage.sendToUser(id, "1");// 审批通过
					// msgMap.put(id, msg);
					// try {
					// linkedBlockingQueue.put(msgMap);
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				}*/
			}else if(taskId.equals("receivetask17")){
				// 使流程直接跳到会议总结
				variables.put("msg", "同意");
				engine.getTaskService().complete(task.getId(), variables);//设置知会人（14人）
			}else{
				engine.getTaskService().complete(task.getId());
			}
		}
	}
	/**
	 * 
	 * @Title: getSms 
	 * @Description: 获取服务配置
	 * @param @return  参数说明 
	 * @return SmsVO    返回类型 
	 * @throws
	 */
    @Override
	public  SmsVO getSms() {
		return sysConfigDao.getSms();
	}
	/**
	 * 
	 * TODO 预约审批-拒绝
	 * @author 谢程算
	 * @date 2017年9月12日  
	 * @version 1.0.0 
	 * @param session
	 * @param comment
	 * @param taskId
	 * @return
	 */
    @Override
	public Map<String, Object> reject(String loginUser, String loginUserId, String comment, String taskId, String processId) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		resultMap.put("errcode", 0);
		resultMap.put("errmsg", "审批拒绝成功");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
		try {
			ScheduleFormVO sf = new ScheduleFormVO();
			sf.setProcessId(processId);
			List<ScheduleFormVO> list = activitiDao.getIsApproved(sf);
			if(list == null || list.size() < 1){
				resultMap.put("errcode", 1);
				resultMap.put("errmsg","预约不存在");
				return resultMap;
			}
			if(StringUtils.isNotBlank(list.get(0).getAccessorId())){//已被审批
				resultMap.put("errcode", 1);
				resultMap.put("errmsg","预约已被" + list.get(0).getAccessorName() + "审批");
				return resultMap;
			}
			TaskService taskService = engine.getTaskService();
			comment = comment == null ? "无" : comment;
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("msg", "拒绝");
			// 认领该任务（设置该节点的审批人为自己，否则流程图取不到该节点审批人）
			List<EmployeeVO> user = activitiDao.getInfo(loginUser);
			if (user == null || user.size() < 1) {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg","该用户不存在");
				return resultMap;
			}
			engine.getTaskService()
					.setAssignee(taskId, user.get(0).getId());
			// 添加批注信息
			taskService.addComment(taskId, processId, comment);
			// 添加流程变量
			engine.getRuntimeService().setVariables(processId, variables);
			taskService.complete(taskId, variables);
			//设置节点审批状态为“拒绝”
			TaskConditionVO taskCondition = new TaskConditionVO();
			taskCondition.setTaskId(taskId);
			taskCondition.setState(GlobalConstants.COMPLETE_REJECT);
			taskConditionDao.addTaskCondition(taskCondition);
			// 通知发起人申请被驳回
			String userId = engine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult().getStartUserId();
			List<String> notifyList = new ArrayList<String>();
			notifyList.add(userId);
			List<EmployeeVO> infoList = activitiDao.getInfoId(notifyList);
			if(infoList != null && infoList.size() > 0){
				//根据processId获取表单信息
				list = activitiDao.getForm(sf);
				if(list != null && list.size() > 0){
					// 发送短信
					if(StringUtils.isNotBlank(infoList.get(0).getPhoneNumber())){
						List<EmployeeVO> userInfo = activitiDao.getInfo(loginUser);
//						int ret = 0;
						if(userInfo != null && userInfo.size() > 0){
							new SmsAgent(infoList.get(0).getPhoneNumber(), WsConstants.SMS_SIGNATURE + "您的会议保障申请单[" + list.get(0).getTitle() + "]已被" + userInfo.get(0).getName() +"拒绝.理由：" + comment + "。").start();
						}else{
							new SmsAgent(infoList.get(0).getPhoneNumber(), WsConstants.SMS_SIGNATURE + "您的会议保障申请单[" + list.get(0).getTitle() + "]已被拒绝.理由：" + comment + "。").start();
						}
//						logger.error("驳回发送短信，手机号：" + infoList.get(0).getPhoneNumber() + "，返回值：" + ret);
					}
					// 将表单状态修改为拒绝
					sf.setStatus(3);//驳回
					sf.setState(GlobalConstants.COMPLETE_REJECT);//1:拒绝
					// 设置审批人ID
					sf.setAccessorId(loginUserId);//设置审批人
					sf.setAccessorTime("accessorTime");
					activitiDao.updateForm(sf);
					
					//将审批消息推送给运维平台
//					InformVO inform = new InformVO();
//					List<ScheduleFormVO> schList = activitiDao.getForm(sf);
//					inform.setInformTitle(schList.get(0).getTitle());
//					inform.setInformCreatorId(schList.get(0).getTitle());
//					inform.setInformType(1);//会议固定为1
//					Integer createType = schList.get(0).getCreateType();
//					//创建类型，1：pamir；2：会易通；3 Gis; 4 会控助手；5 调度服务控制的会议0 会管
//					if(createType == null){
//						inform.setInformDetailType(0);
//					}else{
//						switch(schList.get(0).getCreateType()){
//						case 1:
//							inform.setInformDetailType(1);//此接口为会管平台接口默认为2， 1为帕米尔创建预约 3 手机端创建
//							break;
//						case 2:
//							inform.setInformDetailType(2);//此接口为会管平台接口默认为2， 1为帕米尔创建预约 3 手机端创建
//							break;
//						case 3:
//							inform.setInformDetailType(3);//此接口为会管平台接口默认为2， 1为帕米尔创建预约 3 手机端创建4、其他
//							break;
//						case 4:
//							inform.setInformDetailType(4);//此接口为会管平台接口默认为2， 1为帕米尔创建预约 3 手机端创建
//							break;
//						}
//					}
//					inform.setInformStatus(3);//创建预约通知状态:1预约中，2审批通过，3审批驳回4:其他
//					inform.setInformCreateTime(new Date());
//					inform.setInformRank(schList.get(0).getScheduleLevel());
//					DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//					inform.setInformStartTime(format.parse(schList.get(0).getStartTime()));
//					inform.setInformEndTime(format.parse(schList.get(0).getEndTime()));
//					//根据主席查询主席 regionId与主席地点
//					args.put("devId", schList.get(0).getChairman());
//					List<RegionVO> region = scheduleDao.getRegionInfo(args);
//					inform.setInformRegionbids(region.get(0).getId());
//					inform.setInformSource(region.get(0).getName());
//					args.put("processId", sf.getProcessId());
//					List<ScheduleFormVO> sch = scheduleDao.getSchedulesInUUidList(args);
//					inform.setInformContent(region.get(0).getName()+sf.getName()+"会管服务端");
//					List<InformVO> inList=new ArrayList<InformVO>();
//					inList.add(inform);
//					args.clear();
//					args.put("data", inList);
//					Map<String ,Object> rt = new HashMap<String,Object>();
//					rt = Util.getBaseUrl(filePath);
//					RestClient.post((String) rt.get("slweomsServerUrl")+WsConstants.INFORMATION+WsConstants.ADDINFORMATION, args,WsConstants.INFORMATIONPARAM);
					// 推送消息
					/*List<String> idsList = new ArrayList<String>();
					idsList.add(userId);
					List<String> userIdList = activitiDao
							.getUserIdByOaId(idsList);
					for (String id : userIdList) {
						WebSocketPushMessage.sendToUser(id, "2");// 驳回
						// msgMap.put(id, msg);
						// message.add(msgMap);
						// linkedBlockingQueue.put(msgMap);
					}*/
				}
			}
		} catch (Exception e) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg","拒绝失败，内部异常");
			logger.error("拒绝失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
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
	public Map<String, Object> getSentList(String name) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 获取历史数据服务对象
			HistoryService service = engine.getHistoryService();
			// 根据接受人获取该用户的任务
			List<EmployeeVO> user = activitiDao.getInfo(name);
			if(user == null || user.size() < 1){
				resultMap.put("result", false);
				resultMap.put("msg", "该用户不存在");
				return resultMap;
			}
			List<HistoricProcessInstance> list = service
					.createHistoricProcessInstanceQuery().startedBy(user.get(0).getId())
					.orderByProcessInstanceStartTime().desc().list();
			List<TaskVO> tasks = new ArrayList<TaskVO>();
			TaskVO t = null;
			List<String> procIdList = new ArrayList<String>();
			for (HistoricProcessInstance task : list) {
				t = new TaskVO();
				t.setProcessId(task.getId());// 流程ID
				t.setId(task.getId());// 任务ID
				// t.setName(task.get);//任务名
				t.setCreateTime(task.getStartTime() == null ? "" : TimeUtil
						.dateToString(task.getStartTime()));
				t.setEndTime(task.getEndTime() == null ? "" : TimeUtil
						.dateToString(task.getEndTime()));
				procIdList.add(t.getProcessId());
				t.setEditForm(GlobalConstants.EDIT_FORM_NO);//只能查看表单
				tasks.add(t);
			}
			//批量获取表单数据（减少查库次数，提高效率）
			List<ScheduleFormVO> sfList = activitiDao.getForms(procIdList);
			//数据重组，方便后续使用
			Map<String, ScheduleFormVO> sfMap = new HashMap<String, ScheduleFormVO>();
			for(ScheduleFormVO sfv : sfList){
				sfMap.put(sfv.getProcessId(), sfv);
			}
			Iterator<TaskVO> it = tasks.iterator();
			while(it.hasNext()){
				TaskVO task = it.next();
			    if(sfMap.get(task.getProcessId()) == null){
			    	it.remove();
					continue;
				}
				task.setStartUser(sfMap.get(task.getProcessId()).getCreator());
				task.setFormId(sfMap.get(task.getProcessId()).getFormNo());// 表单编号
				task.setTitle(sfMap.get(task.getProcessId()).getTitle());// 表单标题
				//设置审批状态
				task.setState(sfMap.get(task.getProcessId()).getState());
			}
			resultMap.put("list", tasks);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取已建预约失败", e);
			throw new RuntimeException(e);
		}
		return resultMap;
	}
	
	/**
	 * <pre>
	 * getInfo(创建表单时获取填单人员信息)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月15日 下午5:17:17    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月15日 下午5:17:17    
	 * 修改备注： 
	 * @return
	 * </pre>
	 */
	public List<EmployeeVO> getInfo(String attribute) {
		List<EmployeeVO> list = activitiDao.getInfo(attribute);
		return list;
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
	@SuppressWarnings("unchecked")
	@Override
	public ReturnData getDoneList(Map<String, Object> paramsMap) {
		BaseReturn dataReturn = new BaseReturn();
		List<HistoricTaskInstance> doneList = new ArrayList<HistoricTaskInstance>();
		try {
			List<EmployeeVO> user = activitiDao.getInfo(paramsMap.get("name").toString());
			if(user == null || user.size() < 1){
				return dataReturn.returnError("该用户不存在");
			}
			// 获取历史数据服务对象
			HistoryService service = engine.getHistoryService();
			// 根据接受人获取该用户的任务
			/*List<HistoricTaskInstance> list = service
					.createHistoricTaskInstanceQuery().finished().orderByTaskCreateTime().desc().list();*/
			List<TaskVO> tasks = new ArrayList<TaskVO>();
			List<Object> doneListObj = this.jedisDao.getList(CommonConstrant.REDIS_CACHE_TASK_DONE_LIST);//查询缓存
			doneList = (List<HistoricTaskInstance>)(Object)doneListObj;
			if (doneList == null || doneList.size() <= 0) {//缓存为空
				doneList = service.createHistoricTaskInstanceQuery().finished()
						.orderByTaskCreateTime().desc().list();
			}
			
			TaskVO t = null;
			List<String> procIdList = new ArrayList<String>();
			for (HistoricTaskInstance task : doneList) {
				if(StringUtils.isBlank(task.getAssignee()) || !task.getAssignee().contains(user.get(0).getId())){
					continue;
				}
				if(!procIdList.contains(task.getProcessInstanceId())){
					procIdList.add(task.getProcessInstanceId());
				}else{//去除重复数据
					continue;
				}
				t = new TaskVO();
				t.setProcessId(task.getProcessInstanceId());// 流程ID
				t.setId(task.getId());// 任务ID
				t.setName(task.getName());// 任务名
				t.setCreateTime(task.getStartTime() == null ? "" : TimeUtil
						.dateToString(task.getStartTime()));
				t.setEndTime(task.getEndTime() == null ? "" : TimeUtil
						.dateToString(task.getEndTime()));
				t.setEditForm(GlobalConstants.EDIT_FORM_NO);//只能查看表单
				tasks.add(t);
			}
			//批量获取表单数据（减少查库次数，提高效率）
			paramsMap.put("list", procIdList);
			List<ScheduleFormVO> sfList = activitiDao.getFormsByConfition(paramsMap);
			//数据重组，方便后续使用
			Map<String, ScheduleFormVO> sfMap = new HashMap<String, ScheduleFormVO>();
			for(ScheduleFormVO sfv : sfList){
				sfMap.put(sfv.getProcessId(), sfv);
			}
			Iterator<TaskVO> it = tasks.iterator();
			while(it.hasNext()){
				TaskVO task = it.next();
			    if(sfMap.get(task.getProcessId()) == null){
			    	it.remove();
					continue;
				}
				task.setStartUser(sfMap.get(task.getProcessId()).getCreator());
				task.setFormId(sfMap.get(task.getProcessId()).getFormNo());// 表单编号
				task.setTitle(sfMap.get(task.getProcessId()).getTitle());// 表单标题
				//设置审批状态
				task.setState(sfMap.get(task.getProcessId()).getState());
			}
			
			return dataReturn.returnResult(0, "获取成功", null, tasks);
		} catch (Exception e) {
			logger.error("获取已办事项失败", e);
			return dataReturn.returnError("系统内部异常");
		}
	}
	
	/**
	 * <pre>
	 * getDepRoot(动态获取OA人员信息树节点)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月11日 下午5:09:11    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月11日 下午5:09:11    
	 * 修改备注： 
	 * @return
	 * </pre>
	 */
	public List<DepartmentVO> getDepRoot(String originalPath) {
		List<DepartmentVO> list = activitiDao.getDepRoot();
		List<DepartmentVO> tempList = new ArrayList<DepartmentVO>();
		if (originalPath == null) {//originalPath为空，说明为一级节点
			for (DepartmentVO departmentVO : list) {
				if (departmentVO.getPath().length()==WsConstants.FIRST_NODE_SIZE) {
					tempList.add(departmentVO);
				}
			}
		}else{
			//上级父节点的长度
			 int originalPathLength = originalPath.length();
			 for (DepartmentVO departmentVO : list) {
				 if (departmentVO.getPath().length()>originalPathLength) {
					 //获取对象中与参数相同长度的字符串，两者比较如果相等再判断是否为此节点的子节点
					 String getPathString = departmentVO.getPath().substring(0, originalPathLength);
						//用参数值与截取字符串比较是否相等
						if (getPathString.equals(originalPath)) {
							//如果相等则截取此字符串后续字符串若位数为4则说明是其节点的子节点
							int insertLength = departmentVO.getPath().substring(originalPathLength,departmentVO.getPath().length()).length();
							if (insertLength == WsConstants.NODE_INSERT_SIZE) {
								//tempList.add(departmentVO);
								//查询此节点下是否有员工
								int countEmp = activitiDao.getEmployeeCount(departmentVO);
								//查询此节点下是否有子节点，获取当前对象的path属性长度
								int depPathLength = departmentVO.getPath().length();
								//depList用来存储子节点判断该节点是否显示
								List<DepartmentVO> depList = new ArrayList<DepartmentVO>();
								for (DepartmentVO departmentVO2 : list) {
									//遍历集合，若对象的path属性长度大于当前对象的长度则截取相同位数判断其是否为子节点
									if (departmentVO2.getPath().length()>depPathLength) {
										String depPath = departmentVO2.getPath().substring(0, depPathLength);
										if (depPath.equals(departmentVO.getPath()) || countEmp>0) {
											depList.add(departmentVO2);
										}
									}
								}
								 //if (depList.size()>0) {
										tempList.add(departmentVO);
								  //}
							}
						}
				}
			
			}
		}
		return tempList;
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
	public List<EmployeeVO> getDepEmployee(String depId) {
		return activitiDao.getDepEmployee(depId);
	}
	
	/**
	 * <pre>
	 * getDepRootAll(根据节点信息获取员工信息)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月15日 上午11:25:11    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月15日 上午11:25:11    
	 * 修改备注： 
	 * @param departmentVO
	 * @return
	 * </pre>
	 */
	public List<EmployeeVO> getDepRootAll(String originalPath) {
		List<EmployeeVO> list = activitiDao.getDepEmpAll(originalPath);
		return list;
	}

	/**
	 * 
	 * TODO 预约审批-提交总结表
	 * @author 谢程算
	 * @date 2017年9月12日  
	 * @version 1.0.0 
	 * @param sf
	 * @param taskId
	 * @param session
	 * @return
	 */
	public Map<String, Object> genSumForm(SummaryFormVO sf, String taskId) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 将信息存入数据库
			activitiDao.addSumForm(sf);
			// 完成流程审批操作
			Map<String, Object> variables = new HashMap<String, Object>();
			engine.getRuntimeService().setVariables(sf.getProcessId(),
					variables);
			engine.getTaskService().complete(taskId, variables);
			// 将申请表的审批状态设为“已完成”
			ScheduleFormVO sfv = new ScheduleFormVO();
			sfv.setProcessId(sf.getProcessId());
			sfv.setState(GlobalConstants.COMPLETE_END);//审批完成
			sfv.setDevNum(sf.getDeviceActual());//实际参会点位数
			activitiDao.updateForm(sfv);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("提交失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}
	
	/**
	 * 获取所有已通过审批的本平台跨域会议列表
	 * @param paramsMap 查询条件
	 * @return
	 */
	@Override
	public List<ScheduleFormVO> getCrossDisciplinaryList(Map<String, Object> paramsMap) {
		return this.activitiDao.selectCrossDisciplinaryList(paramsMap);
	}
}
