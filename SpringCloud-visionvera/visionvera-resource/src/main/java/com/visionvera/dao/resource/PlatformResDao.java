package com.visionvera.dao.resource;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.PlatformResourceVO;

/**
 * 平台资源管理dao
 * @author dql
 *
 */
public interface PlatformResDao {
	
	/**
	 * 添加平台资源管理
	 * @param appDeviceVO
	 */
	int insertPlatformResource(PlatformResourceVO platformVO);
	
	/**
	 * 查询平台资源信息
	 * @param paramMap
	 * @return
	 */
	List<PlatformResourceVO> getPlatformResource(Map<String, Object> paramMap);
	
	/**
	 * 根据ID查询平台资源信息
	 * @param id
	 * @return
	 */
	PlatformResourceVO getPlatformResourceById(String id);
	
	/**
	 * 更新平台资源信息
	 * @param appDeviceVO
	 */
	void updatePlatformResource(PlatformResourceVO platformVO);
	
	/**
	 * 删除平台资源信息
	 * @param id
	 * @return
	 */
	void deletePlatformResource(String id);

	/**
	 * 查询重复平台名称
	 * @param platformVO
	 * @return
	 */
	int getRepeatPlatformName(PlatformResourceVO platformVO);

}
