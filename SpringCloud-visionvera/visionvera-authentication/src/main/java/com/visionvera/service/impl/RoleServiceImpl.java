package com.visionvera.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.TPermissionVO;
import com.visionvera.bean.datacore.TRoleVO;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.authentication.RoleDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.RoleService;

/**
 * 角色操作业务实现类
 *
 */
@Service
@Transactional(transactionManager = "transactionManager_authentication", rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RoleDao roleDao;
	@Autowired
	private JRedisDao jredisDao;
	
	/**
	 * 通过条件查询角色信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param role 角色条件信息
	 * @return
	 */
	@Override
	public PageInfo<TRoleVO> getRoleList(boolean isPage, Integer pageNum, Integer pageSize, TRoleVO role) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<TRoleVO> roleList = this.roleDao.selectRoleByConfition(role);
		PageInfo<TRoleVO> roleInfo = new PageInfo<TRoleVO>(roleList);
		return roleInfo;
	}
	
	/**
	 * 通过条件查询角色携带平台信息
	 */
	@Override
	public PageInfo<TRoleVO> getRoleAndPlatformList(boolean isPage, Integer pageNum, Integer pageSize, TRoleVO role) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		List<TRoleVO> roleList = this.roleDao.selectRoleAndPlatformByConfition(role);
		PageInfo<TRoleVO> roleInfo = new PageInfo<TRoleVO>(roleList);
		return roleInfo;
	}

	/**
	 * 添加角色
	 * @param role
	 */
	public void addRole(TRoleVO role, String accessToken) {
		//判断角色是否重名
		if(this.roleDao.selectRepeatRoleByName(role) > 0){
			LOGGER.error("添加角色失败：角色名称重复");
			throw new RuntimeException("角色名称重复");
		}
		try{
			//添加角色基本信息
			role.setCreateName(jredisDao.get(CommonConstrant.PREFIX_TOKEN + "_" + accessToken));
			this.roleDao.addRole(role);
		}catch (Exception e){
			LOGGER.error("添加角色失败", e);
			throw new RuntimeException("添加角色失败");
		}
		//校验数据
		if(StringUtils.isBlank(role.getPlatformId()))
			return;
		if(StringUtils.isBlank(role.getPermissionId()))
			return;
		String platIds[] = role.getPlatformId().split(",");
		String perIds[] = role.getPermissionId().split(";");
		if(platIds.length != perIds.length){
			LOGGER.error("添加角色失败：平台-权限对应数据不正确");
			throw new RuntimeException("平台-权限数据异常");
		}
		try{
			//添加角色和平台的关联关系
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("roleId", role.getUuid());
			params.put("platformIds", platIds);
			this.roleDao.addRoleRolePlatformRel(params);
			//组合数据
			List<Map<String, String>> platPermissionRelList = new ArrayList<Map<String, String>>();
			Map<String, String> platPermissionRel = null;
			String[] platPerIds = {};
 			for(int i = 0; i < platIds.length; i ++){
 				platPerIds = perIds[i].split(",");
 				for(String platPerId : platPerIds){
 					platPermissionRel = new HashMap<String, String>();
 					platPermissionRel.put("platId", platIds[i]);//权限ID
 					platPermissionRel.put("perId", platPerId);//平台ID
 					platPermissionRelList.add(platPermissionRel);
 				}
			}
			//添加角色和权限的关联关系
			params.put("permissionIds", platPermissionRelList);
			this.roleDao.addRolePermissionRel(params);
		}catch (Exception e){
			LOGGER.error("添加角色失败", e);
			throw new RuntimeException("添加角色失败");
		}
	}
	
	/**
	 * 只会添加角色和平台与角色和平台权限的关系
	 * @param role 角色信息
	 * @param access_token
	 * @return
	 */
	@Override
	public void addRoleOtherRel(TRoleVO role, String access_token) {
		String platIds[] = role.getPlatformId().split(",");
		String perIds[] = role.getPermissionId().split(";");
		if(platIds.length != perIds.length){
			LOGGER.error("添加角色失败：平台-权限对应数据不正确");
			throw new BusinessException("平台-权限数据异常");
		}
		
		//添加角色和平台的关联关系
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("roleId", role.getUuid());
		params.put("platformIds", platIds);
		this.roleDao.addRoleRolePlatformRel(params);//添加角色平台关联关系
		//组合数据
		List<Map<String, String>> platPermissionRelList = new ArrayList<Map<String, String>>();
		Map<String, String> platPermissionRel = null;
		String[] platPerIds = {};
			for(int i = 0; i < platIds.length; i ++){
				platPerIds = perIds[i].split(",");
				for(String platPerId : platPerIds){
					platPermissionRel = new HashMap<String, String>();
					platPermissionRel.put("platId", platIds[i]);//权限ID
					platPermissionRel.put("perId", platPerId);//平台ID
					platPermissionRelList.add(platPermissionRel);
				}
		}
		//添加角色和权限的关联关系
		params.put("permissionIds", platPermissionRelList);
		this.roleDao.addRolePermissionRel(params);//添加角色和平台权限的关联关系
	}

	/**
	 * 更新角色
	 * @param role
	 */
	public void updateRole(TRoleVO role) {
		//判断角色是否重名
		if(this.roleDao.selectRepeatRoleByName(role) > 0){
			LOGGER.error("修改角色失败：角色名称重复");
			throw new RuntimeException("角色名称重复");
		}
		try{
			//更新角色基本信息
			this.roleDao.updateRole(role);
			//更新角色和权限的关联关系（先删再加）
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("roleIds", role.getUuid().split(","));
			this.roleDao.deleteRolePermissionRel(params);
			this.roleDao.deleteRolePlatformRel(params);
			params.clear();
		}catch (Exception e){
			LOGGER.error("更新角色失败", e);
			throw new RuntimeException("更新角色失败");
		}
		//校验数据
		if(StringUtils.isBlank(role.getPlatformId()))
			return;
		if(StringUtils.isBlank(role.getPermissionId()))
			return;
		String platIds[] = role.getPlatformId().split(",");
		String perIds[] = role.getPermissionId().split(";");
		if(platIds.length != perIds.length){
			LOGGER.error("添加角色失败：平台-权限对应数据不正确");
			throw new RuntimeException("平台-权限数据异常");
		}
		try{
			//添加角色和平台的关联关系
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("roleId", role.getUuid());
			params.put("platformIds", platIds);
			this.roleDao.addRoleRolePlatformRel(params);
			//组合数据
			List<Map<String, String>> platPermissionRelList = new ArrayList<Map<String, String>>();
			Map<String, String> platPermissionRel = null;
			String[] platPerIds = {};
 			for(int i = 0; i < platIds.length; i ++){
 				platPerIds = perIds[i].split(",");
 				for(String platPerId : platPerIds){
 					platPermissionRel = new HashMap<String, String>();
 					platPermissionRel.put("platId", platIds[i]);//权限ID
 					platPermissionRel.put("perId", platPerId);//平台ID
 					platPermissionRelList.add(platPermissionRel);
 				}
			}
			//添加角色和权限的关联关系
			params.put("permissionIds", platPermissionRelList);
			this.roleDao.addRolePermissionRel(params);
		}catch (Exception e){
			LOGGER.error("添加角色失败", e);
			throw new RuntimeException("添加角色失败");
		}
	}

	/**
	 * 删除角色
	 * @param role
	 */
	public void deleteRole(TRoleVO role) {
		Map<String, Object> params = new HashMap<String, Object>();
		try{
			params.put("roleIds", role.getUuid().split(","));
			this.roleDao.deleteRole(params);
			this.roleDao.deleteRolePermissionRel(params);
			this.roleDao.deleteRolePlatformRel(params);
		}catch (Exception e){
			LOGGER.error("更新角色失败", e);
			throw new RuntimeException("更新角色失败");
		}
	}
	
	/**
	 * 通过ID查询角色信息，携带出平台已经有的平台信息和权限信息
	 * @param uuid 角色主键UUID
	 * @return
	 */
	public ReturnData getRoleInfoById(String uuid) {
		BaseReturn dataReturn = new BaseReturn();
		TRoleVO role = new TRoleVO();
		role.setUuid(uuid);
		
		List<TRoleVO> roleList = this.roleDao.selectRoleAndPlatformByConfition(role);
		if (roleList == null || roleList.size() <= 0) {
			return dataReturn.returnError("未找到该角色");
		}
		
		List<TPermissionVO> permissionList = this.roleDao.selectPermissionByRoleId(uuid);
		roleList.get(0).setPermissionList(permissionList);
		return dataReturn.returnResult(0, "获取成功", null, roleList);
	}
	
	/**
	 * 通过平台ID查询默认的角色信息，包含其对应的权限
	 * @param platformId 平台ID
	 * @return
	 */
	@Override
	public TRoleVO getDefaultRoleByPlatformId(String platformId) {
		return this.roleDao.selectDefaultRoleWithPermissionIdsByPlatformId(platformId);
	}

	/**
	 * 通过平台ID修改默认角色名称
	 * @param role 平台ID、角色名称
	 * @return
	 */
	public void updateDfRoleName(TRoleVO role){
		this.roleDao.updateDfRoleName(role);
	}
}
