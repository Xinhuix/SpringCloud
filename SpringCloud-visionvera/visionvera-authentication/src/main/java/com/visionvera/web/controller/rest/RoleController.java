package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.TRoleVO;
import com.visionvera.common.api.authentication.RoleAPI;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.RoleService;

@RestController
public class RoleController extends BaseReturn implements RoleAPI {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RoleService roleService;
	
	/**
	 * 获取所有用户角色接口。可分页
	 * @return
	 */
	@RequestMapping(value = "/getRoleList", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData getRoleList(@RequestBody(required = false) TRoleVO role, 
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize ) {
		Map<String, Object> extraMap = new HashMap<String, Object>();
		PageInfo<TRoleVO> roleInfo = new PageInfo<TRoleVO>();
		try {
			if (pageNum.equals(-1)) {//不分页查询全部角色信息，不携带平台信息
				role = new TRoleVO();
				roleInfo = this.roleService.getRoleAndPlatformList(false, null, null, role);//查询角色信息
				return super.returnResult(0, "获取成功", null, roleInfo.getList());
				
			}
			
			roleInfo = this.roleService.getRoleAndPlatformList(true, pageNum, pageSize, role);//查询角色信息携带平台信息
			
			extraMap.put("pageSize", pageSize);
			extraMap.put("totalPage", roleInfo.getPages());
			extraMap.put("pageNum", pageNum);
			return super.returnResult(0, "获取成功", null, roleInfo.getList(), extraMap);
		} catch (Exception e) {
			this.LOGGER.error("查询角色信息失败=====>", e);
			return super.returnError("获取角色信息失败");
		}
	}

	/**
	 * 添加角色
	 * @param role
	 * @return
	 */
	@Override
	public ReturnData addRole(@RequestBody TRoleVO role, @RequestParam String access_token) {
		try {
			this.roleService.addRole(role, access_token);
			return super.returnSuccess("添加成功");
		} catch (RuntimeException e) {
			this.LOGGER.error("添加角色失败=====>", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("添加角色失败=====>", e);
			return super.returnError("添加失败");
		}
	}
	
	/**
	 * 只会添加角色和平台与角色和平台权限的关系
	 * @param role 角色信息
	 * @param access_token
	 * @return
	 */
	@Override
	public ReturnData addRoleOtherRel(@RequestBody TRoleVO role, @RequestParam("access_token") String access_token) {
		try {
			//校验数据
			if(StringUtils.isBlank(role.getPlatformId())) {
				return super.returnError("平台ID为空");
			}
			if(StringUtils.isBlank(role.getPermissionId())) {
				return super.returnError("权限ID为空");
			}
			
			this.roleService.addRoleOtherRel(role, access_token);
			return super.returnSuccess("添加成功");
		} catch (BusinessException e) {
			this.LOGGER.error("添加角色关联关系失败=====>", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("添加角色关联关系失败=====>", e);
			return super.returnError("添加失败");
		}
	}

	/**
	 * 修改角色
	 * @param role
	 * @return
	 */
	@Override
	public ReturnData updateRole(@RequestBody TRoleVO role ) {
		try {
			this.roleService.updateRole(role);
			return super.returnSuccess("更新成功");
		} catch (RuntimeException e) {
			this.LOGGER.error("更新角色失败=====>", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("更新角色失败=====>", e);
			return super.returnError("更新失败");
		}
	}

	/**
	 * 删除角色（多个值uuid用,分割）
	 * @param role
	 * @return
	 */
	@Override
	public ReturnData deleteRole(@RequestBody TRoleVO role ) {
		try {
			if(StringUtils.isBlank(role.getUuid())){
				return super.returnError("角色ID不能为空");
			}
			this.roleService.deleteRole(role);
			return super.returnSuccess("删除成功");
		} catch (Exception e) {
			this.LOGGER.error("删除角色失败=====>", e);
			return super.returnError("删除失败");
		}
	}
	
	/**
	 * 通过主键UUID查询角色信息，携带出平台信息、权限信息
	 * @param uuid
	 * @return
	 */
	@RequestMapping(value = "/{uuid}/getRoleInfo", method = RequestMethod.GET)
	public ReturnData getRoleInfo(@PathVariable("uuid") String uuid) {
		try {
			return this.roleService.getRoleInfoById(uuid);
		} catch (Exception e) {
			this.LOGGER.error("RoleController ===== getRoleInfo ===== 获取角色信息失败 =====> ", e);
			return super.returnError("获取失败");
		}
	}
	
	
	/**
	 * 通过平台ID查询默认的角色信息，包含其对应的权限
	 * @param platformId 平台ID
	 * @return
	 */
	@Override
	public ReturnData getDefaultRoleByPlatformId(@PathVariable("platformId") String platformId) {
		try {
			TRoleVO role = this.roleService.getDefaultRoleByPlatformId(platformId);
			
			return super.returnResult(0, "获取成功", null, null, role);
		} catch (BusinessException e) {
			this.LOGGER.error("RoleController ===== getDefaultRoleByPlatformId ===== 通过平台ID查询默认角色失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("RoleController ===== getDefaultRoleByPlatformId ===== 通过平台ID查询默认角色失败 =====> ", e);
			return super.returnError("获取失败");
		}
	}
	
	/**
	 * 通过平台ID查询默认的角色信息，包含其对应的权限
	 * @param platformId 平台ID
	 * @return
	 */
	@Override
	public ReturnData updateDfRoleName(@RequestBody TRoleVO role) {
		try {
			this.roleService.updateDfRoleName(role);
			return super.returnResult(0, "修改成功", null, null, null);
		} catch (BusinessException e) {
			this.LOGGER.error("RoleController ===== getDefaultRoleByPlatformId ===== 通过平台ID修改默认角色失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("RoleController ===== getDefaultRoleByPlatformId ===== 通过平台ID修改默认角色失败 =====> ", e);
			return super.returnError("修改失败");
		}
	}
}
