package com.visionvera.dao.authentication;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.TPermissionVO;
import com.visionvera.bean.datacore.TRoleVO;

/**
 * 操作权限相关的DAO接口
 *
 */
public interface PermissionDao {
	/**
	 * 通过主键ID查询权限信息
	 * @param permissionId
	 * @return
	 */
	public TPermissionVO selectPermissionById(String permissionId);
	
	/**
	 * 通过角色ID查询权限信息
	 * @param roleId
	 * @return
	 */
	public List<TPermissionVO> selectPermissionByRoleId(String roleId);
	
	/**
	 * 通过角色ID和平台ID查询权限信息，用于用户登录使用
	 * @param role 封装了角色ID和平台ID
	 * @return
	 */
	public List<TPermissionVO> selectPermissionForLogin(TRoleVO role);
	
	/**
	 * 根据平台类别查询权限根节点
	 * @param platformType 平台类别
	 * @return
	 */
	public List<TPermissionVO> selectPermissionByCondition(TPermissionVO permission);
	
	/**
	 * 通过主键UUID修改权限信息.目前只修改描述信息，其他不做修改
	 * @param permission
	 * @return
	 */
	public int updatePermissionById(TPermissionVO permission);
	
	/**
	 * 通过角色ID和平台类别查询该角色对应的平台下的所有权限信息
	 * @param paramsMap {"roleId":"","platformType":""}
	 * @return
	 */
	public List<TPermissionVO> selectRolePermissionExist(Map<String, Object> paramsMap);
	
	/**
	 * 根据平台ID查询该平台有没有权限数量
	 * @param platformId 平台ID
	 * @return
	 */
	public int selectCountByPlatformId(String platformId);

	/**
	 * 根据角色ID（批量）查询权限信息
	 * @param paramsMap 角色ID列表
	 * @return
	 */
	public String selectPermissionByRole(Map<String, Object> paramsMap);
}
