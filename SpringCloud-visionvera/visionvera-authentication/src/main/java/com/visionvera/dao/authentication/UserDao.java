package com.visionvera.dao.authentication;

import com.visionvera.bean.cms.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 用户操作DAO 接口
 *
 */
public interface UserDao {
	/**
	 * 通过数据库表的唯一键获取用户数据
	 * @param paramsMap {"uuid":"","loginName":"","phone":""}
	 * @return
	 */
	UserVO selectUserByUniqueKey(Map<String, Object> paramsMap);
	/**
	 * 通过数据库表的唯一键获取用户的数量
	 * @param paramsMap {"loginName":"","phone":""}
	 * @return
	 */
	public int selectUserCountByUniqueKey(Map<String, Object> paramsMap);
	
	/**
	 * 插入用户基本信息
	 * @param user
	 * @return
	 */
	int insertUser(UserVO user);

	/**
	 * 插入用户基本信息：不再数据库中生成UUID
	 * @param user
	 * @return
	 */
	public int insertUserWithoutUUID(UserVO user);
	
	/**
	 * 批量添加用户与角色的关系
	 * @param paramsMap 数据：{"userId":"", "roleIds":""}
	 * @return
	 */
	int insertUserRoleRel(Map<String, Object> paramsMap);
	
	/**
	 * 更新用户基本信息
	 * @param user
	 * @return
	 */
	int updateUser(UserVO user);
	
	/**
	 * 通过用户UUID或者行业归属UUID删除用户与行业归属的关系
	 * @param paramsMap {"userId":"", "industryId":""}
	 * @return
	 */
	int deleteUserIndustryRel(Map<String, Object> paramsMap);
	
	/**
	 * 通过用户UUID批量删除用户与行业归属的关联关系
	 * @param paramsMap {"uuids":""}
	 * @return
	 */
	int deleteUserIndustryRelBatch(Map<String, Object> paramsMap);
	
	/**
	 * 批量添加用户行业归属关系
	 * @param paramsMap {"userId":"", "industryIds":""}
	 * @return
	 */
	int insertUserIndustryRel(Map<String, Object> paramsMap);
	
	/**
	 * 通过用户UUID或角色UUID删除用于与角色的关联关系
	 * @param paramsMap {"userId":"", "roleId":""}
	 * @return
	 */
	int deleteUserRoleRel(Map<String, Object> paramsMap);
	
	/**
	 * 通过用户UUID批量删除用于与角色的关联关系
	 * @param paramsMap {"uuids":""}
	 * @return
	 */
	int deleteUserRoleRelBatch(Map<String, Object> paramsMap);
	
	/**
	 * 批量删除用户基本信息. 超级管理员"admin"无法删除
	 * @param paramsMap {"uuids":""}
	 * @return
	 */
	int deleteUserBatch(Map<String, Object> paramsMap);
	
	/**
	 * 根据条件查询用户信息，携带行业归属名、所属角色信息、可登录平台信息
	 * @param user 用户查询条件
	 * @return
	 */
	List<UserVO> selectUserByCondition(UserVO user);
	
	/**
	 * 根据条件查询用户信息，用于子平台同步用户信息
	 * @param user 查询条件
	 * @return
	 */
	List<UserVO> selectUserByConditionForSync(UserVO user);
	
	/**
	 * 通过用户名和手机号获取用户数据
	 * @param paramsMap
	 * @return
	 */
	UserVO selectByLoginNamePhone(Map<String, Object> paramsMap);
	
	/**
	 * 批量插入用户，如果用户存在则更新相应的字段
	 * @param userList 用户列表信息
	 * @return 操作结果
	 */
	public int insertOrUpdateUserBatch(List<UserVO> userList);
	
	/**
	 * 批量添加用户角色关系
	 * @param userRoleList 用户角色关系: {"userId":"","roleId":""}
	 * @return
	 */
	public int insertUserRoleRelBatch(List<Map<String, Object>> userRoleList);
	
	/**
	 * 通过唯一键或主键查询用户数量，使用OR
	 * @param user 条件
	 * @return
	 */
	public List<UserVO> selectUserByUniqueKeyWithOr(UserVO user);
	
	/**
	 * 更新用户信息：全字段
	 * @param user 用户信息
	 * @return
	 */
	public int updateUserAllFieldById(UserVO user);
	
	/**
	 * 通过用户UUID和角色UUID删除用户与角色的关联关系。
	 * @param paramsMap {"userId":"", "roleIds":list}
	 * @return
	 */
	public int deleteUserRoleBatchByUserIdAndRoleIdList(Map<String, Object> paramsMap);
	/**
	 * 通过用户UUID和角色UUID查询用户与角色的关联关系。
	 * @param paramsMap {"userId":"", "roleIds":list}
	 * @return
	 */
	public int selectCountByUserIdAndRoleId(Map<String, Object> paramsMap);
	
	/**
	 * 删除用户角色关联关系，通过用户ID和平台ID
	 * @param paramsMap {"userIdList":list,"roleIdList":""}
	 * @return
	 */
	public int deleteUserRoleBatchByUserIdAndRoleId(Map<String, Object> paramsMap);

	/**
	 * 通过一组用户手机号查询用户信息，提供给P-Server(掌上通)业务使用
	 * @param phoneArr 用户手机号数组
	 * @return
	 */
	public List<UserVO> selectUserByPhonesForPServer(String[] phoneArr);

	/**
	 * 通过手机号更新用户的登录密码
	 * @param user
	 * @return 更新成功的数量
	 */
	public int updateUserPasswordByPhone(UserVO user);

	/**
	 * 批量更新用户的基本信息: 用户密码、用户所在区域、用户所在区域名称、source、ext1
	 * @param userList 用户列表。用户的uuid必须存在
	 * @return 成功更新的数量
	 */
	int updateUserBatch(List<UserVO> userList);

	/**
	 * 批量插入用户的基本信息
	 * @param userList 用户列表
	 * @return 成功插入的数量
	 */
	int insertUserBatch(List<UserVO> userList);

	/**
	 * 通过用户ID查询用户的密码
	 * @param userId 用户主键ID
	 * @return 用户的密码
	 */
	String selectPwdById(String userId);
	
	/**
	 *  Description:获取所有旧数据用户
	 *  @author  ==zyf==
	 * @param map 
	 *  @date 2019年11月18日 下午2:41:23  
	 *  @return
	 */
	List<UserVO> selectOldUser(Map<String, Object> map);
	
	/**
	 *  Description:更新用户登录失败次数和超时时间
	 *  @author  ==zyf==
	 *  @date 2019年11月18日 下午3:48:31  
	 *  @param loginUser
	 */
	void updateUserLimitInfo(UserVO loginUser);
	
	/**
	 *  Description:批量更新用户信息
	 *  @author  ==zyf==
	 *  @date 2019年11月19日 上午10:40:54  
	 *  @param list2
	 */
	void updateBatchUser(List<UserVO> list2);
}
