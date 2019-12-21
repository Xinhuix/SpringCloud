package com.visionvera.dao.authentication;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.cms.IndustryVO;
import com.visionvera.bean.cms.PhoneVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.RoleVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;

/**
 * 会管DAO
 *
 */
public interface CMSDao {
	/**
	 * 根据时间段获取该时间段内所又的会议数量
	 * @param paramsMap
	 * @return
	 */
	int selectMeetingNum(Map<String, Object> paramsMap);
	
	/**
	 * 通过数据库表的唯一键获取用户数据
	 * @param paramsMap
	 * @return
	 */
	UserVO selectUserByUniqueKey(Map<String, Object> paramsMap);
	
	/**
	 * 获取Session失效时间
	 * @return
	 */
	String selectSessionTimeout();
	
	/**
	 * 通过手机号和验证码查询验证信息
	 * @param paramsMap
	 * @return
	 */
	List<PhoneVO> selectPhoneByPhoneAndVriCode(Map<String, Object> paramsMap);
	
	/**
	 * 通过数据库表的唯一键获取用户的数量
	 * @param paramsMap
	 * @return
	 */
	int selectUserCountByUniqueKey(Map<String, Object> paramsMap);
	
	/**
	 * 插入用户基本信息
	 * @param user
	 * @return
	 */
	int insertUser(UserVO user);
	
	/**
	 * 查询OA通讯录中是否有指定用户
	 * @param user
	 * @return
	 */
	UserVO selectOAUserByName(UserVO user);
	
	/**
	 * 向OA通讯录中插入用户
	 * @param user
	 * @return
	 */
	int insertOAUser(UserVO user);
	
	/**
	 * 新增用户角色关联
	 * @param paramsMap
	 * @return
	 */
	int insertUserRole(Map<String, Object> paramsMap);
	
	/**
	 * 新增用户组
	 * @param userGroup
	 * @return
	 */
	int insertUserGroup(UserGroupVO userGroup);
	
	/**
	 * 新增用户与组的关系
	 * @param paramsMap
	 * @return
	 */
	int insertUserGroupRel(Map<String, Object> paramsMap);
	
	/**
	 * 添加用户组与设备组的关联
	 * @param paramsMap
	 * @return
	 */
	int insertGroupDevGroupRel(Map<String, Object> paramsMap);
	
	/**
	 * 根据用户ID与客户端类型删除access_token
	 * @param paramsMap
	 * @return
	 */
	int deleteAccessToken(Map<String, Object> paramsMap);
	
	/**
	 * 更新用户基本信息
	 * @param user
	 * @return
	 */
	int updateUser(UserVO user);
	
	/**
	 * 根据用户ID删除用户与角色的关联关系
	 * @param paramsMap
	 * @return
	 */
	int deleteUserRoleRelByUserId(Map<String, Object> paramsMap);
	
	/**
	 * 查询是否存在超级管理员admin
	 * @param paramsMap
	 * @return 大于0表示存在
	 */
	int selectAdminCount(Map<String, Object> paramsMap);
	
	/**
	 * 查询用户是否处于登录状态
	 * @param paramsMap
	 * @return
	 */
	UserVO selectUserOnLine(Map<String, Object> paramsMap);
	
	/**
	 * 删除用户
	 * @param paramsMap
	 * @return
	 */
	int deleteUser(Map<String, Object> paramsMap);
	
	/**
	 * 获取用户列表
	 * @param paramsMap
	 * @param rowBounds
	 * @return
	 */
	List<UserVO> selectUserByCondition(UserVO user);
	
	/**
	 * 查询所有用户角色
	 * @return
	 */
	List<RoleVO> selectAllRoles();
	
	/**
	 * 查询现有用户的所有行政区域
	 * @return
	 */
	List<RegionVO> selectUserRegionList();
	
	/**
	 * 获取行政区域列表
	 * @param paramsMap
	 * @return
	 */
	List<RegionVO> selectRegions(Map<String, Object> paramsMap);
	
	/**
	 * 根据用户的主键UUID查询用户信息。关联出其角色和组的信息
	 * @param uuid
	 * @return
	 */
	UserVO selectUserByUUID(String uuid);
	
	/**
	 * 通过条件查询行业归属信息
	 * @param industry 行业归属信息查询条件
	 * @return
	 */
	List<IndustryVO> selectIndustryByCondition(IndustryVO industry);
	
	/**
	 * 批量添加用户可登陆平台信息
	 * @param paramsMap {"userId":"", "platformIds":""}
	 * @return
	 */
	int insertUserPlatform(Map<String, Object> paramsMap);
	
	/**
	 * 批量添加用户行业归属信息
	 * @param paramsMap {"userId":"", "industryIds":""}
	 * @return
	 */
	int insertUserIndustry(Map<String, Object> paramsMap);
	
	/**
	 * 删除用户与可登陆平台的关系
	 * @param paramsMap {"userId":"", "platformId":""}
	 * @return
	 */
	int deleteUserPlatform(Map<String, Object> paramsMap);
	
	/**
	 * 删除用户与行业归属的关系
	 * @param paramsMap {"userId":"", "industryId":""}
	 * @return
	 */
	int deleteUserIndustry(Map<String, Object> paramsMap);
}
