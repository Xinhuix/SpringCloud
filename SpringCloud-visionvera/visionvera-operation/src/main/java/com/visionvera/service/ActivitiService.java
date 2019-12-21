package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DepartmentVO;
import com.visionvera.bean.cms.EmployeeVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.SmsVO;
import com.visionvera.bean.cms.SummaryFormVO;
import com.visionvera.bean.cms.TaskConditionVO;
import com.visionvera.bean.cms.UserVO;

/**
 * 会管数据的业务
 *
 */
public interface ActivitiService {
	/**
	 * TODO 获取待办事项列表
	 * @return
	 */
	Map<String, Object> getTodoList(String name);
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getForm
	 * @Description: TODO 获取预约表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	List<ScheduleFormVO> getForm(ScheduleFormVO sf);
	/** <pre>getInfoId(根据员工id获取员工信息)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月17日 下午1:36:21    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月17日 下午1:36:21    
	 * 修改备注： 
	 * @param idsList
	 * @return</pre>    
	 */
	List<EmployeeVO> getInfoId(List<String> idsList);
	List<UserVO> getUserbyLoginName(Map<String, Object> paramsMap);
	List<TaskConditionVO> getTaskCondition(TaskConditionVO taskCondition);
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getSumForm
	 * @Description: TODO 获取总结表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	List<SummaryFormVO> getSumForm(SummaryFormVO sf);
	/**
	 * @param ConstDataVO cd 
	 * 
	 * @Title: getTaskCondition
	 * @Description: 查询任务状态列表
	 * @param @return  参数说明  
	 * @return List<ConstDataVO>     返回类型 
	 * @throws 
	 */
	List<ConstDataVO> getConstData(ConstDataVO cd);
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
	Map<String, Object> complete(String advice, String names, String taskId,
			ScheduleFormVO sf, String string, String string2);
	
	SmsVO getSms();
	/**
	 * 
	 * TODO 预约审批-拒绝
	 * @author 谢程算
	 * @date 2017年9月12日  
	 * @version 1.0.0 
	 * @param string
	 * @param comment
	 * @param taskId
	 * @param taskId2 
	 * @return
	 */
	Map<String, Object> reject(String loginName, String loginUserId, String comment, String taskId, String processId);
	
	/**
	 * 
	 * TODO 获取已发事项列表
	 * @author 谢程算
	 * @date 2017年9月27日  
	 * @version 1.0.0 
	 * @param name
	 * @return
	 */
	Map<String, Object> getSentList(String name);
	
	/** <pre>getInfo(获取当前登录用户员工基本信息)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月16日 上午10:11:11    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月16日 上午10:11:11    
	 * 修改备注： 
	 * @param attribute
	 * @return</pre>    
	 */
	List<EmployeeVO> getInfo(String attribute);
	
	/**
	 * 
	 * TODO 获取已办事项列表
	 * @author 谢程算
	 * @date 2017年9月27日  
	 * @version 1.0.0 
	 * @param name
	 * @return
	 */
	ReturnData getDoneList(Map<String, Object> paramsMap);
	
	/** <pre>getDepRoot(获取部门节点)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月16日 上午10:10:45    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月16日 上午10:10:45    
	 * 修改备注： 
	 * @param originalPath
	 * @return</pre>    
	 */
	List<DepartmentVO> getDepRoot(String originalPath);
	
	/** <pre>getDepEmployee(根据部门id获取当前节点员工信息（不包含子节点）)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月17日 下午1:36:25    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月17日 下午1:36:25    
	 * 修改备注： 
	 * @param depId
	 * @return</pre>    
	 */
	List<EmployeeVO> getDepEmployee(String depId);
	
	/** <pre>getDepRootAll(获取到当前节点下的所有员工（包含子节点）)   
	 * 创建人：周逸芳1981769658@qq.com         
	 * 创建时间：2017年5月16日 上午10:11:08    
	 * 修改人：周逸芳1981769658@qq.com        
	 * 修改时间：2017年5月16日 上午10:11:08    
	 * 修改备注： 
	 * @param path
	 * @return</pre>    
	 */
	List<EmployeeVO> getDepRootAll(String path);
	
	/**
	 * 
	 * TODO 预约审批-提交总结表
	 * @author 谢程算
	 * @date 2017年9月12日  
	 * @version 1.0.0 
	 * @param sf
	 * @param taskId
	 * @return
	 */
	Map<String, Object> genSumForm(SummaryFormVO sf, String taskId);
	
	/**
	 * 获取所有已通过审批的本平台跨域会议列表
	 * @param paramsMap 查询条件
	 * @return
	 */
	public List<ScheduleFormVO> getCrossDisciplinaryList(Map<String, Object> paramsMap);
}
