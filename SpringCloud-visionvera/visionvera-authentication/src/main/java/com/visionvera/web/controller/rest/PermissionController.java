package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.TPermissionVO;
import com.visionvera.common.api.authentication.PermissionAPI;
import com.visionvera.service.PermissionService;
import com.visionvera.util.StringUtil;

/**
 * 资源权限操作Controller
 *
 */
@RestController
public class PermissionController extends BaseReturn implements PermissionAPI {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PermissionService permissionService;
	
	/**
	 * 通过平台类别查询功能资源树(权限列表)根节点
	 * @param platformType 平台类别
	 * @return
	 */
	@RequestMapping(value = "/{platformType}/getRootPermission", method = RequestMethod.GET)
	public ReturnData getRootPermission(@PathVariable("platformType") String platformType) {
		try {
			List<TPermissionVO> rootPermissionList = this.permissionService.getRootPermissionByPlatformType(platformType);
			return super.returnResult(0, "查询成功", null, rootPermissionList);
		} catch (Exception e) {
			this.LOGGER.error("通过平台类别查询功能资源树的根节点失败 ===== PermissionController ===== getRootPermission =====>", e);
			return super.returnError("查询资源树失败");
		}
	}
	
	/**
	 * 通过上级目录ID查询其对应的子权限信息
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "/{pid}/getChildrenPermission", method = RequestMethod.GET)
	public ReturnData getChildrenPermission(@PathVariable("pid") String pid) {
		try {
			List<TPermissionVO> childrenPermissionList = this.permissionService.getChildrenPermissionByPid(pid);
			return super.returnResult(0, "查询成功", null, childrenPermissionList);
		} catch (Exception e) {
			this.LOGGER.error("通过上级目录ID获取其子菜单 ===== PermissionController ===== getChildrenPermission =====>", e);
			return super.returnError("查询资源树失败");
		}
	}
	
	/**
	 * 通过主键UUID查询权限信息
	 * @param uuid 主键UUID
	 * @return
	 */
	@RequestMapping(value = "/{uuid}/getPermission", method = RequestMethod.GET)
	public ReturnData getPermission(@PathVariable("uuid") String uuid) {
		try {
			TPermissionVO permission = this.permissionService.getPermission(uuid);
			return super.returnResult(0, "查询成功", null, null, permission);
		} catch (Exception e) {
			this.LOGGER.error("通过主键UUID查询权限信息 ===== PermissionController ===== getPermission =====>", e);
			return super.returnError("查询资源失败");
		}
	}
	
	/**
	 * 通过主键UUID查询权限描述信息
	 * @param permission 权限信息
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData editPermission(@RequestBody TPermissionVO permission) {
		try {
			if (StringUtil.isNull(permission.getUuid())) {
				return super.returnError("主键UUID不能为空");
			}
			this.permissionService.updatePermissionById(permission);
			return super.returnResult(0, "修改成功");
		} catch (Exception e) {
			this.LOGGER.error("修改权限失败 ===== PermissionController ===== editPermission =====>", e);
			return super.returnError("修改权限失败");
		}
	}
	
	/**
	 * 通过平台类型和角色ID获取角色在该平台下的权限信息
	 * @param systemId 平台ID
	 * @param roleId 角色ID
	 * @return
	 */
	@Override
	public ReturnData getRolePermissionExist(
			@PathVariable("systemId") String systemId, 
			@PathVariable("roleId") String roleId) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("roleId", roleId);
			paramsMap.put("systemId", systemId);
			List<TPermissionVO> permissionList = this.permissionService.getRolePermissionExist(paramsMap);
			return super.returnResult(0, "获取成功", null, permissionList);
		} catch (Exception e) {
			this.LOGGER.error("获取角色对应的权限失败 ===== PermissionController ===== getRolePermissionExist =====>", e);
			return super.returnError("获取角色对应的权限失败");
		}
	}
	
	@RequestMapping(value = "/{platformType}/getAllPermission", method = RequestMethod.GET)
	public ReturnData getAllPermission(@PathVariable("platformType") String platformType) {
		try {
			List<TPermissionVO> permissionList= this.permissionService.getAllPermissionByPlatformType(platformType);//查询该平台下所有的权限信息
			return super.returnResult(0, "获取成功", null, permissionList);
		} catch (Exception e) {
			this.LOGGER.error("获取该平台下的所有权限失败 ===== PermissionController ===== getAllPermission =====>", e);
			return super.returnError("获取该平台下的所有权限失败");
		}
	}
}
