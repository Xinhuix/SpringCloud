package com.visionvera.dao.operation;

import java.util.List;

import com.visionvera.bean.cms.TaskConditionVO;

public interface TaskConditionDao {
	

	
	/**
	 * @param taskCondition 
	 * 
	 * @Title: getTaskCondition
	 * @Description: 查询任务状态
	 * @param @return  参数说明  
	 * @return TaskCondition    返回类型 
	 * @throws
	 */
	List<TaskConditionVO> getTaskCondition(TaskConditionVO taskCondition);
	
	
	/**
	 * @param taskCondition 
	 * 
	 * @Title: addTaskCondition
	 * @Description: 新增任务状态表单
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	 int addTaskCondition(TaskConditionVO taskCondition);
}
