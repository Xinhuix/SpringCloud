package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.TRoleVO;

/**
 * 角色业务
 *
 */
public interface RoleService {
	/**
	 * 通过条件查询角色信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param role 角色条件信息
	 * @return
	 */
	public PageInfo<TRoleVO> getRoleList(boolean isPage, Integer pageNum, Integer pageSize, TRoleVO role);
	
	/**
	 * 通过条件查询角色信息携带平台信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param role 角色+平台查询信息
	 * @return
	 */
	public PageInfo<TRoleVO> getRoleAndPlatformList(boolean isPage, Integer pageNum, Integer pageSize, TRoleVO role);

	/**
	 * 添加角色
	 * @param role
	 * @param access_token 
	 */
	public void addRole(TRoleVO role, String access_token);
	
	/**
	 * 只会添加角色和平台与角色和平台权限的关系
	 * @param role 角色信息
	 * @param access_token
	 * @return
	 */
	public void addRoleOtherRel(TRoleVO role, String access_token);

	/**
	 * 更新角色
	 * @param role
	 */
	public void updateRole(TRoleVO role);

	/**
	 * 删除角色
	 * @param role
	 */
	public void deleteRole(TRoleVO role);
	
	/**
	 * 通过ID查询角色信息，携带出平台已经有的平台信息和权限信息
	 * @param uuid 角色主键UUID
	 * @return
	 */
	public ReturnData getRoleInfoById(String uuid);
	
	/**
	 * 通过平台ID查询默认的角色信息，包含其对应的权限
	 * @param platformId 平台ID
	 * @return
	 */
	public TRoleVO getDefaultRoleByPlatformId(String platformId);
	
	/**
	 * 通过平台ID修改默认角色名称
	 * @param role 平台ID、角色名称
	 * @return
	 */
	public void updateDfRoleName(TRoleVO role);
}
