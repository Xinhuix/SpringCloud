package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.LmtUserVo;
import com.visionvera.bean.datacore.ServerConfig;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.Map;

/**
 * 用户相关业务层接口
 *
 */
public interface UserService {
	/**
	 * 用户登录操作
	 * 
	 * @param user
	 *            用户信息
	 * @param loginType
	 *            登陆类型。1: 表示用户名密码登录；2: 表示手机号密码登录；3: 表示手机号验证码登录； 4: 表示用户名验证码登陆
	 * @param sessionTimeoutMinute
	 *            Session过期时间，分钟
	 * @return
	 */
	public ReturnData userLogin(UserVO user, Integer loginType, Integer sessionTimeoutMinute);

	/**
	 * 获取手机验证码
	 * @param phone 手机号
	 * @param timeoutMinutes 失效时间，单位为分钟
	 * @return
	 */
	public ReturnData sendVirificationCode(String phone, Integer timeoutMinutes);

	/**
	 * 发送手机号验证码
	 * 
	 * @param user
	 *            用户信息
	 * @param type
	 *            验证码类型：1 通过手机号验证码； 2 用户名获取验证码
	 * @return
	 */
	public ReturnData sendVirificationCode(UserVO user, Integer type);

	/**
	 * 获取手机验证码
	 * @param phone 手机号
	 * @param timeoutMinutes 失效时间，单位为分钟
	 * @param operationType 操作类型。1表示用户注册(不需要验证用户信息)，2表示用户重置密码(需要验证用户信息)
	 * @return
	 */
	ReturnData sendVirificationCode(String phone, Integer timeoutMinutes, Integer operationType);
	
	/**
	 * 添加用户
	 * @param user 用户信息
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData addUser(UserVO user, String token);
	
	/**
	 * 流媒体添加用户的业务处理
	 * @param user 用户信息
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData addUserForLmt(UserVO user, String token);
	
	/**
	 * 用户登出
	 * @param accessToken 访问令牌
	 * @param platformId 平台标识/ID
	 * @return
	 */
	public ReturnData userLogout(String accessToken, String platformId);
	
	/**
	 * 更新用户相关信息
	 * @param user
	 * @return
	 */
	public ReturnData updateUser(UserVO user, String token);
	/**
	 * 更新用户相关信息
	 * @param user
	 * @return
	 */
	public ReturnData updateUserPwd(UserVO user);
	
	/**
	 * 通过Token去缓存直接取用户信息
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData getUserByToken(String token);
	
	/**
	 * 批量删除用户信息
	 * @param uuids 用户主键UUID，多个主键使用英文逗号隔开
	 * @param token 访问令牌
	 * @param source 用户来源。运维工作站和会管用户无来源(传空)，其他系统同步均需要来源
	 * @return
	 */
	public ReturnData delUserBatch(String uuids, String token, String source);
	
	/**
	 * 获取用户列表信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param user 用户查询条件
	 * @return
	 */
	public PageInfo<UserVO> getUserList(boolean isPage, Integer pageNum, Integer pageSize, UserVO user);
	
	/**
	 * 获取用户信息列表，用于子平台同步用户
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param user 查询条件
	 * @return
	 */
	public PageInfo<UserVO> getUserListForSync(boolean isPage, Integer pageNum, Integer pageSize, UserVO user);
	
	/**
	 * 通过用户UUID获取用户信息，携带出对应的角色和权限信息
	 * @param uuid 用户UUID
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData getUserInfoById(String uuid, String token);
	
	/**
	 * 通过Token去缓存中获取登录名和平台ID
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData getPlatformIdAndLoginName(String token);
	/**
	 * 通过uuid、loginName、手机号获取用户
	 * @param paramsMap
	 * @return
	 */
	UserVO selectUserByUniqueKey(Map<String, Object> paramsMap);
	
	/**
	 * 同步流媒体用户
	 * @param user 用户信息
	 * @return
	 */
	public ReturnData addUserForLmt(UserVO user, LmtUserVo lmtUser,ServerConfig serverConfig);
	
	/**
	 * 同步会管用户信息
	 * @param token 访问令牌
	 * @param otherPlatformId 平台ID
	 * @param platformType 平台类别
	 * @return
	 */
	public ReturnData syncHuiguanUser(String token, String otherPlatformId, String platformType);
	
	/**
	 * 同步流媒体用户
	 * @param otherPlatformId 平台ID
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData syncUserForLmt(String otherPlatformId, String token);
	
	/**
	 * 同步流媒体用户信息.已废弃，无前端调用
	 * @param otherPlatformId 平台ID
	 * @return
	 */
	@Deprecated
	public ReturnData syncUserListForLmt( String otherPlatformId);
	
	/**
	 * 当会管系统添加用户的时候，同步添加到数据中心的用户表中
	 * @param user 用户信息
	 * @param isAdd 是否是添加用户操作.true表示添加用户，false表示修改用户
	 * @return
	 */
	public ReturnData addOrEditUserForHuiguan(UserVO user, boolean isAdd);

	/**
	 * 获取同步失败的用户的工作簿对象
	 * @param exportSystemType 导出哪个平台的系统类型。1：表示与会管平台同步；2：表示与流媒体平台同步；3：表示与网管平台同步
	 *                         4：表示与一机一档平台同步；5：表示与会易通平台同步
	 * @return
	 */
	public HSSFWorkbook getSyncUserFailWorkbook(String exportSystemType);

	/**
	 * 重置用户的密码
	 * @param user {"phone":"","verifiCode":"","loginPwd":""}
	 * 		phone: 手机号; verifiCode: 验证码; loginPwd: 密码，必须为MD5加密后的密码
	 * @return
	 */
	ReturnData resetPassword(UserVO user);

	/**
	 * 注册用户信息
	 * @param user 用户信息 {"loginName":"","name":"","loginPwd":"","verifiCode":"","areaId":"","roleIds":"","areaName":"","platformId":""}
	 * @return
	 */
	ReturnData userRegister(UserVO user);

	/**
	 * 通过用户的主键ID查询用户的密码
	 * @param userId 用户主键ID
	 * @return 用户的密码
	 */
	String getUserPwdByUserId(String userId);
	
	/**
	 *  Description:更新老用户密码加密方式
	 *  @author  ==zyf==
	 *  @date 2019年11月18日 下午2:21:26  
	 *  @return
	 */
	ReturnData updateHistoryUser();
	/**
	 * 根据用户名密码获取手机验证码
	 * @param user 用户信息
	 * @return
	 */
	public ReturnData sendCodeByLoginNameAndLoginPwd(UserVO user);
	/**
	 * 根据手机号获取验证码
	 * @param user 用户信息
	 * @return
	 */
	public ReturnData sendCodeByPhone(UserVO user);
}
