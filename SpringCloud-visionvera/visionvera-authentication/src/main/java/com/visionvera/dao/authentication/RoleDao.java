package com.visionvera.dao.authentication;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.TPermissionVO;
import com.visionvera.bean.datacore.TRoleVO;

/**
 * 操作角色的DAO接口
 *
 */
public interface RoleDao {
	/**
	 * 查询角色信息，提供登陆使用。
	 * 	条件：用户UUID和平台类别
	 * @param paramsMap {"userId":""}
	 * @return
	 */
	List<TRoleVO> selectRoleForLogin(Map<String, Object> paramsMap);
	
	/**
	 * 通过条件查询角色信息
	 * @param role 角色信息
	 * @return
	 */
	List<TRoleVO> selectRoleByConfition(TRoleVO role);

	/**
	 * 查询角色名称是否重复
	 * @param role 角色信息
	 * @return
	 */
	int selectRepeatRoleByName(TRoleVO role);
	
	/**
	 * 通过条件查询角色携带平台信息
	 * @param role
	 * @return
	 */
	List<TRoleVO> selectRoleAndPlatformByConfition(TRoleVO role);
	
	/**
	 * 添加角色
	 * @param role
	 */
	public void addRole(TRoleVO role);
	
	/**
	 * 添加角色权限关联关系
	 * @param role
	 */
	public void addRolePermissionRel(Map<String, Object> params);
	
	/**
	 * 添加角色平台关联关系
	 * @param role
	 */
	public void addRoleRolePlatformRel(Map<String, Object> params);
	
	/**
	 * 删除角色权限关联
	 * @param params
	 */
	public void deleteRolePermissionRel(Map<String, Object> params);
	
	/**
	 * 删除角色平台关联
	 * @param params
	 */
	public void deleteRolePlatformRel(Map<String, Object> params);

	/**
	 * 更新角色
	 * @param role
	 */
	public void updateRole(TRoleVO role);


	/**
	 * 删除角色
	 * @param params
	 */
	public void deleteRole(Map<String, Object> params);
	
	/**
	 * 通过角色ID查询该角色对应的权限信息
	 * @param roleId 角色ID
	 * @return
	 */
	public List<TPermissionVO> selectPermissionByRoleId(String roleId);
	
	/**
	 * 通过平台ID查询默认的角色信息，包含其对应的权限.每个平台只有一个默认角色
	 * @param platformId 平台ID
	 * @return
	 */
	public TRoleVO selectDefaultRoleWithPermissionIdsByPlatformId(String platformId);
	
	/**
	 * 通过平台ID查询角色ID
	 * @param paramsMap 平台ID
	 * @return
	 */
	TRoleVO selectRoleIdByplatformId(Map<String, Object> paramsMap);
	
	/**
	 * 通过平台ID修改默认角色名称
	 * @param role 平台ID、角色名称
	 * @return
	 */
	public void updateDfRoleName(TRoleVO role);
}
