package com.visionvera.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.visionvera.constrant.PlatformTypeConstrant;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.PlatformResourceVO;
import com.visionvera.bean.datacore.TPermissionVO;
import com.visionvera.bean.datacore.TRoleVO;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.resource.PlatformResDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.feign.PermissionService;
import com.visionvera.feign.RoleService;
import com.visionvera.feign.UserService;
import com.visionvera.service.PlatformResService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.ReturnDataUtil;
import com.visionvera.util.StringUtil;
/**
 * 平台资源信息Service
 * @author dql
 *
 */
@Service
@Transactional(value = "transactionManager_resource", rollbackFor = Exception.class)
public class PlatformResServiceImpl implements PlatformResService {
	private static final Logger logger = LogManager.getLogger(PlatformResServiceImpl.class);
	
	@Autowired
	private PlatformResDao platformResDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PermissionService permissionService;
	
	private final String DEFAULT_ROLE_NAME = "默认角色";

	@Override
	public void insertPlatformResource(PlatformResourceVO platformResVO) {
		if(platformResDao.getRepeatPlatformName(platformResVO) > 0){
			logger.error("添加平台失败：平台名称重复");
			throw new RuntimeException("平台名称重复");
		}
		platformResDao.insertPlatformResource(platformResVO);
	}
	
	/**
	 * 添加平台资源
	 * @param platformResource 平台资源信息
	 * @param token 访问令牌
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ReturnData addPlatformResource(PlatformResourceVO platformResource, String token) {
		BaseReturn dataReturn = new BaseReturn();
		String platformId = StringUtil.get32UUID();//平台ID
		
		//校验平台名称是否重复
		if (this.platformResDao.getRepeatPlatformName(platformResource) > 0) {
			throw new BusinessException("平台名称重复");
		}
		
		//限制只能添加一个会管平台
		if(platformResource.getDevType().equals(6)){//会管平台类型
			boolean isDuplicationPlatform = this.checkIsDuplicationPlatformByType("6");
			if (!isDuplicationPlatform) {
				return dataReturn.returnError("暂时只支持添加一个会管平台");
			}
		} else if (platformResource.getDevType().equals(5)) {//流媒体平台类型
			boolean isDuplicationPlatform = this.checkIsDuplicationPlatformByType("5");
			if (!isDuplicationPlatform) {
				return dataReturn.returnError("暂时只支持添加一个流媒体平台");
			}
		}

		//补齐平台资源数据 Start
		platformResource.setId(platformId);//ID
		ReturnData userData = userService.getUser(token);//获取缓存用户信息(即登录的用户信息), 走Feign接口
		if (!userData.getErrcode().equals(0)) {
			return dataReturn.returnError(userData.getErrmsg());
		}
		Map<String,Object> userMap = (Map<String,Object>)((Map<String,Object>)userData.getData()).get("extra");
		platformResource.setCreateid((String)userMap.get("uuid"));
		platformResource.setCreateName((String)userMap.get("loginName"));
		platformResource.setCreatetime(DateUtil.date2String(new Date()));
		//补齐平台资源数据 End
		
		//插入平台资源 Start
		int platformCount = this.platformResDao.insertPlatformResource(platformResource);
		if (platformCount <= 0) {
			throw new BusinessException("添加平台失败");
		}
		//插入平台资源 End
		
		//添加平台对应的默认角色
		this.addDefaultRole(platformId, platformResource.getDevName(), platformResource.getDevType() + "", token);
		return dataReturn.returnResult(0, "添加平台资源成功");
	}
	
	/**
	 * 校验是否有相同平台类别的平台信息
	 * @param devType 平台类别
	 */
	private boolean checkIsDuplicationPlatformByType(String devType) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("devType", devType.split(","));
		List<PlatformResourceVO> platformResourceList = platformResDao.getPlatformResource(paramMap);
		if(platformResourceList != null && platformResourceList.size() > 0){
			return false;
		}
		return true;
	}

	@Override
	public List<PlatformResourceVO> getPlatformResource(
			Map<String, Object> paramMap) {
		Integer pageNum = (Integer)paramMap.get("pageNum");
		Integer pageSize = (Integer)paramMap.get("pageSize");
		
		List<PlatformResourceVO> platformResourceList = null;
		if(pageNum != -1) {
			PageHelper.startPage(pageNum, pageSize);
		}
		platformResourceList = platformResDao.getPlatformResource(paramMap);
		if(platformResourceList == null) {
			Collections.emptyList();
		}
		return platformResourceList;
	}

	@Override
	public Map<String,Object> updatePlatformResource(PlatformResourceVO platformResVO) throws Exception {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String id = platformResVO.getId();
		//限制只能添加一个会管平台
		if(platformResVO.getDevType().equals(6)){//会管平台类型
			boolean isDuplicationPlatform = this.checkIsDuplicationPlatform("6", id);
			if (!isDuplicationPlatform) {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "暂时只支持添加一个会管平台");
				return resultMap;
			}
		} else if (platformResVO.getDevType().equals(5)) {//流媒体平台类型
			boolean isDuplicationPlatform = this.checkIsDuplicationPlatform("5", id);
			if (!isDuplicationPlatform) {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "暂时只支持添加一个流媒体平台");
				return resultMap;
			}
		}
		PlatformResourceVO platformResource = platformResDao.getPlatformResourceById(id);
		if(platformResource == null) {
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "要修改的平台不存在");
			return resultMap;
		}
		if(platformResDao.getRepeatPlatformName(platformResVO) > 0){
			logger.error("添加平台失败：平台名称重复");
			throw new RuntimeException("平台名称重复");
		}
		BeanUtils.copyProperties(platformResource, platformResVO, "id","devName","devType","url","updateid","updateName","updatetime");
		
		platformResDao.updatePlatformResource(platformResVO);
		
		this.updateDefaultRole(platformResVO.getId(), platformResVO.getDevName());//更新其对应的默认角色
		
		resultMap.put("errcode", 0);
		resultMap.put("errmsg", "修改平台资源信息成功");
		return resultMap;
	}
	
	/**
	 * 校验平台是否存在
	 * @param devType 平台类别
	 * @param platformId 平台ID
	 * @return
	 */
	private boolean checkIsDuplicationPlatform(String devType, String platformId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("devType", devType.split(","));
		paramMap.put("platformId", platformId);
		List<PlatformResourceVO> platformResourceList = platformResDao.getPlatformResource(paramMap);
		if(platformResourceList != null && platformResourceList.size() > 0){
			return false;
		}
		return true;
	}

	@Override
	public void deletePlatformResource(String id) {
		platformResDao.deletePlatformResource(id);
		this.deleteDefaultRole(id);
	}

	/**
	 * 通过主键ID获取平台资源信息
	 * @param id 主键ID
	 * @return
	 */
	@Override
	public PlatformResourceVO getPlatformResourceById(String id) {
		return this.platformResDao.getPlatformResourceById(id);
	}
	
	/**
	 * 添加默认的平台角色
	 * @param platformId 平台ID
	 * @param devName 平台名称
	 * @param platformType 平台类别
	 * @param token 访问令牌
	 */
	private void addDefaultRole(String platformId, String devName, String platformType, String token) {
		String roleName = devName + "-" + this.DEFAULT_ROLE_NAME;
		if (PlatformTypeConstrant.MEDIA_SERVER_PLATFORM_TYPE.equals(platformType)) {//如果是流媒体
			//创建平台对应的默认角色,
			TRoleVO role = this.getRoleByFiled(roleName, roleName, platformId, GlobalConstants.MEDIA_SERVER_LOGIN_PERMISSION_ID,
					CommonConstrant.DEFAULT_IS_DEFAULT_TYPE, CommonConstrant.NON_DELETE_IS_DELETE_TYPE);
				ReturnData addRoleReturnData = this.roleService.addRole(role, token);//添加角色、平台角色关系、角色平台权限关系
			if (!addRoleReturnData.getErrcode().equals(0)) {
				throw new BusinessException("添加默认角色失败");
			}
		} else if (PlatformTypeConstrant.CMS_WEB_PLATFORM_TYPE.equals(platformType)) {//会管平台，将运维工作站为会管平台创建的两个固定角色(会管超级管理员和会管普通用户角色)分配平台
			//添加会管超级管理员角色平台关系和会管超级管理员平台权限关系 Start
			TRoleVO role = new TRoleVO();
			role.setUuid(GlobalConstants.INTELLIGENT_OPERATION_ADMIN_ROLE_ID);//会管超级管理员
			role.setPlatformId(platformId);//平台ID
			role.setPermissionId(GlobalConstants.INTELLIGENT_OPERATION_ADMIN_PERMISSION_ID);//概念上的超级管理员权限
			
			ReturnData addRoleOtherRel = this.roleService.addRoleOtherRel(role, token);//添加角色、平台关系和角色平台权限关系
			if (!addRoleOtherRel.getErrcode().equals(0)) {
				throw new BusinessException("添加默认角色失败");
			}
			//添加会管超级管理员角色平台关系和会管超级管理员平台权限关系 End
			
			//添加会管普通用户角色平台关系和会管普通用户角色平台权限关系 Start
			role.setUuid(GlobalConstants.INTELLIGENT_OPERATION_ORDINARY_ROLE_ID);//会管超级管理员
			role.setPermissionId(GlobalConstants.INTELLIGENT_OPERATION_ORDINARY_PERMISSION_ID);//概念上的预约操作员权限
			addRoleOtherRel = this.roleService.addRoleOtherRel(role, token);//添加角色、平台关系和角色平台权限关系
			if (!addRoleOtherRel.getErrcode().equals(0)) {
				throw new BusinessException("添加默认角色失败");
			}
			//添加会管普通用户角色平台关系和会管普通用户角色平台权限关系 End
		} else if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(platformType) ||
				PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE.equals(platformType) ||
				PlatformTypeConstrant.VSDC_PLATFORM_TYPE.equals(platformType)) {//网管(8)、会易通(9)、一机一档(10)平台
			platformId = platformId + "," + GlobalConstants.ME_APP_PLATFORM_ID;
			String permissionIds = "";//权限ID
			if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(platformType)) {
				//网管平台的权限：网管登录权限和视联汇APP的网管模块权限
				permissionIds = GlobalConstants.NET_MANAGER_LOGIN_PERMISSION_ID + ";" +
						GlobalConstants.NETWORK_MANAGER_ME_APP_PERMISSION_ID;
			} else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE.equals(platformType)) {
				//会易通平台的权限：会易通登录权限和视联汇APP的会易通模块权限
				permissionIds = GlobalConstants.HUIYITONG_LOGIN_PERMISSION_ID + ";" +
						GlobalConstants.HUIYITONG_ME_APP_PERMISSION_ID;
			} else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE.equals(platformType)) {
				//一机一档平台的权限：一机一档登录权限和视联汇APP的一机一档模块权限
				permissionIds = GlobalConstants.VSDC_LOGIN_PERMISSION_ID + ";" +
						GlobalConstants.VSDC_ME_APP_PERMISSION_ID;
			}
			//创建平台对应的默认角色: 该角色对应该平台的登录功能(用户对应的该平台服务列表)和登录视联汇APP{网管、会易通、一机一档}模块的功能
			TRoleVO role = this.getRoleByFiled(roleName, roleName, platformId, permissionIds,
					CommonConstrant.DEFAULT_IS_DEFAULT_TYPE, CommonConstrant.NON_DELETE_IS_DELETE_TYPE);
			ReturnData addRoleReturnData = this.roleService.addRole(role, token);//添加角色、平台角色关系、角色平台权限关系
			if (!addRoleReturnData.getErrcode().equals(0)) {
				throw new BusinessException("添加默认角色失败");
			}
		}
	}

	/**
	 * 通过字段获取TRoleVO对象信息
	 * @param roleName 角色名称
	 * @param desc 角色的秒数
	 * @param platformId 平台ID，多个平台使用英文逗号","分隔
	 * @param permissionId 权限ID，多个权限ID使用英文分号";"分隔
	 * @param isDefault 是否是默认角色。一个平台只能有一个默认角色。1表示是默认角色，0表示不是默认角色
	 * @param isDelete 是否可以删除。1表示不可以删除，0表示可以删除
	 * @return
	 */
	private TRoleVO getRoleByFiled(String roleName, String desc, String platformId,
								   String permissionId, String isDefault, String isDelete) {
		TRoleVO role = new TRoleVO();
		role.setRoleName(roleName);//平台名称 - 默认角色
		role.setDescription(roleName);//描述
		role.setPlatformId(platformId);//平台ID
		role.setPermissionId(permissionId);//流媒体登录权限
		role.setIsDefault(isDefault);//默认的平台角色
		role.setIsDelete(isDelete);//不能删除
		return role;
	}
	
	/**
	 * 修改平台信息的时候，同时更新其默认的角色信息
	 * @param platformId 平台ID
	 */
	private void updateDefaultRole(String platformId, String devName) {
		TRoleVO role = new TRoleVO();
		role.setPlatformId(platformId);
		role.setRoleName(devName + "-" + this.DEFAULT_ROLE_NAME);
		this.roleService.updateDfRoleName(role);//根据平台ID查询该平台默认的角色的名称
	}
	
	/**
	 * 通过平台ID删除对应的默认角色
	 * @param platformId 平台ID
	 */
	private void deleteDefaultRole(String platformId) {
		ReturnData resultData = this.roleService.getDefaultRoleByPlatformId(platformId);//根据平台ID查询该平台默认的角色
		
		if (resultData != null && resultData.getErrcode().equals(0)) {
			TRoleVO role = ReturnDataUtil.getExtraJsonObject(resultData, TRoleVO.class);
			if(role != null){
				ReturnData deleteRoleResultData = this.roleService.deleteRole(role);
				
				if (deleteRoleResultData == null || !deleteRoleResultData.getErrcode().equals(0)) {
					throw new BusinessException("更新失败");//回滚事务
				}
			}
		}
	}
	
	/**
	 * 通过平台ID和角色ID查询对应的权限列表，将权限ID以逗号方式拼接返回
	 * @param platformId 平台ID
	 * @param roleId 角色ID
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getPermissionIds(String platformId, String roleId) {
		String permissionIds = "";
		List<String> permissionIdList = new ArrayList<String>();//权限IDList
		ReturnData rolePermissionExist = this.permissionService.getRolePermissionExist(platformId, roleId);//查询会管超级管理员对应的智能运维平台的权限列表
		if (rolePermissionExist == null || !rolePermissionExist.getErrcode().equals(0)) {
			throw new BusinessException("添加失败");//回滚事务
		}
		
		 List<TPermissionVO> permissionList = ReturnDataUtil.getItemsList(rolePermissionExist, TPermissionVO.class);
		 if (permissionList != null && permissionList.size() > 0) {
			 for (TPermissionVO permission : permissionList) {
				permissionIdList.add(permission.getUuid());
			}
			permissionIds = StringUtils.join(permissionIdList.toArray(), ",");
		 }
		return permissionIds;
	}
}
