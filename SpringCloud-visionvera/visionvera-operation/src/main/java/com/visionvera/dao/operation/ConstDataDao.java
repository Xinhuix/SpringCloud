package com.visionvera.dao.operation;

import java.util.List;

import com.visionvera.bean.cms.ConstDataVO;

public interface ConstDataDao {
	/**
	 * @param ConstDataVO cd 
	 * 
	 * @Title: getTaskCondition
	 * @Description: 查询任务状态列表
	 * @param @return  参数说明  
	 * @return List<ConstDataVO>     返回类型 
	 * @throws 
	 */
	public List<ConstDataVO> getConstData(ConstDataVO cd);
}
