package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.TPermissionVO;

/**
 * 功能资源树(权限列表)操作业务层接口
 *
 */
public interface PermissionService {
	/**
	 * 通过平台类别查询权限根节点
	 * @param platformType 平台类别
	 * @return
	 */
	public List<TPermissionVO> getRootPermissionByPlatformType(String platformType);
	
	/**
	 * 通过上级目录ID获取子菜单列表
	 * @param pid 上级目录ID
	 * @return
	 */
	public List<TPermissionVO> getChildrenPermissionByPid(String pid);
	
	/**
	 * 通过主键UUID查询权限信息
	 * @param uuid 主键UUID
	 * @return
	 */
	public TPermissionVO getPermission(String uuid);
	
	/**
	 * 通过主键UUID修改描述信息
	 * @param permission
	 * @return
	 */
	public int updatePermissionById(TPermissionVO permission);
	
	/**
	 * 通过角色ID和平台类别查询角色在该平台下的权限
	 * @param paramsMap {"roleId":"", "platformType":""}
	 * @return 
	 */
	public List<TPermissionVO> getRolePermissionExist(Map<String, Object> paramsMap);
	
	/**
	 * 获取对应平台的所有权限
	 * @param platformType 平台类别
	 * @return
	 */
	public List<TPermissionVO> getAllPermissionByPlatformType(String platformType);
}
