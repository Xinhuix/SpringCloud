package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.PlatformResourceVO;

/**
 * 平台资源信息Service
 * @author dql
 *
 */
public interface PlatformResService {
	
	/**
	 * 添加平台资源
	 * @param appDeviceVO
	 */
	void insertPlatformResource(PlatformResourceVO platformResVO);
	
	/**
	 * 添加平台资源
	 * @param platformResource 平台资源信息
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData addPlatformResource(PlatformResourceVO platformResource, String token);
	
	/**
	 * 查询平台资源信息
	 * @param paramMap
	 * @return
	 */
	List<PlatformResourceVO> getPlatformResource(Map<String, Object> paramMap);
	
	/**
	 * 通过主键ID获取平台资源信息
	 * @param id 主键ID
	 * @return
	 */
	PlatformResourceVO getPlatformResourceById(String id);

	/**
	 * 修改平台资源信息
	 * @param appDeviceVO
	 */
	Map<String,Object> updatePlatformResource(PlatformResourceVO platformResVO) throws Exception;
	
	/**
	 * 删除平台资源信息
	 * @param id
	 */
	void deletePlatformResource(String id);

}
