package com.visionvera.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.datacore.TPermissionVO;
import com.visionvera.dao.authentication.PermissionDao;
import com.visionvera.service.PermissionService;

/**
 * 功能资源树(权限列表)操作业务层接口
 *
 */
@Service
@Transactional(transactionManager = "transactionManager_authentication", rollbackFor = Exception.class)
public class PermissionServiceImpl implements PermissionService {
	@Autowired
	private PermissionDao permissionDao;
	
	/**
	 * 通过平台类别查询权限根节点
	 * @param platformType 平台类别
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return
	 */
	@Override
	public List<TPermissionVO> getRootPermissionByPlatformType(String platformType) {
		TPermissionVO paramsPermission = new TPermissionVO();
		paramsPermission.setPlatformType(platformType);
		paramsPermission.setPid("0");//所有资源(权限)树根节点的PID为0
		return this.permissionDao.selectPermissionByCondition(paramsPermission);
	}

	/**
	 * 通过上级目录ID获取子菜单列表
	 * @param pid 上级目录ID
	 * @return
	 */
	@Override
	public List<TPermissionVO> getChildrenPermissionByPid(String pid) {
		TPermissionVO permission = new TPermissionVO();
		permission.setPid(pid);
		return this.permissionDao.selectPermissionByCondition(permission);
	}
	
	/**
	 * 通过主键UUID查询权限信息
	 * @param uuid 主键UUID
	 * @return
	 */
	@Override
	public TPermissionVO getPermission(String uuid) {
		return this.permissionDao.selectPermissionById(uuid);
	}
	
	/**
	 * 通过主键UUID修改描述信息
	 * @param permission
	 * @return
	 */
	@Override
	public int updatePermissionById(TPermissionVO permission) {
		return this.permissionDao.updatePermissionById(permission);
	}

	/**
	 * 通过角色ID和平台类别查询角色在该平台下的权限
	 * @param paramsMap {"roleId":"", "platformType":""}
	 * @return 
	 */
	@Override
	public List<TPermissionVO> getRolePermissionExist(Map<String, Object> paramsMap) {
		return this.permissionDao.selectRolePermissionExist(paramsMap);
	}
	
	/**
	 * 获取对应平台的所有权限
	 * @param platformType 平台类别
	 * @return
	 */
	@Override
	public List<TPermissionVO> getAllPermissionByPlatformType(String platformType) {
		TPermissionVO paramsPermission = new TPermissionVO();
		paramsPermission.setPlatformType(platformType);//通过平台类别查询
		return this.permissionDao.selectPermissionByCondition(paramsPermission);
	}
}
