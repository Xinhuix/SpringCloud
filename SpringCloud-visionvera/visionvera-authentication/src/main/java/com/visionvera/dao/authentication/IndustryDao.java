package com.visionvera.dao.authentication;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.TIndustrybVO;

/**
 * 操作行业归属的DAO
 *
 */
public interface IndustryDao {
	/**
	 * 批量添加用户行业归属信息
	 * @param paramsMap {"userId":"", "industryIds":""}
	 * @return
	 */
	public int insertUserIndustry(Map<String, Object> paramsMap);
	
	/**
	 * 通过条件获取行业归属信息
	 * @param industry 查询条件
	 * @return
	 */
	public List<TIndustrybVO> selectIndustryByCondition(TIndustrybVO industry);
}
