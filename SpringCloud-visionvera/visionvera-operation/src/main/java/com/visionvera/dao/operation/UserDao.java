package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.alibaba.druid.sql.visitor.functions.Function;
import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.PhoneVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.Role;
import com.visionvera.bean.cms.RoleVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.UserAccessTokenVO;
import com.visionvera.bean.cms.UserApproveVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.cms.WorkAreaVO;
import com.visionvera.bean.cms.WorkDepartVO;
import com.visionvera.bean.cms.WorkUnitVO;


public interface UserDao {

	/**
	 * 
	 * @Title: getUserList 
	 * @Description: TODO 获取用户列表 
	 * @param @return  参数说明 
	 * @return List<UserVO>    返回类型 
	 * @throws
	 */
	List<UserVO> getUserList(Map<String,Object> map, RowBounds rowBounds);
	
	
	/**
	 * 
	 * @Title: getUserListCount 
	 * @Description: 获取用户列表总条目
	 * @param @param map selct条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getUserListCount(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: getEditUserList 
	 * @Description: TODO 获取用户列表（供新增、修改页面的选择页使用）
	 * @param @return  参数说明 
	 * @return List<UserVO>    返回类型 
	 * @throws
	 */
	List<UserVO> getEditUserList(Map<String,Object> map);

	/**
	 * 
	 * @Title: getUserFunctions 
	 * @Description: 获取用户的功能权限 
	 * @param @param userid
	 * @param @return  参数说明 
	 * @return List<Function>    返回类型 
	 * @throws
	 */
	List<Function> getUserFunctions(String userid);
	
	/**
	 * 
	 * @Title: getUserRoles 
	 * @Description: 获取用户具有的权限 
	 * @param @param userid
	 * @param @return  参数说明 
	 * @return List<Role>    返回类型 
	 * @throws
	 */
	List<Role> getUserRoles(String userid);
	
	/**
	 * 
	 * @Title: addUser 
	 * @Description: TODO 新增用户
	 * @param @param user
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addUser(UserVO user);
	
	/**
	 * 
	 * @Title: getOAUser 
	 * @Description: TODO 查询OA通讯录中是否有指定用户
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<UserVO> getOAUser(UserVO user);

	/**
	 * 
	 * @Title: addOAUser 
	 * @Description: TODO 将新增的用户信息插入OA通讯录中
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addOAUser(UserVO user);

	/**
	 * 
	 * @Title: updateOAUser 
	 * @Description: TODO 更新OA通讯录用户信息 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateOAUser(UserVO user);
	
	/**
	 * 
	 * @Title: addUserRole 
	 * @Description: TODO 添加用户角色 
	 * @param @param userid 用户uuid
	 * @param @param role 角色uuid集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addUserRole(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: updateUser 
	 * @Description: TODO 更新用户信息 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateUser(UserVO user);
	/**
	 * 
	 * @Title: deleteUser 
	 * @Description: TODO 删除用户 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteUser(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: deleteUserRole 
	 * @Description: TODO 删除用户角色关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteUserRole(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: deleteNoExistUserRole 
	 * @Description: 删除不存在的用户角色关联 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteNoExistUserRole(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: getUserbyLoginName 
	 * @Description: 根据用户名和密码获取用户 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<UserVO>    返回类型 
	 * @throws
	 */
	List<UserVO> getUserbyLoginName(Map<String,Object> map);

	/**
	 * 
	 * @Title: getUserbyUuid 
	 * @Description: 根据用户uuid获取用户 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<UserVO> getUserbyUuid(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: getAllRoles 
	 * @Description: 获取所有角色列表 
	 * @param @return  参数说明 
	 * @return List<RoleVO>    返回类型 
	 * @throws
	 */
	List<RoleVO> getAllRoles();
	
	/**
	 * 
	 * @Title: checkLoginName 
	 * @Description: 验证登录名是否重复
	 * @param @param loginName
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkLoginName(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: checkName 
	 * @Description: 验证用户名称是否重复
	 * @param @param name
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkName(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: onlineUserCount 
	 * @Description: 在线用户统计 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int onlineUserCount();
	
	/**
	 * @param map 
	 * 
	 * @Title: getAllUsers 
	 * @Description: 获取所有用户 
	 * @param @return  参数说明 
	 * @return List<UserVO>    返回类型 
	 * @throws
	 */
	List<UserVO> getAllUsers(Map<String, Object> map);
	
	
	/**
	 * 
	 * @Title: updateUserRole 
	 * @Description: 更新用户角色关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateUserRole(Map<String,Object> map);

	/**
	 * 
	 * @Title: resetAdmPwd 
	 * @Description: 强制重置超级管理员密码 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int resetAdmPwd(Map<String, Object> map);

	/**
	 * @param rowBounds 
	 * 
	 * @Title: getUserGroupList 
	 * @Description: 获取用户分组列表 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<UserGroupVO> getUserGroupList(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 
	 * @Title: addUserGroup 
	 * @Description: 新增用户分组信息
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addUserGroup(UserGroupVO userGroup);

	/**
	 * 
	 * @Title: deleteUserGroup 
	 * @Description: 删除用户分组信息
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteUserGroup(Map<String, Object> map);

	/**
	 * 
	 * @Title: isUserInGroupOnline 
	 * @Description: 判断用户组内是否有用户正处于登录状态
	 * @param @param map
	 * @param @return  参数说明 
	 * @return UserVO    返回类型 
	 * @throws
	 */
	List<UserVO> isUserInGroupOnline(Map<String, Object> map);

	/**
	 * 
	 * @Title: isAdmin 
	 * @Description: 根据用户ID判断是否包含admin用户
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int isAdmin(Map<String, Object> map);

	/**
	 * 
	 * @Title: isUserOnline 
	 * @Description: 判断用户是否处于登录状态
	 * @param @param map
	 * @param @return  参数说明 
	 * @return UserVO    返回类型 
	 * @throws
	 */
	List<UserVO> isUserOnline(Map<String, Object> map);

	/**
	 * 
	 * @Title: isScheduleValid 
	 * @Description: 判断用户组是否有有效预约
	 * @param @param map
	 * @param @return  参数说明 
	 * @return ScheduleVO    返回类型 
	 * @throws
	 */
	List<ScheduleVO> isScheduleValid(Map<String, Object> map);

	/**
	 * 
	 * @Title: updateUserGroup 
	 * @Description: 更新用户分组信息 
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateUserGroup(UserGroupVO userGroup);

	/**
	 * 
	 * @Title: getUserGroupListCount 
	 * @Description: 获取用户分组列表总数
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getUserGroupListCount(Map<String, Object> map);

	/**
	 * 
	 * @Title: addUser2Group 
	 * @Description: 添加用户分组和用户的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addUser2Group(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: deleteUserGroup
	 * @Description: 删除用户分和用户组的关联（通过userid删除）
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteUser2Group(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: addDeviceGroup2Group 
	 * @Description: 添加用户分组和设备组的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDeviceGroup2Group(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: deleteDeviceGroup2Group 
	 * @Description: 删除用户分组和设备组的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteDeviceGroup2Group(Map<String, Object> paramsMap);

	/**
	 * @param map 
	 * 
	 * @Title: getAllGroups 
	 * @Description: 获取所有用户分组
	 * @param @param map
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<UserGroupVO> getAllGroups(Map<String, Object> map);

	/**
	 * 
	 * @Title: getUserGroupByLoginName 
	 * @Description: 根据用户账号获取用户组ID
	 * @param @param map
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	String getUserGroupByLoginName(Map<String, Object> map);

	/**
	 * 
	 * @Title: avaliableUsers
	 * @Description: 获取所有未分组用户
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<UserVO> avaliableUsers(Map<String, Object> map);

	/**
	 * 
	 * @Title: devicesInGroup
	 * @Description: 获取组内已添加设备（一个用户组可含多个设备分组）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> devicesInGroup(Map<String, Object> map);

	/**
	 * 
	 * @Title: checkRepeatGroup 
	 * @Description: 验证用户名组称是否重复
	 * @param @param name
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkRepeatGroup(Map<String, Object> map);


	/**
	 * 
	 * @Title: devGroupsInGroup
	 * @Description: 获取组内已添加设备分组（一个用户组可含多个设备分组）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> devGroupsInGroup(Map<String, Object> paramsMap);
	
	/**
	 * 审核webService注册用户是否允许登录
	 * @Title: adjustUser 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int approveUser(Map<String, Object> paramsMap);
	
	/**
	 * 更新用户与组的关联信息
	 * @Title: updateUserWithGroup 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateUserWithGroup(Map<String, Object> paramsMap);
	
	/**
	 * 更新用户基本信息
	 * @Title: updateUserInfo 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param user
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateUserInfo(UserApproveVO user);
	
	/**
	 * 根据用户id获取所在用户组ID
	 * @Title: getUserGroupByUserId 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return String    返回类型 
	 * @throws
	 */
	String getUserGroupByUserId(Map<String, Object> paramsMap);
	
	/**
	 * 验证用户是否有超级管理员权限
	 * @Title: checkSuperAuthorById 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkSuperAuthorById(Map<String, Object> paramsMap);
	
	/**
	 * 验证用户是否有管理员权限
	 * @Title: checkAdminAuthorById 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkAdminAuthorById(Map<String, Object> paramsMap);
	
	/**
	 * 改变普通用户成为管理员角色 
	 * @Title: changeUserToManager 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int changeUserToManager(Map<String, Object> paramsMap);
	
	/**
	 * 批量删除用户和角色关联根据用户id
	 * @Title: delUserWithRoleMany 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int delUserWithRoleMany(Map<String, Object> paramsMap);
	
	/**
	 * 用户是否有效
	 * @Title: checkUserisValid 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkUserisValid(String userid);
	
	/**
	 * 获取带审批用户列表
	 * @Title: getApproveUsers 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return List<UserVO>    返回类型 
	 * @throws
	 */
	List<UserVO> getApproveUsers(Map<String, Object> paramsMap, RowBounds rowBounds);
	
	/**
	 * 单条插入用户和用户组关联
	 * @Title: addUserToGroupSingle 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addUserToGroupSingle(Map<String, Object> paramsMap);
	
	/**
	 * 删除指定用户和角色关联信息
	 * @Title: delUserWithRoleSingle 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int delUserWithRoleSingle(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: addToUserApprove 
	 * @Description: 向用户待审核表中添加数据 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addToUserApprove(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: delUserFromApproves 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param userid
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int delUserFromApproves(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: resetUserPwd 
	 * @Description: 更新用户密码
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int resetUserPwd(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: webserviceLogin 
	 * @Description: weservice登录
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<UserVO>    返回类型 
	 * @throws
	 */
	List<UserVO> webserviceLogin(Map<String,Object> paramsMap);

	/**
	 * 
	 * @Title: setWsLoginState 
	 * @Description: 更新用户状态-是否从webservice登录过（0表示没有，1表示有）
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int   返回类型 
	 * @throws
	 */
	int setWsLoginState(Map<String,Object> paramsMap);
	
	/**
	 * 
	 * @Title: getUnitDepartByUnitId 
	 * @Description: 获取工作单位包含的工作部门 
	 * @param @param paramsMap key:unitid工作单位id
	 * @param @return  参数说明 
	 * @return List<WorkDepartVO>    返回类型 
	 * @throws
	 */
	List<WorkDepartVO> getUnitDepartByUnitId(Map<String,Object> paramsMap);
	
	/**
	 * 
	 * @Title: getUnitAreaByUnitId 
	 * @Description: 获取工作单位包含的工作区域 
	 * @param @param paramsMap key:unitid工作单位id
	 * @param @return  参数说明 
	 * @return List<WorkAreaVO>    返回类型 
	 * @throws
	 */
	List<WorkAreaVO> getUnitAreaByUnitId(Map<String,Object> paramsMap);
	
	/**
	 * 
	 * @Title: getAllUnits 
	 * @Description: 获取所有工作单位 
	 * @param @return  参数说明 
	 * @return List<WorkUnitVO>    返回类型 
	 * @throws
	 */
	List<WorkUnitVO> getAllUnits();
	
	/**
	 * 
	 * @Title: delUserWithUserGroupByUserId 
	 * @Description: 删除用户与用户分组关联通过用户id 
	 * @param @param paramsMap key:userid
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int delUserWithUserGroupByUserId(Map<String,Object> paramsMap);
	
	/**
	 * 
	 * @Title: rejectUserApproved 
	 * @Description: 驳回用户提交的审核操作
	 * @param @param paramsMap key:userid
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int rejectUserApproved(Map<String,Object> paramsMap);
	
	/**
	 * 
	 * @Title: checkUserExist 
	 * @Description: 校验用户是否存在 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkUserExist(Map<String,Object> paramsMap);

	/**
	 * 
	 * @Title: setVerifiCode 
	 * @Description: 根据手机号保存验证码
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int setVerifiCode(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: chkVerifiCode 
	 * @Description: 根据手机号判断验证码是否正确
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<PhoneVO> chkVerifiCode(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: delPhone 
	 * @Description: 删除手机号对应的数据
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int delPhone(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: updateUserIsValid 
	 * @Description: 更新用户是否有效标识符
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateUserIsValid(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: addApprovesWhenUpdateInfo 
	 * @Description: 追加普通用户更新信息到申请更新表中 
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addApprovesWhenUpdateInfo(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: checkUserLogin 
	 * @Description: 验证用户是否登录
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkUserLogin(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: setImg 
	 * @Description: 设置用户头像url
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int setImg(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: setAccessToken 
	 * @Description: 保存用户的接口调用凭据
	 * @param @param UserAccessTokenVO
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int setAccessToken(UserAccessTokenVO userAccessTokenVO);
	
	/**
	 * 
	 * @Title: delAccessToken 
	 * @Description: 删除用户的接口调用凭据
	 * @param @param UserAccessTokenVO
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int delAccessToken(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: createAccessToken 
	 * @Description: 生成用户的接口调用凭据
	 * @param @param UserAccessTokenVO
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int createAccessToken(UserAccessTokenVO userAccessTokenVO);
	
	/**
	 * 
	 * @Title: getAccessToken 
	 * @Description: 获取用户的接口调用凭据
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getAccessToken(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: getUserByPhone 
	 * @Description: 根据手机号查询用户信息
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return List<UserVO>    返回类型 
	 * @throws
	 */
	List<UserVO> getUserByPhone(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: devGroupsInUse 
	 * @Description: 获取用户组下所有被占用的会场
	 * @param @param map
	 * @param @return  参数说明 
	 * @return map    返回类型 
	 * @throws
	 */
	List<String> devGroupsInUse(Map<String, Object> map);


	String selSession();
	
	/**
	 * 
	 * @author 褚英奇
	 * @param paramsMap
	 * @return UserVO
	 */
	UserVO getUserById(Map<String ,Object> paramsMap);
	
	int setIsvalid(String uuid);
	
	int setWebLogin(Map<String,Object> paramsMap);
	
	int deleteUserById(Map<String,Object> paramsMap);
	
	List<UserVO> getUserAuditList(Map<String,Object> map);
	
	int getUserAuditListCount(Map<String,Object> paramsMap);
	/**
	 * @return 
	 * 
	 * @Title: addUserBooks 
	 * @Description: 通讯录新增用户接口
	 * @param @param user
	 * @param @return  参数说明 
	 * @return void   返回类型 
	 * @throws
	 */
	int addUserBooks(UserVO user);

	/**
	 * @return 
	 * 
	 * @Title: addUserDevice 
	 * @Description: 通讯录新增用户关联设备接口
	 * @param @param user
	 * @param @return  参数说明 
	 * @return int   返回类型 
	 * @throws
	 */
	int addUserDevice(Map<String, Object> paramsMap);

	/**
	 * @return 
	 * 
	 * @Title: addUserDeviceGroup 
	 * @Description: 通讯录新增用户关联设备群组接口
	 * @param @param user
	 * @param @return  参数说明 
	 * @return int   返回类型 
	 * @throws
	 */
	int addUserDeviceGroup(Map<String, Object> paramsMap);

	/**
	 * @return 
	 * 
	 * @Title: getUserInfo 
	 * @Description: 通讯录新增用户信息接口
	 * @param @param user
	 * @param @return  参数说明 
	 * @return int   返回类型 
	 * @throws
	 */
	List<UserVO> getUserInfo(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * @return 
	 * 
	 * @Title: getUserInfoCount 
	 * @Description: 通讯录获取用户信息总条数
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int   返回类型 
	 * @throws
	 */
	int getUserInfoCount(Map<String, Object> paramsMap);

	/**
	 * @return 
	 * 
	 * @Title: updateUserBooks 
	 * @Description: 更新用户信息
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int   返回类型 
	 * @throws
	 */
	int updateUserBooks(Map<String, Object> paramsMap);

	/**
	 * @return 
	 * 
	 * @Title: deleteUserDevice 
	 * @Description: 删除用户与设备关联信息
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int   返回类型 
	 * @throws
	 */
	int deleteUserDevice(Map<String, Object> paramsMap);

	/**
	 * @return 
	 * 
	 * @Title: deleteUserDevice 
	 * @Description: 删除用户与设备群组关联信息
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int   返回类型 
	 * @throws
	 */
	int deleteUserDeviceGroup(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取用户定制列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	List<UserVO> getCustomizedUserList(Map<String, Object> map,
			RowBounds rowBounds);

	/**
	 * 
	 * TODO 获取用户定制列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	int getCustomizedUserCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取用户定制私有联系人列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getCustomizedDevList(Map<String, Object> map,
			RowBounds rowBounds);

	/**
	 * 
	 * TODO 获取用户定制私有联系人列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int getCustomizedDevCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取用户定制私有群列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	List<DeviceGroupVO> getCustomizedDevGroupList(Map<String, Object> map,
			RowBounds rowBounds);

	/**
	 * 
	 * TODO 获取用户定制私有群列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	int getCustomizedDevGroupCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取用户定制私有群成员列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getCustomizedGroupMembers(Map<String, Object> map,
			RowBounds rowBounds);

	/**
	 * 
	 * TODO 获取用户定制私有群成员列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int getCustomizedGroupMembersCount(Map<String, Object> paramsMap);
	
	List<UserVO> getUserListInfo(Map<String, Object> paramMap);

	List<UserVO> getUserInformation();
	
	int addUserInfo(List<Object> list);
	
	List<UserVO> getUserByIds(Map<String, Object> paramMap);
	
	List<UserVO> getUserByUuid(String uuid);

	UserVO getUserByLoginName(Map<String, Object> paramsMap);

	/** <pre>getUserRegionList(获取现有用户的来源-行政区域)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年12月12日 下午7:16:52    
	 * 修改人：周逸芳        
	 * 修改时间：2017年12月12日 下午7:16:52    
	 * 修改备注： 
	 * @return</pre>    
	 */
	List<RegionVO> getUserRegionList(Map<String, Object> paramsMap);

	/** <pre>getNameByLoginName(根据用户登录名获取真实名称)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年12月12日 下午7:16:52    
	 * 修改人：周逸芳        
	 * 修改时间：2017年12月12日 下午7:16:52    
	 * 修改备注： 
	 * @return</pre>    
	 */
	UserVO getNameByLoginName(String loginName);

	/** <pre>getTokenByLoginName(根据用户登录名获取真实名称)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月20日 下午7:16:52    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月20日 下午7:16:52    
	 * 修改备注： 
	 * @return</pre>    
	 */
	String getTokenByLoginName(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: updateMaxDevNumByLoginName 
	 * @Description: TODO 更新用户最大参会数
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateMaxDevNumByLoginName(UserVO user);
	
}
