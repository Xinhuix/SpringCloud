package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.cms.DepartmentVO;
import com.visionvera.bean.cms.EmployeeVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.SummaryFormVO;
import com.visionvera.bean.cms.UserVO;

public interface ActivitiDao {

	/**
	 * @param paramsMap 
	 * 
	 * @Title: addForm
	 * @Description: TODO 新增预约表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int addForm(ScheduleFormVO sf);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: getIsApproved
	 * @Description: TODO 获取会议保障申请表审批情况
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	List<ScheduleFormVO> getIsApproved(ScheduleFormVO sf);

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

	/**
	 * @param paramsMap 
	 * 
	 * @Title: getForms
	 * @Description: TODO 批量获取会议保障申请表（简化字段，提高效率，给待审、已审、已建列表使用）
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	List<ScheduleFormVO> getForms(List<String> procIdList);
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getForms
	 * @Description: TODO 通过条件批量获取会议保障申请表（简化字段，提高效率，给待审、已审、已建列表使用）
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	List<ScheduleFormVO> getFormsByConfition(Map<String, Object> paramsMap);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: updateForm
	 * @Description: TODO 修改表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int updateForm(ScheduleFormVO sf);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: deleteForm
	 * @Description: TODO 删除表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteForm(ScheduleFormVO sf);
	
	List<DepartmentVO> getDepRoot();

	int getEmployeeCount(DepartmentVO departmentVO);
	
	/**
	 * 
	 * TODO 查询指定用户名的员工是否存在
	 * @author 谢程算
	 * @date 2017年8月1日  
	 * @version 1.0.0 
	 * @param paraMap
	 * @return
	 */
	List<EmployeeVO> getEmployee(Map<String, Object> paraMap);

	List<EmployeeVO> getDepEmpInfo(DepartmentVO departmentVO);

	List<EmployeeVO> getInfo(String attribute);

	List<EmployeeVO> getDepEmpAll(String originalPath);

	List<EmployeeVO> getInfoId(List<String> idsList);

	List<EmployeeVO> getDepEmployee(String depId);
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: addSumForm
	 * @Description: TODO 新增总结表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int addSumForm(SummaryFormVO sf);

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
	 * @param paramsMap 
	 * 
	 * @Title: updateSumForm
	 * @Description: TODO 修改总结表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int updateSumForm(SummaryFormVO sf);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: deleteSumForm
	 * @Description: TODO 删除总结表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteSumForm(SummaryFormVO sf);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: deleteProcess
	 * @Description: TODO 删除流程
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteProcess(Map<String, Object> paramsMap);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: getDevProvince
	 * @Description: TODO 根据终端号码查询所在省份
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	String getDevProvince(Map<String, Object> map);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: getDevCity
	 * @Description: TODO 根据终端号码查询所在城市
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	String getDevCity(Map<String, Object> map);

	Integer getDevId(Integer chairman);

	/**
	 * @param map 
	 * 添加预约与设备关联（pamir创建预约用）
	 * @Title: addDevs2Schedule
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDevs2Schedule(List<Map<String, Object>> list);

	/**
	 * @param map 
	 * 删除预约与设备关联（pamir修改预约用）
	 * @Title: addDevs2Schedule
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int delDevs2Schedule(Map<String, Object> map);
	
	List<String> getUserIdByOaId(List<String> idsList);

	/**
	 * 
	 * TODO 把新部署流程图`act_re_procdef`中的ID_修改为ActivitiProcess:1:4
	 * @author 谢程算
	 * @date 2017年9月7日  
	 * @version 1.0.0 
	 * @param map
	 */
	void setDefActId(Map<String, Object> map);
	/**
	 * @param String 
	 * 
	 * @Title: getUserInfo
	 * @Description: TODO 
	 * @param @return  参数说明  
	 * @return List    返回类型 
	 * @throws
	 */
	List<UserVO> getUserInfo(String userId);
	
	/**
	 * 获取所有已通过的本平台跨域会议
	 * @param paramsMap
	 * @return
	 */
	public List<ScheduleFormVO> selectCrossDisciplinaryList(Map<String, Object> paramsMap);
}
