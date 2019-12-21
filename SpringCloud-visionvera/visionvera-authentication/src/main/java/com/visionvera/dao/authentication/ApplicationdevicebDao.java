package com.visionvera.dao.authentication;

import java.util.List;

import com.visionvera.bean.datacore.TApplicationdevicebVO;

/**
 * 操作平台的DAO接口
 *
 */
public interface ApplicationdevicebDao {
	/**
	 * 通过平台主键ID查询平台信息
	 * @param platformId
	 * @return
	 */
	public TApplicationdevicebVO selectApplicationdevicebById(String platformId);
	
	/**
	 * 通过角色ID查询角色对应的平台信息
	 * @param roleId 角色ID
	 * @return
	 */
	public List<TApplicationdevicebVO> selectPlatformByRoleId(String roleId);
}
