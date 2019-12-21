package com.visionvera.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.LmtUserVo;
import com.visionvera.bean.datacore.ServerConfig;
import com.visionvera.bean.datacore.SyncUserResultEntity;
import com.visionvera.bean.datacore.TApplicationdevicebVO;
import com.visionvera.bean.datacore.TPermissionVO;
import com.visionvera.bean.datacore.TRoleVO;
import com.visionvera.bean.restful.client.RestClient;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.LogTypeConstrant;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.authentication.ApplicationdevicebDao;
import com.visionvera.dao.authentication.IndustryDao;
import com.visionvera.dao.authentication.PermissionDao;
import com.visionvera.dao.authentication.RoleDao;
import com.visionvera.dao.authentication.UserDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.feign.OperationUserService;
import com.visionvera.mq.provider.RabbitProviderAsync;
import com.visionvera.service.ServerConfigService;
import com.visionvera.service.SyncLmtUserService;
import com.visionvera.service.UserService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.RandomUtil;
import com.visionvera.util.Sm3Utils;
import com.visionvera.util.SmsAgentUtil;
import com.visionvera.util.StringUtil;
import com.visionvera.util.TimeUtil;

/**
 * 用户相关业务层接口实现类
 *
 */
@Service
@Transactional(transactionManager = "transactionManager_authentication", rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public class UserServiceImpl implements UserService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserDao userDao;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private PermissionDao permissionDao;
	
	@Autowired
	private ApplicationdevicebDao applicationdevicebDao;
	
	@Autowired
	private IndustryDao industryDao;

	@Autowired
	private JRedisDao jedisDao;
	
	@Autowired
	private RabbitProviderAsync rabbitProvider;
	
	@Autowired
	private SyncLmtUserService syncLmtUserService;
	
	/*@Autowired
	private CmsUserService cmsUserService;*/
	
	@Autowired
	private ServerConfigService serverConfigService;
	
	@Autowired
	private OperationUserService operationUserService;
	
	/** 存储用户角色关系用户Key */
	private static final String USER_ID_STR = "userId";
	
	/** 存储用户角色关系角色Key */
	private static final String ROLE_ID_STR = "roleId";
	
	/** 删除用户角色关联关系用户ID集合的Key */
	private static final String USER_ID_LIST_STR = "userIdList";
	
	/** 删除用户角色关联关系角色ID集合的Key */
	private static final String ROLE_ID_LIST_STR = "roleIdList";

	/** 删除用户角色关联关系用户IDS集合的Key:roleIds */
//	private static final String ROLE_IDS_LIST_STR = "roleIds";

	/** 失败原因：手机号相同,用户名不同 */
	private static final String REASON_SAME_PHONE = "手机号相同,用户名不同";
	
	/** 失败原因：用户名相同,手机号不同 */
	private static final String REASON_SAME_LOGIN_NAME = "用户名相同,手机号不同";
	
	private static final String REASON_CMS_USER_NULL_PHONE = "手机号为空";
	
	/** 被同步平台失败用户，返回给前端的Key */
	private static final String OTHER_DATA_LIST_KEY = "otherDataList";
	
	/** 双方平台同步失败用户，返回给前端的Key */
	private static final String LOCAL_DATA_LIST_KEY = "localDataList";

	/** 注册用户的验证码类型：1 */
	private static final Integer SEND_CODE_OPERATION_TYPE_REGISTRY = 1;
	/** 充值用户密码的验证码类型：2 */
	private static final Integer SEND_CODE_OPERATION_TYPE_RESET_PASSWORD = 2;
	
	@Value("${login.limitNum:5}")
	private Integer limitNum;//登录限制次数
	@Value("${login.limitNum:3}")
	private Integer limitTime;//登录限制时长（分钟）
	@Value("${login.modityDay:30}")
	private Long modityDay;//密码修改时长（天）

	/**
	 * 用户登录操作
	 * 
	 * @param userParam 
	 *            用户信息
	 * @param loginType
	 *            登陆类型。1: 表示用户名密码登录；2: 表示手机号密码登录；3: 表示手机号验证码登录； 4: 表示用户名验证码登陆
	 * @param sessionTimeoutMinute
	 *            Session过期时间，分钟
	 * @return
	 */
	@Override
	public ReturnData userLogin(UserVO userParam, Integer loginType, Integer sessionTimeoutMinute) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> paramsMap = new HashMap<String,Object>();// 向DAO传递的参数Map
		Map<String, Object> extraData = new HashMap<String,Object>();// 结果Map
		Set<TPermissionVO> userPermissionSet = new LinkedHashSet<>();// 有序的Set集合

		if (loginType.equals(1) || loginType.equals(4)|| loginType.equals(5)) {// 使用用户名+密码或者用户名+验证码封装查询数据
			paramsMap.clear();
			paramsMap.put("loginName", userParam.getLoginName());
		}

		if (loginType.equals(2) || loginType.equals(3)) {// 使用手机号+密码或者手机号+验证码封装查询数据
			paramsMap.clear();
			paramsMap.put("phone", userParam.getPhone());
		}
		
		/** 校验用户信息 Start */
		UserVO user = this.userDao.selectUserByUniqueKey(paramsMap);// 查询用户信息
		if (user == null) {// 用户不存在
			this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGIN, "用户不存在", userParam.getLoginName(), false, userParam.getPlatformId(), "用户不存在", null);
			return returnData.returnError("用户不存在");
		}
		
		
		if (loginType.equals(1) || loginType.equals(2)) {// 用户名+密码或者手机号+密码登陆
			//校验密码加密方式，如果是md5加密，则根据加密规则sm3（login_name+md5（login_pwd））对密码加密后与数据库返回密码进行比对
			if(userParam.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
				String loginPwd = Sm3Utils.encrypt(user.getLoginName()+userParam.getLoginPwd());//最终的用户密码
				userParam.setLoginPwd(loginPwd);
			}
			if (!user.getLoginPwd().equalsIgnoreCase(userParam.getLoginPwd())) {// 密码不对
				this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGIN, "用户名和密码不匹配", user.getLoginName(), false, userParam.getPlatformId(), "用户名和密码不匹配", null);
				return returnData.returnError("用户名和密码不匹配");
			}
		}
		
		/** 校验用户信息 End */
		
		/** 校验平台信息 Start */
		TApplicationdevicebVO platform = this.applicationdevicebDao.selectApplicationdevicebById(userParam.getPlatformId());
		if (platform == null) {//平台不存在
			this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGIN, "平台不存在", user.getLoginName(), false, userParam.getPlatformId(), "平台不存在", null);
			return returnData.returnError("平台不存在");
		}
		/** 校验平台信息 End */
		
		/** 验证码登陆，校验验证码信息 Start */
		if (loginType.equals(3) || loginType.equals(4)) {// 用户名+验证码或手机号+验证码登陆 或用户名+密码+验证码
			String code = this.jedisDao.get(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()));
			if (StringUtil.isNull(code)) {//验证码过期
				this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGIN, "验证码已过期,请重新获取验证码", user.getLoginName(), false, userParam.getPlatformId(), "验证码已过期,请重新获取验证码", null);
				return returnData.returnError("验证码已过期,请重新获取验证码");
			}
			if (!userParam.getVerifiCode().equals(code)) {//验证码不正确
				this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGIN, "验证码不正确", user.getLoginName(), false, userParam.getPlatformId(), "验证码不正确", null);
				return returnData.returnError("验证码不正确");
			}
			this.jedisDao.del(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()));// 验证码验证成功后，删除验证码缓存
		}
		/** 验证码登陆，校验验证码信息 End */
		
		if(loginType.equals(5)){
			// 验证用户密码是否过期
			Date startModifyDate = user.getPwdModifyTime();
			if(startModifyDate==null){
				Date currentDate =new Date();
				startModifyDate = currentDate ;
				UserVO params = new UserVO();
				params.setUuid(user.getUuid());
				params.setPwdModifyTime(currentDate);
				this.userDao.updateUser(params);// 更新用户基本信息
			}
			Date endModifyDate = DateUtil.string2Date(DateUtil.addDateDays(startModifyDate, modityDay));
			if (!DateUtil.isEffectiveDate(new Date(), startModifyDate, endModifyDate)) {
				extraData.put("isPwdExpires", true);
				return returnData.returnResult(1, "账号密码已过期，请修改密码后重试", null, null, extraData);
			}
			ReturnData resultCheck =this.failCheckLoginHandle(user,userParam,2);
			if(resultCheck.getErrcode()!=0){
				this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGIN, resultCheck.getErrmsg(), user.getLoginName(), false, userParam.getPlatformId(), resultCheck.getErrmsg(), null);
				return resultCheck;
			}
				
		}
		paramsMap.clear();
		paramsMap.put("userId", user.getUuid());//用户主键UUID
		paramsMap.put("platformId", userParam.getPlatformId());//平台ID
		List<TRoleVO> roleList = this.roleDao.selectRoleForLogin(paramsMap);//根据用户主键ID和系统ID查询角色信息
		
		if (!"78b0de7ecb22414cbb44025e4402204f".equals(user.getPlatformId())) {//GIS调度平台不验证角色信息(初版)
			if (roleList == null || roleList.size() <= 0) {//用户没有分配角色
				this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGIN, "该用户没有被分配角色权限", user.getLoginName(), false, userParam.getPlatformId(), "请先给用户分配角色", null);
				return returnData.returnError("请先给用户分配权限");
			}
			
			for (TRoleVO role : roleList) {// 根据角色查询权限信息
				role.setPlatformId(userParam.getPlatformId());
				List<TPermissionVO> permissionList = this.permissionDao.selectPermissionForLogin(role);// 查询角色和平台对应的权限信息
				
				if (permissionList != null && permissionList.size() > 0) {
					for (TPermissionVO tPermissionVO : permissionList) {
						userPermissionSet.add(tPermissionVO);// 权限信息LinkedHashSet集合
					}
				}
			}
		}
		
		String accessToken = StringUtil.get32UUID();// 生成票据信息
		user.setRoleList(roleList);// 角色列表
		user.setPermissionSet(userPermissionSet);// 权限列表
		this.checkAndSaveRedis(user, sessionTimeoutMinute, accessToken, platform.getId());// 检查并存放Redis
		user.setAccessToken(null);
		user.setPlatformId(userParam.getPlatformId());
		this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGIN, "登录成功", user.getLoginName(), true, userParam.getPlatformId(), null, null);
		if(loginType.equals(5)){
			UserVO params = new UserVO();
			params.setUuid(user.getUuid());
			params.setFailNum(0);
			this.userDao.updateUser(params);//密码验证成功重置次数
			this.jedisDao.del(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()));// 验证码验证成功后，删除验证码缓存
		}
		return returnData.returnResult(0, "成功", accessToken, null, user);
	}

	/**
	 * 发送验证码
	 * 
	 * @param userParams
	 *            手机号
	 * @param type
	 *            验证码类型：1 通过手机号验证码； 2 用户名获取验证码
	 * @return
	 */
	@Override
	public ReturnData sendVirificationCode(UserVO userParams, Integer type) {
		BaseReturn returnData = new BaseReturn();// 返回的数据
		Map<String, Object> paramsMap = new HashMap<String, Object>();// 查询的参数Map
		
		if (type.equals(1)) {//封装使用手机号查询的数据
			paramsMap.clear();
			paramsMap.put("phone", userParams.getPhone());
		} else if (type.equals(2)) {//封装使用登录名查询的数据
			paramsMap.clear();
			paramsMap.put("loginName", userParams.getLoginName());
		}
		
		UserVO user = this.userDao.selectUserByUniqueKey(paramsMap);// 查询用户信息
		if (user == null) {
			return returnData.returnError("没有该用户");
		}
		
		if (!accessResend(user.getPhone())) {// 校验在一分钟内只能发送一次短信验证码
			this.LOGGER.error("UserServiceImpl ----- sendVirificationCode ---- 一分钟内只能发送一次短信");
			return returnData.returnError("一分钟内只能发送一次短信");
		}
		String verifiCode = RandomUtil.getRandomNum(4);//获取4位数字验证码

		int status = SmsAgentUtil.post(user.getPhone(), "【视联动力】" + "您的短信验证码为: " + verifiCode + " , 有效期为 1 分钟, 如非您本人操作, 请忽略!");// 发送到手机上
		if (status != 1) {// 发送失败
			this.LOGGER.error("UserServiceImpl ----- sendVirificationCode ---- 发送验证码失败, 失败状态码{}:" + status);
			return returnData.returnError("发送验证码失败");
		}
		this.jedisDao.set(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()), verifiCode,
				TimeUtil.getSecondsByMinute(CommonConstrant.VERIFICATION_CODE_TIMEOUT_MINUTES));// 将验证码存储Redis，失效时间为1分钟

		return returnData.returnResult(0, "发送成功");
	}

	/**
	 * 获取手机验证码
	 * @param phone 手机号
	 * @param timeoutMinutes 失效的时间，单位为分钟
	 * @return
	 */
	@Override
	public ReturnData sendVirificationCode(String phone, Integer timeoutMinutes) {
		BaseReturn returnData = new BaseReturn();// 返回的数据
		if (!accessResend(phone)) {// 校验在规定时间内只能发送一次短信验证码
			this.LOGGER.error("{} 分钟内只能发送一次短信, 用户的手机号为: {}", timeoutMinutes, phone);
			return returnData.returnError("[" + timeoutMinutes + "] 分钟内只能发送一次短信");
		}
		String verifiCode = RandomUtil.getRandomNum(4);//获取4位数字验证码

		int status = SmsAgentUtil.post(phone, "【视联动力】" + "您的短信验证码为: " + verifiCode + " , 有效期为 [" + timeoutMinutes + "] 分钟, 如非您本人操作, 请忽略!");// 发送到手机上
		if (status != 1) {// 发送失败
			this.LOGGER.error("发送验证码失败, , 失败状态码{}: ", status);
			return returnData.returnError("发送验证码失败");
		}
		this.jedisDao.set(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, phone), verifiCode,
				TimeUtil.getSecondsByMinute(timeoutMinutes));// 将验证码存储Redis，失效时间为1分钟

		return returnData.returnResult(WsConstants.OK, "发送成功");
	}

	/**
	 * 获取手机验证码
	 * @param phone 手机号
	 * @param timeoutMinutes 失效时间，单位为分钟
	 * @param operationType 操作类型。1表示用户注册(不需要验证用户信息)，2表示用户重置密码(需要验证用户信息)
	 * @return
	 */
	public ReturnData sendVirificationCode(String phone, Integer timeoutMinutes, Integer operationType) {
		BaseReturn returnData = new BaseReturn();// 返回的数据
		if (SEND_CODE_OPERATION_TYPE_REGISTRY.equals(operationType)) {//注册

		} else if (SEND_CODE_OPERATION_TYPE_RESET_PASSWORD.equals(operationType)) {//重置/忘记密码
			//验证用户的信息
			Map<String, Object> paramsMap = new HashMap<>();// 向DAO传递的参数Map
			paramsMap.put("phone", phone);
			int userCount = this.userDao.selectUserCountByUniqueKey(paramsMap);//查询用户的数量
			if (userCount == 0) {
				LOGGER.warn("重置用户的密码, 没有找到对应的用户, 用户输入的手机号为: {}, 操作类型为: ", phone, operationType);
				return returnData.returnError("没有该用户");
			}
		} else {
			LOGGER.warn("操作类型不正确, 操作类型为: {}", operationType);
			return returnData.returnError("请输入正确的操作类型");
		}
		return this.sendVirificationCode(phone, timeoutMinutes);
	}

	/**
	 * 添加用户
	 * @param user 用户信息
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData addUser(UserVO user, String token) {
		BaseReturn dataReturn = new BaseReturn();
		
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		
		int userCount = 0;//用户数量
		
		//检查用户名是否重复
		paramsMap.put("loginName", user.getLoginName());
		userCount = this.userDao.selectUserCountByUniqueKey(paramsMap);//查询用户的数量
		if (userCount > 0) {
			return dataReturn.returnError("用户名已存在");
		}
		
		//检查手机号是否重复
		paramsMap.clear();
		paramsMap.put("phone", user.getPhone());
		userCount = this.userDao.selectUserCountByUniqueKey(paramsMap);
		if (userCount > 0) {
			return dataReturn.returnError("[" + user.getPhone() + "]手机号已存在");
		}
		
		//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
			String md5pwd = user.getLoginPwd();
			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
			user.setLoginPwd(loginPwd);
			user.setMd5Pwd(md5pwd);
		}
		this.userDao.insertUser(user);//执行插入数据库
		/** 添加用户与行业归属的关系 Start */
		if (StringUtil.isNotNull(user.getIndustryId())) {
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			paramsMap.put("industryIds", user.getIndustryId().split(","));
			this.industryDao.insertUserIndustry(paramsMap);
		}
		/** 添加用户与行业归属的关系 End */
		
		/** 添加用户与角色的关系 Start */
		if (StringUtil.isNotNull(user.getRoleIds())) {
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			paramsMap.put("roleIds", user.getRoleIds().split(","));
			this.userDao.insertUserRoleRel(paramsMap);
		}
		/** 添加用户与角色的关系 End */
		
		//根据角色查询权限
		paramsMap.clear();
		paramsMap.put("roleIds", user.getRoleIds().split(","));
		String permission = this.permissionDao.selectPermissionByRole(paramsMap);
		user.setPermission(permission);
		
		//发送操作日志消息: 异步
		this.rabbitProvider.sendLogMessage(LogTypeConstrant.ADD_USER, "添加用户成功", true, null, token);
		
		/**本地用户注册，调用流媒体注册用户接口:异步*/
		if (StringUtil.isNull(user.getSource())) {//只有从运维工作站(source为空)添加的用户实时同步到会管。如果不为空则走流媒体的用户添加方法this.addUserForLmt()
			//向队列发送添加用户的消息: 异步
//			rabbitProvider.sendHuiguanUserMessage(user, CommonConstrant.RABBIT_USER_ADD_OPERATION_TYPE);
//			
//			//如果是视联汇app则添加用户与设备关联关系
//			this.addUserGroupDevRel(user, token);
//			//如果是视联汇app则添加用户最大参会数
//			if(user.getMaxDevNum() != null){
//				this.updateMaxDevNumByLoginName(user, token);
//			}
//			syncLmtUserService.lmtUserAdd(user);
			new Thread(new Runnable() {
				public void run() {
					//向队列发送添加用户的消息: 异步
					//给会管返回的数据要将loginPwd放入md5加密的密码
					user.setSm3Pwd(user.getLoginPwd());
					user.setLoginPwd(user.getMd5Pwd());
					rabbitProvider.sendHuiguanUserMessage(user, CommonConstrant.RABBIT_USER_ADD_OPERATION_TYPE);
					
					addUserGroupDevRel(user, token);
					//如果是视联汇app则添加用户最大参会数
					if(user.getMaxDevNum() != null){
						updateMaxDevNumByLoginName(user, token);
					}
					syncLmtUserService.lmtUserAdd(user);
				}
			}).start();
		}
		
		return dataReturn.returnResult(0, "添加成功", null, null, user);
	}
	
	@SuppressWarnings("static-access")
	private int addUserGroupDevRel(UserVO user,String token){
		//根据用户名获取会管用户uuid
	    try {
	    	UserVO userVo =null;
			for(int i=0;i<3;i++){
				userVo = operationUserService.getHgUserInfo(user.getLoginName(), token);
				if(userVo!=null){
					break;
				}
				if(userVo==null&&i==2){//等待2次查询失败后抛出异常使事务回滚
					throw new RuntimeException();
				}
				Thread.currentThread().sleep(1000);
			}
			String uuid =userVo.getUuid();
			user.setUuid(uuid);
			return operationUserService.addUserGroupDev(user, token);
	    
	    } catch (InterruptedException e) {
			this.LOGGER.error("-----addUserGroupDevRel"+e);
		}
	    return 0;
   }
	
	/**
	 * 设置视联汇用户最大允许参会数
	 * @param user
	 * @param token
	 * @return
	 */
	@SuppressWarnings("static-access")
	private int updateMaxDevNumByLoginName(UserVO user,String token){
		//根据用户名获取会管用户uuid
		try {
//			if(user != null && user.getUuid() == null){
				UserVO userVo =null;
				for(int i=0;i<3;i++){
					userVo = operationUserService.getHgUserInfo(user.getLoginName(), token);
					if(userVo!=null){
						break;
					}
					if(userVo==null&&i==2){//等待2次查询失败后抛出异常使事务回滚
						throw new RuntimeException();
					}
					Thread.currentThread().sleep(1000);
				}
				String uuid =userVo.getUuid();
				user.setUuid(uuid);
//			}
			return operationUserService.updateMaxDevNumByLoginName(user, token);
			
		} catch (InterruptedException e) {
			this.LOGGER.error("-----addUserGroupDevRel"+e);
		}
		return 0;
	}
	
	private int updateUserGroupDevRel(UserVO user,String token){
		//根据用户名获取会管用户uuid
		UserVO userVo = operationUserService.getHgUserInfo(user.getLoginName(), token);
		if(userVo!=null){
			String uuid =userVo.getUuid();
			user.setUuid(uuid);
			return  operationUserService.updateUserGroupDev(user, token);
		}
		return 0;
   }
	
	/**
	 * 用户登出
	 * @param accessToken 访问令牌
	 * @param platformId 平台标识/ID
	 * @return
	 */
	@Override
	public ReturnData userLogout(String accessToken, String platformId) {
		BaseReturn dataReturn = new BaseReturn();
		if("33333333333333333333333333333333".equals(accessToken)){//特殊token，不删除，直接返回成功
			return dataReturn.returnResult(0, "登出成功");
		}
		String tokenKey = this.getRedisKey(CommonConstrant.PREFIX_TOKEN, accessToken);//Token完整Key
		
		String loginName = this.jedisDao.get(tokenKey);//获取用户名
		if (StringUtil.isNotNull(loginName)) {
			this.LOGGER.info("删除Redis缓存的Token信息 =====> " + tokenKey);
			this.jedisDao.del(tokenKey);//删除token
			this.jedisDao.del(this.getRedisKey(CommonConstrant.PREFIX_PLATFORM_ID, accessToken));//删除平台信息
			
			//获取平台信息
			TApplicationdevicebVO platform = this.applicationdevicebDao.selectApplicationdevicebById(platformId);
			if (platform != null) {
				//用户信息完整Key
				String userInfoKey = this.getRedisKey(CommonConstrant.PREFIX_LOGIN_NAME, loginName) + "_" + platformId;
				this.LOGGER.info("删除Redis缓存的用户角色权限信息 =====> " + userInfoKey);
				this.jedisDao.del(userInfoKey);
			}
		}
		this.rabbitProvider.sendLogMessage(LogTypeConstrant.LOGOUT, "登出成功", loginName, true, platformId, null, null);
		return dataReturn.returnResult(0, "登出成功");
	}
	
	/**
	 * 更新用户相关信息
	 * @param user
	 * @return
	 */
	@Override
	public ReturnData updateUser(UserVO user, String token) {
		BaseReturn dataReturn = new BaseReturn();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("loginName", user.getLoginName());
		UserVO entity = userDao.selectUserByUniqueKey(params);
        if(entity != null){
        	user.setUuid(entity.getUuid());
        }
        //如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
        if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
        	String md5pwd = user.getLoginPwd();
        	String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
        	user.setLoginPwd(loginPwd);
        	user.setMd5Pwd(md5pwd);
        }
		this.userDao.updateUser(user);//更新用户基本信息
		
		/** 更新用户与行业归属的关系 Start */
		if (StringUtil.isNull(user.getSource())) {//只有运维工作站用户修改(source为空)才会修改行业归属信息
			//根据用户UUID删除用户对应的行业归属关系
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			this.userDao.deleteUserIndustryRel(paramsMap);
			
			//添加用户与行业归属的关系
			if (StringUtil.isNotNull(user.getIndustryId())) {
				paramsMap.clear();
				paramsMap.put("userId", user.getUuid());
				paramsMap.put("industryIds", user.getIndustryId().split(","));
				this.userDao.insertUserIndustryRel(paramsMap);//添加用于与行业归属的关系
			}
		}
		/** 更新用户与行业归属的关系End */
		
		/** 更新用于与角色的关系 Start */
		//根据用户UUID删除用户对应的角色关系
		paramsMap.clear();
		paramsMap.put("platformId", user.getSource());
		TRoleVO lmtDefaultRole = roleDao.selectRoleIdByplatformId(paramsMap);//根据平台ID查询默认角色
		
		if (StringUtil.isNotNull(user.getSource())) {//流媒体走的删除
			List<String> userIdList = new ArrayList<String>();//用户Id集合
			List<String> roleIdList = new ArrayList<String>();//角色Id集合
			userIdList.add(user.getUuid());
			roleIdList.add(user.getRoleIds());
			if (lmtDefaultRole != null) {//如果有默认角色就删除
				userIdList.add(user.getUuid());
				roleIdList.add(lmtDefaultRole.getUuid());
			}
			paramsMap.clear();
			paramsMap.put(USER_ID_LIST_STR, userIdList);
			paramsMap.put(ROLE_ID_LIST_STR, roleIdList);
			this.userDao.deleteUserRoleBatchByUserIdAndRoleId(paramsMap);
		} else {//运维工作站走的删除
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			this.userDao.deleteUserRoleRel(paramsMap);//删除用户与角色的关联关系
		}
		//添加用户与角色的关系
		if (StringUtil.isNotNull(user.getRoleIds())) {
			String roleIds = user.getRoleIds();//用户角色
			if (StringUtil.isNotNull(user.getSource())) {//来自除了本平台和会管之外的平台(流媒体),添加用户的默认角色
				if (lmtDefaultRole != null) {
					roleIds = roleIds + "," + lmtDefaultRole.getUuid();
				} else {
					LOGGER.warn("该平台: {} 没有默认角色", user.getSource());
				}
			}
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			paramsMap.put("roleIds", roleIds.split(","));
			
			this.userDao.insertUserRoleRel(paramsMap);//添加
		}
		/** 更新用于与角色的关系 End */
		
		//根据角色查询权限
		paramsMap.clear();
		paramsMap.put("roleIds", user.getRoleIds().split(","));
		String permission = this.permissionDao.selectPermissionByRole(paramsMap);
		user.setPermission(permission);
		
		//发送操作日志消息: 异步
		this.rabbitProvider.sendLogMessage(LogTypeConstrant.EDIT_USER, "修改用户成功", true, null, token);
		
		/** 异步调用流媒体修改用户接口：异步*/
		if(StringUtil.isNull(user.getSource())){//只有是运维工作站本身修改的用户(source为空)则向会管推送用户
			//向队列发送编辑用户的消息: 异步
			// 将旧密码回传给会管，以保证sm3加密成功
//	        if(StringUtils.isBlank(user.getLoginPwd())){
//	        	user.setLoginPwd(entity.getLoginPwd());
//	        }
//			this.rabbitProvider.sendHuiguanUserMessage(user, CommonConstrant.RABBIT_USER_EDIT_OPERATION_TYPE);
//			//如果是视联汇app则修改用户与设备关联关系
//			this.updateUserGroupDevRel(user, token);
//			//如果是视联汇app则添加用户最大参会数
//			if(user.getMaxDevNum() != null){
//				this.updateMaxDevNumByLoginName(user, token);
//			}
//			syncLmtUserService.lmtUpdateUser(user);
			new Thread(new Runnable() {
				public void run() {
					//向队列发送编辑用户的消息: 异步
					// 将旧密码回传给会管，以保证sm3加密成功
			        if(StringUtils.isBlank(user.getLoginPwd())){
			        	user.setLoginPwd(entity.getLoginPwd());
			        }
			        user.setSm3Pwd(user.getLoginPwd());
			        user.setLoginPwd(user.getMd5Pwd());
					rabbitProvider.sendHuiguanUserMessage(user, CommonConstrant.RABBIT_USER_EDIT_OPERATION_TYPE);
					//如果是视联汇app则修改用户与设备关联关系
					updateUserGroupDevRel(user, token);
					//如果是视联汇app则添加用户最大参会数
					if(user.getMaxDevNum() != null){
						updateMaxDevNumByLoginName(user, token);
					}
					syncLmtUserService.lmtUpdateUser(user);
				}
			}).start();
		}
		return dataReturn.returnResult(0, "更新成功", null, null, user);
	}
	
	/**
	 * 通过Token去缓存直接取用户信息
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData getUserByToken(String token) {
		BaseReturn dataReturn = new BaseReturn();
		String tokenKey = this.getRedisKey(CommonConstrant.PREFIX_TOKEN, token);//Token完整Key
		String loginName = this.jedisDao.get(tokenKey);//通过Token获取用户名信息

		if (StringUtil.isNull(loginName)) {
			this.LOGGER.info("Token为空");
			return dataReturn.returnError("Token过期");
		}

		String platformId = this.jedisDao.get(this.getRedisKey(CommonConstrant.PREFIX_PLATFORM_ID, token));//平台ID

		if (StringUtil.isNull(platformId)) {
			this.LOGGER.info("平台ID为空");
			return dataReturn.returnError("Token过期");
		}

		// 用户名+平台类型+平台ID组成的key
		String loginNameKey = this.getRedisKey(CommonConstrant.PREFIX_LOGIN_NAME, loginName) + "_" + platformId;
		UserVO user = (UserVO)this.jedisDao.getObject(loginNameKey);//用户缓存信息
		return dataReturn.returnResult(0, "获取成功", null, null, user);
	}
	
	/**
	 * 批量删除用户信息
	 * @param uuids 用户主键UUID，多个主键使用英文逗号隔开
	 * @param token 访问令牌
	 * @param source 用户来源。运维工作站和会管用户无来源(传空)，其他系统同步均需要来源.CommonConstrant.RABBIT_USER_DEL_OPERATION_TYPE表示会管发过来的删除请求
	 * @return
	 */
	public ReturnData delUserBatch(String uuids, String token, String source) {
		BaseReturn dataReturn = new BaseReturn();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		List<UserVO> list = new ArrayList<UserVO>();
		List<String> userIdListForHuiguan = new ArrayList<String>();//存储会管用户ID的集合
		//查询所有要删除的用户
		String uuid[] = uuids.split(",");
		for(String uid:uuid){
			paramsMap.clear();
			paramsMap.put("uuid", uid);
			/** 校验用户信息 Start */
			UserVO user = userDao.selectUserByUniqueKey(paramsMap);// 查询用户信息
			if(user!=null){
				list.add(user);
				userIdListForHuiguan.add(user.getExt1());//ext1表示本地保存的会管的用户ID
			}
		}
		UserVO userParam = new UserVO();//传递消息用户对象
		userParam.setUuid(uuids);//封装UUID
		paramsMap.put("uuids", uuids.split(","));
		
		if (StringUtil.isNull(source)) {//正常运维工作站走的删除(其他请求均有source值)
			String userIdForHuiguan = "";
			//计算删除会管的ID Start
			if (userIdListForHuiguan != null && userIdListForHuiguan.size() > 0) {
				for (String string : userIdListForHuiguan) {
					userIdForHuiguan = string + "," + userIdForHuiguan;
				}
				userParam.setUuid(userIdForHuiguan);//封装UUID
			}
			//计算删除会管的ID End
			
			this.userDao.deleteUserIndustryRelBatch(paramsMap);//删除用户对应的行业归属信息
			this.userDao.deleteUserRoleRelBatch(paramsMap);//删除用户对应的角色信息
			this.userDao.deleteUserBatch(paramsMap);//删除用户的基本信息
			/** 异步调用流媒体删除用户接口：异步*/
			syncLmtUserService.lmtDeleteUser(list);
			//向队列发送删除用户的消息: 异步
			this.rabbitProvider.sendHuiguanUserMessage(userParam, CommonConstrant.RABBIT_USER_DEL_OPERATION_TYPE);
		} else if (source.equals(CommonConstrant.RABBIT_USER_DEL_OPERATION_TYPE + "")) {//会管走的删除，只会删除这个用户对应会管的用户角色关系
			paramsMap.clear();
			List<String> userIdList = new ArrayList<String>();
			List<String> roleIdList = new ArrayList<String>();
			for (String uId : uuids.split(",")) {
				UserVO userParams = new UserVO();
				userParams.setExt1(uId);//会管的UUID
				List<UserVO> userList = this.userDao.selectUserByCondition(userParams);//通过会管的主键ID查询该数据
				if (userList != null && userList.size() > 0) {
					for (UserVO localUser : userList) {
						userIdList.add(localUser.getUuid());
						roleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ADMIN_ROLE_ID);//会管超级管理员角色
						userIdList.add(localUser.getUuid());
						roleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ORDINARY_ROLE_ID);//会管普通角色
					}
				}
			}
			paramsMap.put(USER_ID_LIST_STR, userIdList);
			paramsMap.put(ROLE_ID_LIST_STR, roleIdList);
			this.userDao.deleteUserRoleBatchByUserIdAndRoleId(paramsMap);
		} else {//其他平台,只删除该平台下的所有角色.其他平台不支持批量删除
			paramsMap.clear();
			List<String> userIdList = new ArrayList<String>();
			List<String> roleIdList = new ArrayList<String>();
			TRoleVO roleParam = new TRoleVO();//查询条件
			roleParam.setPlatformId(source);//通过平台ID查询
			List<TRoleVO> roleList = this.roleDao.selectRoleAndPlatformByConfition(roleParam);//通过平台ID查询该平台下的所有角色信息
			if (roleList != null && roleList.size() > 0) {
				for (TRoleVO role : roleList) {
					userIdList.add(uuids);
					roleIdList.add(role.getUuid());
					userIdList.add(uuids);
					roleIdList.add(GlobalConstants.MEDIA_SERVER_RESERVED_ROLE_ID);//流媒体专用角色
				}
				paramsMap.put(USER_ID_LIST_STR, userIdList);
				paramsMap.put(ROLE_ID_LIST_STR, roleIdList);
				this.userDao.deleteUserRoleBatchByUserIdAndRoleId(paramsMap);
			}
			
			//流媒体用户删除对应的固定角色
			
		}
		
		//发送操作日志消息: 异步
		this.rabbitProvider.sendLogMessage(LogTypeConstrant.DEL_USER, "删除用户成功, 删除的用户是: " + JSONObject.toJSONString(userParam), true, null, token);

		//将用户信息返回(暂不支持批量删除),在AOP中使用
		return dataReturn.returnResult(0, "删除成功", null, null, ((list != null && list.size() > 0) ? list.get(0) : new UserVO()));
	}
	
	/**
	 * 获取用户列表信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param user 用户查询条件
	 * @return
	 */
	public PageInfo<UserVO> getUserList(boolean isPage, Integer pageNum, Integer pageSize, UserVO user) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<UserVO> userList = this.userDao.selectUserByCondition(user);//查询用户列表信息，携带行业归属+所属区域+可登陆平台信息+角色信息
		PageInfo<UserVO> userInfo = new PageInfo<UserVO>(userList);//分装分页信息
		return userInfo;
	}
	
	/**
	 * 获取用户信息列表，用于子平台同步用户
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param user 查询条件
	 * @return
	 */
	public PageInfo<UserVO> getUserListForSync(boolean isPage, Integer pageNum, Integer pageSize, UserVO user) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<UserVO> userList = this.userDao.selectUserByConditionForSync(user);//查询用户列表信息
		PageInfo<UserVO> userInfo = new PageInfo<UserVO>(userList);//分装分页信息
		return userInfo;
	}
	
	
	/**
	 * 通过用户UUID获取用户信息，携带出对应的角色和权限信息
	 * @param uuid 用户UUID
	 * @param token 访问令牌
	 * @return
	 */
	@Override
	public ReturnData getUserInfoById(String uuid, String token) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> paramsMap = new HashMap<>();// 向DAO传递的参数Map
		Set<TPermissionVO> userPermissionSet = new LinkedHashSet<>();// 有序的Set集合
		String platformId = "";//平台ID

		paramsMap.put("uuid", uuid);
		/** 校验用户信息 Start */
		UserVO user = this.userDao.selectUserByUniqueKey(paramsMap);// 查询用户信息
		if (user == null) {// 用户不存在
			return returnData.returnError("用户不存在");
		}
		/** 校验用户信息 End */
		
		/** 根据Token去缓存中获取平台ID Start */
		platformId = this.jedisDao.get(this.getRedisKey(CommonConstrant.PREFIX_PLATFORM_ID, token));
		/** 根据Token去缓存中获取平台ID End */
		
		paramsMap.clear();
		paramsMap.put("userId", user.getUuid());//用户主键UUID
		paramsMap.put("platformId", platformId);//平台ID
		List<TRoleVO> roleList = this.roleDao.selectRoleForLogin(paramsMap);//根据用户主键UUID角色信息
		
		if (roleList == null || roleList.size() <= 0) {//用户没有分配角色
			return returnData.returnError("权限为空");
		}
		for (TRoleVO role : roleList) {// 根据角色查询权限信息
			List<TPermissionVO> permissionList = this.permissionDao.selectPermissionByRoleId(role.getUuid());// 查询角色对应的权限信息
			if (permissionList != null && permissionList.size() > 0) {
				for (TPermissionVO tPermissionVO : permissionList) {
					userPermissionSet.add(tPermissionVO);// 权限信息LinkedHashSet集合
				}
			}
		}
		
		user.setRoleList(roleList);
		user.setPermissionSet(userPermissionSet);
		return returnData.returnResult(0, "成功", null, null, user);
	}
	
	/**
	 * 通过Token去缓存中获取登录名和平台ID
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData getPlatformIdAndLoginName(String token) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> extraMap = new HashMap<String, Object>();
		String platformId = this.jedisDao.get(this.getRedisKey(CommonConstrant.PREFIX_PLATFORM_ID, token));//平台ID
		String loginName = this.jedisDao.get(this.getRedisKey(CommonConstrant.PREFIX_TOKEN, token));
		
		extraMap.put("platformId", platformId);
		extraMap.put("loginName", loginName);
		
		return returnData.returnResult(0, "获取成功", null, null, extraMap);
	}
	
	/**
	 * 同步会管用户信息
	 * @param token 访问令牌
	 * @param otherPlatformId 平台ID
	 * @param platformType 平台类别
	 * @return
	 */
	@Override
	public ReturnData syncHuiguanUser(String token, String otherPlatformId, String platformType) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> extraDataMap = new HashMap<String, Object>();//同步结果数量Map
		List<Map<String, Object>> userRoleRelList = new ArrayList<Map<String, Object>>();//数据中心数据看用户角色关系List
		List<SyncUserResultEntity> otherDataList = new ArrayList<SyncUserResultEntity>();//被同步平台失败用户
		List<SyncUserResultEntity> localDataList = new ArrayList<SyncUserResultEntity>();//双方平台同步失败用户
		Map<String, Object> urlParamsMap = new HashMap<String, Object>();
		
		String ipPortUrl = this.serverConfigService.getIpPortUrl(otherPlatformId);//获取IP、Port的URL，类似：http://ip:port
		
		/** 调用会管查询相同号码的用户列表 Start */
		//查询相同号码的用户信息的URL
		urlParamsMap.put("microServerFlag", 1);//会管免登陆标志
		String url = ipPortUrl + WsConstants.URL_GET_SAME_PHONE_USER_HUIGUAN;//获取会管相同号码用户信息的URL
		
		
		JSONObject samePhoneUserJsonObject = RestTemplateUtil.getForObject(url, JSONObject.class, urlParamsMap);
		List<UserVO> samePhoneUserList = samePhoneUserJsonObject.getJSONObject("data").getJSONArray("items").toJavaList(UserVO.class);
		/** 调用会管查询相同号码的用户列表 End */
		
		url = ipPortUrl + WsConstants.URL_GET_ALL_USER_HUIGUAN;//获取会管所有用户的URL
		urlParamsMap.put("pageSize", -1);//pageSize表示查询会管所有用户，不分页
		JSONObject allUserJsonObject = RestTemplateUtil.getForObject(url, JSONObject.class, urlParamsMap);//获取会管所有用户信息
		List<UserVO> cmsUserList = allUserJsonObject.getJSONObject("data").getJSONArray("items").toJavaList(UserVO.class);
		
		int totalCmsUserCount = cmsUserList.size();//会管用户的总数量
		
		if (cmsUserList == null || cmsUserList.size() < 0) {
			return returnData.returnError("会管无用户");
		}
		
		/** 被同步平台(会管),用户的手机号相同处理逻辑 Start */
		if (samePhoneUserList != null && samePhoneUserList.size() > 0) {//会管平台手机号相同的用户
			for (UserVO samePhoneUser : samePhoneUserList) {
				SyncUserResultEntity syncUserResult = 
						this.generateSyncUserResult(null, samePhoneUser.getLoginName(), samePhoneUser.getPhone(), samePhoneUser.getCreateTime(), REASON_SAME_PHONE);
				otherDataList.add(syncUserResult);
			}
			cmsUserList.removeAll(samePhoneUserList);//去除号码重复的用户
		}
		/** 被同步平台(会管),用户的手机号相同处理逻辑 End */
		
		/** 双方平台同步(会管和本平台)失败的处理逻辑 Start */
		for (int i = 0; i < cmsUserList.size(); i++) {
			UserVO cmsUser = cmsUserList.get(i);
			
			if (StringUtils.isBlank(cmsUser.getPhone())) {//会管手机号为空
				cmsUserList.remove(cmsUser);
				otherDataList.add(this.generateSyncUserResult(null, cmsUser.getLoginName(), cmsUser.getPhone(), cmsUser.getCreateTime(), REASON_CMS_USER_NULL_PHONE));
				i--;
				continue;
			}
			
			//通过用户登录名或者电话号码查询用户信息
			List<UserVO> userList = this.userDao.selectUserByUniqueKeyWithOr(cmsUser);
			
			if(userList != null && userList.size() > 0) {
				for (UserVO user : userList) {
					//会管用户与数据中心用户登录名一样，但是手机号不一样
					if (StringUtil.isNotNull(cmsUser.getPhone()) && StringUtil.isNotNull(user.getPhone())) {
						if ((cmsUser.getLoginName().equals(user.getLoginName())) && !(cmsUser.getPhone().equals(user.getPhone()))) {
							cmsUserList.remove(cmsUser);
							localDataList.add(this.generateSyncUserResult(user, cmsUser.getLoginName(), cmsUser.getPhone(), cmsUser.getCreateTime(), REASON_SAME_LOGIN_NAME));
							i--;
							continue;
						}
						
						//会管用户与数据中心用户登录名不一样，但是手机号一样
						if (!(cmsUser.getLoginName().equals(user.getLoginName())) && (cmsUser.getPhone().equals(user.getPhone()))) {
							cmsUserList.remove(cmsUser);
							localDataList.add(this.generateSyncUserResult(user, cmsUser.getLoginName(), cmsUser.getPhone(), cmsUser.getCreateTime(), REASON_SAME_PHONE));
							i--;
						}
					}
				}
			}
		}
		
		//将用户的手机号相同List存放数据库，用于导出Excel
		this.jedisDao.setList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_HUIGUAN_OTHER), 
				otherDataList, TimeUtil.getSecondsByMinute(null));
		//将用户的手机号相同用户名不同或者用户名相同手机号不同的结果List存放数据库，用于导出Excel
		this.jedisDao.setList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_HUIGUAN_LOCAL), 
				localDataList, TimeUtil.getSecondsByMinute(null));
		/** 双方平台同步(会管和本平台)失败的处理逻辑 End */
		
		for (UserVO cmsUser : cmsUserList) {
			//会管用户名和手机号与运维工作站用户名和手机号相同，但是UUID不同的处理逻辑
			this.setLocalUserUUIDAndCreateTime(cmsUser);
			
			cmsUser.setRoleIds(cmsUser.getRole());//由于会管将角色ID设置成了role字段，对应智能运维的roleIds字段
			
			//解析会管用户在会管页面的角色权限,赋予其在智能运维统一平台的角色权限
			userRoleRelList = this.parseWebPermissionInCms(userRoleRelList, cmsUser.getRoleIds(), cmsUser.getUuid(), cmsUser.getWebLogin());
			
			//解析会管用户对应的视联汇和视联管家的是否是VIP用户的权限
			userRoleRelList = this.parseVipPermissionInCms(userRoleRelList, cmsUser.getAllowHkzs(), cmsUser.getUuid());
			
			//解析会管用户对应的视联汇和视联管家的其他权限 Start
			//解析视联汇的权限
			userRoleRelList = this.parseMeAppPermissionInCms(userRoleRelList, cmsUser.getPermission(), cmsUser.getUuid());
			
			
			//视联管家权限 
			userRoleRelList = this.parsePamirMobilePermissionInCms(userRoleRelList, cmsUser.getUuid(), cmsUser.getAllowHkzs());
			//解析会管用户对应的视联汇和视联管家的其他权限 End
			
			//处理用户sm3加密方式密码兼容
			//判断用户sm3Pwd字段是否为空
			if(StringUtil.isNotNull(cmsUser.getSm3Pwd())){//不为空
				//将sm3的密码赋值给loginPwd
				cmsUser.setLoginPwd(cmsUser.getSm3Pwd());
			}else{
				//杂凑组成sm3之后将密码赋值给loginPwd
				String loginPwd = Sm3Utils.encrypt(cmsUser.getLoginName()+cmsUser.getLoginPwd());//最终的用户密码
				cmsUser.setLoginPwd(loginPwd);
			}
		}
		
		//批量添加/更新用户信息
		if(cmsUserList.size() > 0){
			this.userDao.insertOrUpdateUserBatch(cmsUserList);
		}
		
		//封装操作数量结果
		extraDataMap.put(OTHER_DATA_LIST_KEY, otherDataList);//被同步平台失败用户的List
		extraDataMap.put(LOCAL_DATA_LIST_KEY, localDataList);//双方平台同步失败用户
		extraDataMap.put("userSyncCount", totalCmsUserCount);//用户同步的总数量
		extraDataMap.put("successUserSyncCount", cmsUserList.size());//同步成功数量
		extraDataMap.put("errorUserSyncCount", totalCmsUserCount - cmsUserList.size());//同步失败数量
		extraDataMap.put(GlobalConstants.EXPORT_TYPE_KEY, GlobalConstants.EXPORT_TYPE_HUIGUAN_SYSTEM);//导出会管平台的系统类型
		
		//批量添加用户角色关系
		if (userRoleRelList != null && userRoleRelList.size() > 0) {
			List<String> userIdList = new ArrayList<String>();
			List<String> roleIdList = this.getFixedRoleIdForHuiguan();//获取会管、视联汇、视联管家所有角色ID的集合
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			for (Map<String, Object> userRoleMap : userRoleRelList) {
				userIdList.add(userRoleMap.get(USER_ID_STR) + "");
			}
			
			paramsMap.put(USER_ID_LIST_STR, userIdList);
			paramsMap.put(ROLE_ID_LIST_STR, roleIdList);
			
			this.userDao.deleteUserRoleBatchByUserIdAndRoleId(paramsMap);
			this.userDao.insertUserRoleRelBatch(userRoleRelList);
		}
		
		//发送操作日志消息: 异步
		this.rabbitProvider.sendLogMessage(LogTypeConstrant.SYNC_HUIGUAN_USER, "同步会管用户成功", true, null, token);
		return returnData.returnResult(0, "同步成功", null, null, extraDataMap);
	}
	
	/**
	 * 当会管系统添加用户的时候，同步添加到数据中心的用户表中
	 * @param user 用户信息
	 * @param isAdd 是否是添加用户操作.true表示添加用户，false表示修改用户
	 * @return
	 */
	@Override
	public ReturnData addOrEditUserForHuiguan(UserVO user, boolean isAdd) {
		BaseReturn returnData = new BaseReturn();
		List<Map<String, Object>> userRoleRelList = new ArrayList<Map<String, Object>>();//数据中心数据看用户角色关系List
		
		List<UserVO> userList = new ArrayList<UserVO>();//插入用户数据
		int userCount = 0;//操作数据库的结果
		//会管用户名和手机号与运维工作站用户名和手机号相同，但是UUID不同的处理逻辑
		boolean isExist = this.setLocalUserUUIDAndCreateTime(user);
		//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
			String md5pwd = user.getLoginPwd();
			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
			user.setLoginPwd(loginPwd);
			user.setMd5Pwd(md5pwd);
		}
		if (isAdd) {//如果是添加用户，如果用户已存在(用户名和手机号),则更新用户信息
			if (!isExist) {//如果本地不存在用户名和手机号都一样的用户
				List<UserVO> sameUserList = this.userDao.selectUserByUniqueKeyWithOr(user);//查询是否有重复的手机号码或者重复的用户名
				if (sameUserList != null && sameUserList.size() > 0) {
					throw new BusinessException("有重复的手机号或用户名,用户名为:{}" + user.getLoginName() + ", 手机号为:{}" + user.getPhone());
				}
			}
			userList.add(user);
			userCount = this.userDao.insertOrUpdateUserBatch(userList);//插入用户信息
		} else {//更新用户
			userCount = this.userDao.updateUserAllFieldById(user);
			
			if (userCount > 0) {//更新用户成功，然后删除用户对应的视联汇和视联管家的权限
				this.userDao.deleteUserRoleBatchByUserIdAndRoleIdList(this.generateAppDelUserRoleMap(user.getUuid()));
			}
		}
		
		if (userCount <= 0) {
			//发送操作日志消息: 异步
			this.rabbitProvider.sendLogMessage(LogTypeConstrant.ADD_USER, "会管用户添加或更新,在运维工作站添加或更新数据失败", "admin-huiguan", false, "会管平台", null, null);
			throw new BusinessException("添加/更新失败, 操作结果的数量:{} " + userCount);
		}
		
		
		//解析会管用户在会管页面的角色权限,赋予其在智能运维统一平台的角色权限
		userRoleRelList = this.parseWebPermissionInCms(userRoleRelList, user.getRole(), user.getUuid(), user.getWebLogin());
		
		//解析会管用户对应的视联汇和视联管家的是否是VIP用户的权限
		userRoleRelList = this.parseVipPermissionInCms(userRoleRelList, user.getAllowHkzs(), user.getUuid());
		
		//解析视联汇权限
		userRoleRelList = this.parseMeAppPermissionInCms(userRoleRelList, user.getPermission(), user.getUuid());
		
		//视联管家权限
		userRoleRelList = this.parsePamirMobilePermissionInCms(userRoleRelList, user.getUuid(), user.getAllowHkzs());
		
		
		//批量添加用户角色关系
		if (userRoleRelList != null && userRoleRelList.size() > 0) {
			List<String> userIdList = new ArrayList<String>();
			List<String> roleIdList = this.getFixedRoleIdForHuiguan();//获取会管、视联汇、视联管家所有角色ID的集合
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			for (Map<String, Object> userRoleMap : userRoleRelList) {
				userIdList.add(userRoleMap.get(USER_ID_STR) + "");
			}
			
			paramsMap.put(USER_ID_LIST_STR, userIdList);
			paramsMap.put(ROLE_ID_LIST_STR, roleIdList);
			
			this.userDao.deleteUserRoleBatchByUserIdAndRoleId(paramsMap);
			this.userDao.insertUserRoleRelBatch(userRoleRelList);
		}
		
		return returnData.returnResult(0, "操作成功");
	}
	
	/**
	 * 获取同步失败的用户的工作簿对象
	 * @param exportSystemType 导出哪个平台的系统类型。1：表示与会管平台同步；2：表示与流媒体平台同步；3：表示与网管平台同步
	 *                         4：表示与一机一档平台同步；5：表示与会易通平台同步
	 * @return
	 */
	@Override
	public HSSFWorkbook getSyncUserFailWorkbook(String exportSystemType) {
		List<Object> otherDataList = new ArrayList<Object>();//被同步平台失败的用户List
		List<Object> localDataList = new ArrayList<Object>();//双方平台同步失败的用户List
		HSSFRow row = null;//行数
		HSSFCell cell = null;//单元格

		//创建一个Workbook，对应一个Excel文件
		HSSFWorkbook workBook = new HSSFWorkbook();

		//生成样式 Start
		//字体样式
		HSSFFont font = workBook.createFont();//字体样式
		font.setFontName("宋体");
		font.setFontHeightInPoints((short)14);//字体大小

		//单元格样式
		HSSFCellStyle style = workBook.createCellStyle();
		style.setFont(font);//将字体注入
		style.setWrapText(false);//自动换行
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
		style.setBorderTop((short) 1);// 边框的大小
		style.setBorderBottom((short) 1);
		style.setBorderLeft((short) 1);
		style.setBorderRight((short) 1);
		//生成样式 End

		//生成两个Sheet Start
		HSSFSheet otherSheet = workBook.createSheet("被同步平台失败用户");
		HSSFSheet localSheet = workBook.createSheet("双方平台同步失败用户");
		otherSheet.setColumnWidth(3, 35*256);//第4列单元格宽
		this.setSyncUserExcelDefaultColumnWidth(otherSheet);//第1、2、3列宽度

		this.setSyncUserExcelDefaultColumnWidth(localSheet);//第1、2、3列宽度
		localSheet.setColumnWidth(3, 22*256);//第4列单元格宽
		localSheet.setColumnWidth(4, 22*256);//第5列单元格宽
		localSheet.setColumnWidth(5, 35*256);//第6列单元格宽
		localSheet.setColumnWidth(6, 35*256);//第7列单元格宽

		//合并单元格.第一个参数和第二个参数分别是起始行、最后一行；第三个参数和第四个参数分别是起始列和最后一列
		localSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
		localSheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 5));
		localSheet.addMergedRegion(new CellRangeAddress(0, 1, 6, 6));
		//生成两个Sheet End

		//去缓存取数据
		if (GlobalConstants.EXPORT_TYPE_HUIGUAN_SYSTEM.equals(exportSystemType)) {//与会管平台同步失败的用户List
			//会管平台失败的用户List
			otherDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_HUIGUAN_OTHER));
			//双方平台(会管与本平台)同步失败的用户List
			localDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_HUIGUAN_LOCAL));
		} else if (GlobalConstants.EXPORT_TYPE_LIUMEITI_SYSTEM.equals(exportSystemType)) {//与流媒体平台同步失败的用户List
			//流媒体平台失败的用户List
			otherDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_LIUMEITI_OTHER));
			//双方平台(流媒体与本平台)同步失败的用户List
			localDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_LIUMEITI_LOCAL));
		} else if (GlobalConstants.EXPORT_TYPE_NET_MANAGER_SYSTEM.equals(exportSystemType)) {//与网管平台同步失败的用户List
			//网管平台失败的用户List
			otherDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_NET_MANAGER_OTHER));
			//双方平台(网管与本平台)同步失败的用户List
			localDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_NET_MANAGER_LOCAL));
		} else if (GlobalConstants.EXPORT_TYPE_VSDC_SYSTEM.equals(exportSystemType)) {//与一机一档平台同步失败的用户List
			//一机一档平台失败的用户List
			otherDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_VSDC_OTHER));
			//双方平台(一机一档与本平台)同步失败的用户List
			localDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_VSDC_LOCAL));
		} else if (GlobalConstants.EXPORT_TYPE_HUIYITONG_SYSTEM.equals(exportSystemType)) {//与会易通平台同步失败的用户List
			//会易通平台失败的用户List
			otherDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_HUIYITONG_OTHER));
			//双方平台(会易通与本平台)同步失败的用户List
			localDataList = this.jedisDao.getList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_HUIYITONG_LOCAL));
		}

		/** 生成"被同步平台失败用户"Sheet Start */
		//设置表头信息 Start
		row = otherSheet.createRow(0);
		this.setSyncUserHeaderCellOfNamePhoneTime(row, style, 0, 1, 2);//设置前三单元格的头部信息
		cell = row.createCell(3);//第4个单元格
		this.setCellValueAndStyle(cell, style, "原因");//设置单元格样式和值
		//设置表头信息 End

		//输入数据
		if (otherDataList != null && otherDataList.size() > 0) {
			for (int i = 1; i <= otherDataList.size(); i++) {
				if (otherDataList.get(i - 1) instanceof SyncUserResultEntity) {
					row = otherSheet.createRow(i);//行数
					SyncUserResultEntity userResult = (SyncUserResultEntity)otherDataList.get(i - 1);
					this.setSyncUserCellValueOfNamePhoneTime(row, "otherData", style, userResult);//存值
				}
			}
		}
		/** 生成"被同步平台失败用户"Sheet End */

		/** 生成"双方平台同步失败用户"Sheet Start */
		//表头信息 Start
		row = localSheet.createRow(0);//第一行
		cell = row.createCell(0);//单元格,第一个单元格
		this.setCellValueAndStyle(cell, style, "被同步平台用户");//设置单元格中样式和内容
		cell = row.createCell(3);//第4个单元格
		this.setCellValueAndStyle(cell, style, "本平台用户");//设置单元格中样式和内容
		cell = row.createCell(6);//第7个单元格
		this.setCellValueAndStyle(cell, style, "原因");//设置单元格中样式和内容

		row = localSheet.createRow(1);//第二行
		this.setSyncUserHeaderCellOfNamePhoneTime(row, style, 0, 1, 2);//被同步平台用户名、手机号、创建时间表头
		this.setSyncUserHeaderCellOfNamePhoneTime(row, style, 3, 4, 5);//本平台用户名、手机号、创建时间表头
		cell = row.createCell(6);//第7个单元格
		cell.setCellStyle(style);//设置单元格样式

		//输入数据
		if (localDataList != null && localDataList.size() > 0) {
			for (int i = 1; i <= localDataList.size(); i++) {
				if (localDataList.get(i - 1) instanceof SyncUserResultEntity) {
					row = localSheet.createRow(i + 1);//行数
					SyncUserResultEntity userResult = (SyncUserResultEntity)localDataList.get(i - 1);
					this.setSyncUserCellValueOfNamePhoneTime(row, "localData", style, userResult);//存值
				}
			}
		}
		//表头信息 End
		/** 生成"双方平台同步失败用户"Sheet End */
		return workBook;
	}

	/**
	 * 设置同步用户的Excel默认(用户名、手机号、创建日期)的列宽
	 * @param sheet
	 */
	private void setSyncUserExcelDefaultColumnWidth(HSSFSheet sheet) {
		sheet.setColumnWidth(0, 22*256);//第1列单元格宽
		sheet.setColumnWidth(1, 22*256);//第2列单元格宽
		sheet.setColumnWidth(2, 35*256);//第3列单元格宽
	}

	/**
	 * 设置同步用户的Excel用户名、号码、创建时间的表格头
	 * @param row 行
	 * @param style 单元格样式
	 * @param loginNameCell 用户名单元格
	 * @param phoneCell 电话号码单元格
	 * @param createTimeCell 创建时间单元格
	 */
	private void setSyncUserHeaderCellOfNamePhoneTime(HSSFRow row, HSSFCellStyle style, int loginNameCell, int phoneCell, int createTimeCell) {
		//创建row单元格，从0开始 Start
		HSSFCell cell = row.createCell(loginNameCell);//单元格,第一个单元格
		this.setCellValueAndStyle(cell, style, "用户名");//设置单元格中样式和内容
		cell = row.createCell(phoneCell);//第2个单元格
		this.setCellValueAndStyle(cell, style, "手机号");//设置单元格中样式和内容
		cell = row.createCell(createTimeCell);//第3个单元格
		this.setCellValueAndStyle(cell, style, "创建时间");//设置单元格中样式和内容
	}

	/**
	 * 设置单元格的样式和值
	 * @param cell 单元格
	 * @param style 样式
	 * @param value 内容
	 */
	private void setCellValueAndStyle(HSSFCell cell, HSSFCellStyle style, String value) {
		if (StringUtil.isNotNull(value)) {
			cell.setCellValue(value);
		}
		cell.setCellStyle(style);
	}

	/**
	 * 设置用户名、电话号码、创建时间的值的单元格
	 * @param row 行数
	 * @param dataType 数据类型。"localData"表示本双方平台同步失败的用户；"otherData"表示被同步平台同步失败的用户
	 * @param style 样式
	 * @param userResult
	 */
	private void setSyncUserCellValueOfNamePhoneTime(HSSFRow row, String dataType, HSSFCellStyle style, SyncUserResultEntity userResult) {
		HSSFCell cell = row.createCell(0);//从第1个单元格开始，设置每个单元格样式
		this.setCellValueAndStyle(cell, style, userResult.getOtherLoginName());//设置单元格的样式和内容

		cell = row.createCell(1);//第2个单元格
		this.setCellValueAndStyle(cell, style, userResult.getOtherPhone());//设置单元格的样式和内容

		cell = row.createCell(2);//第3个单元格
		this.setCellValueAndStyle(cell, style, userResult.getOtherCreateTime());//设置单元格的样式和内容

		if ("localData".equals(dataType)) {//如果是双方平台，继续设置本平台单元格内容和值
			cell = row.createCell(3);//第4个单元格
			this.setCellValueAndStyle(cell, style, userResult.getLocalLoginName());//设置单元格的样式和内容

			cell = row.createCell(4);//第5个单元格
			this.setCellValueAndStyle(cell, style, userResult.getLocalPhone());//设置单元格的样式和内容

			cell = row.createCell(5);//第6个单元格
			this.setCellValueAndStyle(cell, style, userResult.getLocalCreateTime());//设置单元格的样式和内容

			cell = row.createCell(6);//第7个单元格
			this.setCellValueAndStyle(cell, style, userResult.getReason());//设置单元格的样式和内容
		} else if ("otherData".equals(dataType)) {//如果是被同步平台，则设置原因
			cell = row.createCell(3);//第4个单元格
			this.setCellValueAndStyle(cell, style, userResult.getReason());//设置单元格的样式和内容
		}
	}

	/**
	 * 检查用户信息 1、判断Redis是否保存了loginName+platformType组成的key对应的用户信息 1.1
	 * 如果有则删除(踢掉)之前用户的登陆信息 1.2 如果没有, 则存放信息。默认超时时间为3天
	 * 
	 * @param user 用户+权限+角色信息
	 * @param sessionTimeoutMinute 过期时间，单位为分钟
	 * @param accessToken 访问令牌
	 * @param platformId 平台ID
	 */
	private synchronized void checkAndSaveRedis(UserVO user, Integer sessionTimeoutMinute,
			String accessToken, String platformId) {
		String loginName = user.getLoginName();// 用户登录名

		// 用户名+平台类型+平台ID组成的key
		String loginNameKey = this.getRedisKey(CommonConstrant.PREFIX_LOGIN_NAME, loginName) + "_" + platformId;
		/** 是否需要踢掉之前的用户 Start */
		UserVO userRedis = (UserVO) this.jedisDao.getObject(loginNameKey);
		if (userRedis != null) {// 之前已经有登录过的信息了
			// 删除用户信息和会话token
			this.jedisDao.del(loginNameKey);// 删除用户信息
			this.jedisDao.del(this.getRedisKey(CommonConstrant.PREFIX_TOKEN, userRedis.getAccessToken()));// 删除Token
			this.jedisDao.del(this.getRedisKey(CommonConstrant.PREFIX_PLATFORM_ID, userRedis.getAccessToken()));//删除平台信息
			
		}
		/** 是否需要踢掉之前的用户 End */

		// 向Redis缓存存放新的用户信息
		user.setAccessToken(accessToken);// 用户里保存Token信息
		this.jedisDao.set(this.getRedisKey(CommonConstrant.PREFIX_TOKEN, accessToken), loginName,
				TimeUtil.getSecondsByMinute(sessionTimeoutMinute));//存储用户名：key：token
		this.jedisDao.set(this.getRedisKey(CommonConstrant.PREFIX_PLATFORM_ID, accessToken), platformId, 
				TimeUtil.getSecondsByMinute(sessionTimeoutMinute));//存储平台信息。key：token
		this.jedisDao.setObject(loginNameKey, user, TimeUtil.getSecondsByMinute(sessionTimeoutMinute));//存储用户信息
	}

	/**
	 * 拼写Redis的Key
	 * 
	 * @param prefix
	 *            前缀
	 * @param postfix
	 *            后缀
	 * @return prefix_postfix
	 */
	private String getRedisKey(String prefix, String postfix) {
		return prefix + "_" + postfix;
	}

	/**
	 * 校验在规定时间内用户是否多次重新发送验证码，默认是一分钟
	 * 
	 * @param param
	 * @return
	 */
	private boolean accessResend(String param) {
		String code = this.jedisDao.get(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, param));
		if (StringUtil.isNotNull(code)) {
			return false;
		}
		return true;
	}
	@Override
	public UserVO selectUserByUniqueKey(Map<String, Object> paramsMap) {
		UserVO user = userDao.selectUserByUniqueKey(paramsMap);
		return user;
	}

	/**
	 * 同步流媒体用户
	 */
	@Override
	public ReturnData addUserForLmt(UserVO user,LmtUserVo lmtUser, ServerConfig serverConfig) {
        BaseReturn dataReturn = new BaseReturn();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			//通过用户名和手机号查询用户数据
			paramsMap.put("loginName", user.getLoginName());
			paramsMap.put("phone", user.getPhone());
			UserVO userInfo = userDao.selectByLoginNamePhone(paramsMap);
			if (userInfo !=null) {
			   //用户名相同且手机号相同，以流媒体用户为准进行修改
	        	user.setUuid(userInfo.getUuid());
	        	//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
	        	if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
	        		String md5pwd = user.getLoginPwd();
	        		String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
	        		user.setLoginPwd(loginPwd);
	        		user.setMd5Pwd(md5pwd);
	        	}
			    int count =	this.userDao.updateUser(user);//更新用户基本信息
			    if(count>0){
			    	/**流媒体用户注册,给用户再添加一个平台默认角色,如果此用户是我们同步给流媒体的则不创建*/
			    	if(serverConfig.getPlatformId().equals(lmtUser.getPlatformId())){
			    		return dataReturn.returnResult(0, "更新成功");
			    	}
					paramsMap.clear();
					paramsMap.put("platformId", serverConfig.getOtherPlatformId());
					TRoleVO roleVo = roleDao.selectRoleIdByplatformId(paramsMap);//根据平台ID查询默认角色
					if(roleVo==null){
						return dataReturn.returnResult(1,"未找到平台默认角色，添加失败",null,null,userInfo);
					}
					paramsMap.clear();
			    	paramsMap.put("userId", userInfo.getUuid());
			    	paramsMap.put("roleId", roleVo.getUuid());
					int userRoleCount =this.userDao.selectCountByUserIdAndRoleId(paramsMap);//查询用户角色关系是否已存在
					if(roleVo!=null&&userRoleCount==0){
						paramsMap.clear();
						paramsMap.put("userId", user.getUuid());
						paramsMap.put("roleIds", roleVo.getUuid().split(","));
						this.userDao.insertUserRoleRel(paramsMap);
					}
			    	return dataReturn.returnResult(0, "更新成功");
			    }
			    return dataReturn.returnResult(1,"添加失败",null,null,userInfo);
			}
			//检查用户名是否重复
			paramsMap.clear();
			paramsMap.put("loginName", user.getLoginName());
			userInfo = userDao.selectUserByUniqueKey(paramsMap);
			if (userInfo !=null) {
				return dataReturn.returnResult(1,"用户名相同，手机号不同",null,null,userInfo);
			}
			//检查手机号是否重复
			paramsMap.clear();
			paramsMap.put("phone", user.getPhone());
			userInfo = userDao.selectUserByUniqueKey(paramsMap);
			if (userInfo !=null) {
				return dataReturn.returnResult(1,"手机号相同，用户名不同",null,null,userInfo);
			}
			//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
			if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
				String md5pwd = user.getLoginPwd();
				String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
				user.setLoginPwd(loginPwd);
				user.setMd5Pwd(md5pwd);
			}
			int count = this.userDao.insertUser(user);//执行插入数据库
			if (count > 0) {
				/** 添加用户与角色的关系 Start */
				if (StringUtil.isNotNull(user.getRoleIds())) {
					paramsMap.clear();
					paramsMap.put("userId", user.getUuid());
					paramsMap.put("roleIds", user.getRoleIds().split(","));
					this.userDao.insertUserRoleRel(paramsMap);
				}
				/**流媒体用户注册,给用户再添加一个平台默认角色*/
				paramsMap.clear();
				paramsMap.put("platformId", user.getSource());
				TRoleVO roleVo = roleDao.selectRoleIdByplatformId(paramsMap);//根据平台ID查询默认角色
				if(roleVo!=null){
					paramsMap.clear();
					paramsMap.put("userId", user.getUuid());
					paramsMap.put("roleIds", roleVo.getUuid().split(","));
					this.userDao.insertUserRoleRel(paramsMap);
				}
				/** 添加用户与角色的关系 End */
				return dataReturn.returnResult(0, "添加成功");
			}
			
		} catch (Exception e) {
			LOGGER.error("同步流媒体用户系统异常："+e);
		}
		return dataReturn.returnError("添加用户失败");
	}

	/**
	 * 解析视联汇和视联管家在会管的VIP权限,将VIP权限平移到智能运维统一接口上
	 * @param userRoleRelList 视联汇、视联管家在会管的权限对应视联汇在统一接口的用户角色关系
	 * @param allowHkzs 是否允许会控助手(视联汇和视联管家)登录。0表示禁止登录，1表示普通用户，2表示VIP用户
	 * @param userId
	 * @return
	 */
	private List<Map<String, Object>> parseVipPermissionInCms(List<Map<String, Object>> userRoleRelList, Integer allowHkzs, String userId) {
		if (allowHkzs.equals(2)) {//视联汇、视联管家的VIP用户
//			Map<String, Object> pamirMobileVipUserRoleMap = new HashMap<String, Object>();//视联管家用户角色关系Map
			Map<String, Object> meAppVipUserRoleMap = new HashMap<String, Object>();//视联汇用户角色关系Map
			
//			pamirMobileVipUserRoleMap.put(USER_ID_STR, userId);
//			pamirMobileVipUserRoleMap.put(ROLE_ID_STR, GlobalConstants.PAMIR_MOBILE_VIP_ROLE_ID);//视联管家VIP角色
			meAppVipUserRoleMap.put(USER_ID_STR, userId);
			meAppVipUserRoleMap.put(ROLE_ID_STR, GlobalConstants.ME_APP_VIP_ROLE_ID);//视联汇/PAMIR MOBILE VIP角色
			userRoleRelList.add(meAppVipUserRoleMap);
		}
		
		return userRoleRelList;
	}
	
	/**
	 * 解析用户在会管页面的权限,将会管页面的权限平移到运维工作站上
	 * @param userRoleRelList 会管的权限对应视联汇在统一接口的用户角色关系
	 * @param roleId 会管用户对应会管的角色ID
	 * @param userId 会管用户的用户ID
	 * @param cmsWebLogin 是否允许登录会管， 0-不允许，1-允许
	 * @return
	 */
	private List<Map<String, Object>> parseWebPermissionInCms(List<Map<String, Object>> userRoleRelList, String roleId, String userId, Integer cmsWebLogin) {
		//解析会管用户在会管页面的角色权限,赋予其在智能运维统一平台的角色权限 Start
		if (cmsWebLogin == null || cmsWebLogin.equals(1)) {//允许登录会管才会分配相应的运维工作站权限
			if (StringUtil.isNotNull(roleId)) {
				if (GlobalConstants.CMS_ADMIN_ROLE_ID.equals(roleId)) {//会管的超级管理员角色
					//会管的超级管理员角色对应智能运维的超级管理员角色
					Map<String, Object> userRoleMap = new HashMap<String, Object>();
					userRoleMap.put(USER_ID_STR, userId);
					userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ADMIN_ROLE_ID);//智能运维统一平台的超级管理员角色ID
					userRoleRelList.add(userRoleMap);
				}else if(GlobalConstants.INTELLIGENT_OPERATION_NOLOGIN_ROLE_ID.equals(roleId)){
					//会管的待审核普通用户角色(无登录权限)
					userRoleRelList = new ArrayList<Map<String, Object>>() ;
				} else {//会管的其他角色的权限：预约操作员、演示操作员、审批操作员
					Map<String, Object> userRoleMap = new HashMap<String, Object>();
					userRoleMap.put(USER_ID_STR, userId);
					userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ORDINARY_ROLE_ID);//兼容会管普通用户：对应会管普通用户的角色ID
					userRoleRelList.add(userRoleMap);
				}
			}
		}
		//解析会管用户在会管页面的角色权限,赋予其在智能运维统一平台的角色权限 End
		
		return userRoleRelList;
	}
	
	/**
	 * 解析视联管家在会管的权限,将视联管家在会管的权限平移到智能运维统一接口上.PS:当前视联管家的权限并没有在会管上控制
	 * @param userRoleRelList
	 * @param userId
	 * @param allowHkzs 是否允许会控助手登录。0禁止登录 1普通用户，2vip用户
	 * @return
	 */
	private List<Map<String, Object>> parsePamirMobilePermissionInCms(List<Map<String, Object>> userRoleRelList, String userId, Integer allowHkzs) {
		//视联管家的权限在会管并没有控制，所以直接赋予视联管家的所有权限
		
		if (allowHkzs.equals(1)) {//如果不是禁止登录，则赋予相应的权限
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.PAMIR_MOBILE_COMMON_ROLE_ID);//智能运维统一平台的视联管家普通用户权限的角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		
		return userRoleRelList;
	}
	
	/**
	 * 解析视联汇在会管的权限,将视联汇在会管的权限平移到智能运维统一接口上
	 * @param userRoleRelList 视联汇在会管的权限对应视联汇在统一接口的用户角色关系
	 * @param cmsUserPermission 权限
	 * @param userId 用户的UUID
	 * @return 用户角色关系
	 */
	private List<Map<String, Object>> parseMeAppPermissionInCms(List<Map<String, Object>> userRoleRelList, String cmsUserPermission, String userId) {
		String[] initCmsUserPermissionArr = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0"};//由于会管对应的视联汇和视联管家的权限可能有少于7位的数组，所以先初始化一个长度为7的数组
		
		if (StringUtil.isNotNull(cmsUserPermission)) {
			String[] cmsUserPermissionArr = cmsUserPermission.split(",");
			initCmsUserPermissionArr = Arrays.copyOf(cmsUserPermissionArr, initCmsUserPermissionArr.length);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[0]) && "1".equals(initCmsUserPermissionArr[0])) {//视联会在会管的V观大数据权限
			//对应统一接口的V观大数据角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_V_BIG_ROLE_ID);//智能运维统一平台的视联汇v观大数据权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[1]) && "1".equals(initCmsUserPermissionArr[1])) {//视联会在会管的会控权限
			//对应统一接口的会控角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_CONFERENCE_CONTROL_ROLE_ID);//智能运维统一平台的视联汇会控权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[2]) && "1".equals(initCmsUserPermissionArr[2])) {//视联会在会管的遥控器权限
			//对应统一接口的遥控器角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_REMOTE_CONTROL_ROLE_ID);//智能运维统一平台的视联汇遥控器权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[3]) && "1".equals(initCmsUserPermissionArr[3])) {//视联会在会管的点播回放权限
			//对应统一接口的点播回放角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_REPLAY_ROLE_ID);//智能运维统一平台的视联汇点播回放权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[4]) && "1".equals(initCmsUserPermissionArr[4])) {//视联会在会管的分布图权限
			//对应统一接口的分布图角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_DIAGRAM_ROLE_ID);//智能运维统一平台的视联汇分布图权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[5]) && "1".equals(initCmsUserPermissionArr[5])) {//视联会在会管的唐古拉监控权限
			//对应统一接口的唐古拉监控角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_TGL_MONITOR_ROLE_ID);//智能运维统一平台的视联汇唐古拉监控权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[6]) && "1".equals(initCmsUserPermissionArr[6])) {//视联会在会管的运维工具权限
			//对应统一接口的运维工具角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_OPERATION_TOOLS_ROLE_ID);//智能运维统一平台的视联汇运维工具权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[7]) && "1".equals(initCmsUserPermissionArr[7])) {//视联会在会管的会议信息权限
			//对应统一接口的会议信息角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_CONFERENCE_INFO_ROLE_ID);//智能运维统一平台的视联汇会议信息权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		
		if (StringUtil.isNotNull(initCmsUserPermissionArr[8]) && "1".equals(initCmsUserPermissionArr[8])) {//视联会在会管的智能音箱权限
			//对应统一接口的智能音箱角色
			Map<String, Object> userRoleMap = new HashMap<String, Object>();
			userRoleMap.put(USER_ID_STR, userId);
			userRoleMap.put(ROLE_ID_STR, GlobalConstants.INTELLIGENT_OPERATION_ME_APP_SOUND_BOX_ROLE_ID);//智能运维统一平台的视联汇智能音箱权限角色ID
			userRoleRelList.add(userRoleMap);
		}
		return userRoleRelList;
	}
	
	/**
	 * 生成用户角色关系Map，在会管修改用户的时候，先删除运维工作站对应的视联汇和视联管家用户角色关系，然后再添加。此处生成用户角色关系Map
	 * @param userId
	 * @return
	 */
	private Map<String, Object> generateAppDelUserRoleMap(String userId) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("userId", userId);
		paramsMap.put("roleIds", getFixedRoleIdForHuiguan());
		return paramsMap;
	}
	
	/**
	 * 生成同步用户结果的实体类
	 * @param localUser 本地用户信息
	 * @param otherLoginName 被同步平台用户名
	 * @param otherPhone 被同步平台用户的手机号
	 * @param otherCreateTime 被同步平台用户的创建时间
	 * @param reason 原因
	 * @return
	 */
	private SyncUserResultEntity generateSyncUserResult(UserVO localUser, String otherLoginName, String otherPhone, String otherCreateTime, String reason) {
		SyncUserResultEntity syncUserResult = new SyncUserResultEntity();
		
		if (localUser != null) {
			syncUserResult.setLocalLoginName(localUser.getLoginName());
			syncUserResult.setLocalPhone(localUser.getPhone());
			syncUserResult.setLocalCreateTime(localUser.getCreateTime());
		}
		
		syncUserResult.setOtherLoginName(otherLoginName);
		syncUserResult.setOtherPhone(otherPhone);
		syncUserResult.setOtherCreateTime(otherCreateTime);
		syncUserResult.setReason(reason);
		return syncUserResult;
	}
	
	private List<String> getFixedRoleIdForHuiguan() {
		List<String> fixedRoleIdList = new ArrayList<String>();
		
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ADMIN_ROLE_ID);//会管的超级管理员角色，对应运维工作站的所有功能 
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ORDINARY_ROLE_ID);//对应会管普通用户的角色ID 
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_V_BIG_ROLE_ID);//视联汇V观大数据权限的角色ID
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_CONFERENCE_CONTROL_ROLE_ID);//视联汇会控权限的角色ID 
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_REMOTE_CONTROL_ROLE_ID);//视联汇遥控器权限的角色ID
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_REPLAY_ROLE_ID);//视联汇点播回放权限的角色ID 
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_DIAGRAM_ROLE_ID);//视联汇分布图权限的角色ID
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_TGL_MONITOR_ROLE_ID);//视联汇唐古拉监控权限的角色ID 
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_OPERATION_TOOLS_ROLE_ID);//视联汇运维工具权限的角色ID 
//		fixedRoleIdList.add(GlobalConstants.PAMIR_MOBILE_VIP_ROLE_ID);//视联管家VIP权限的角色ID
		fixedRoleIdList.add(GlobalConstants.PAMIR_MOBILE_COMMON_ROLE_ID);//视联管家普通用户权限的角色ID 
		fixedRoleIdList.add(GlobalConstants.ME_APP_VIP_ROLE_ID);//Pamir Mobile/视联汇VIP权限的角色ID
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_SOUND_BOX_ROLE_ID);//视联汇智能音箱权限角色ID
		fixedRoleIdList.add(GlobalConstants.INTELLIGENT_OPERATION_ME_APP_CONFERENCE_INFO_ROLE_ID);//视联汇会议信息权限角色ID
		return fixedRoleIdList;
	}
	
	/**
	 * 接续会管用户和手机号与运维工作站用户名和手机号相同，但是UUID不同的逻辑：
	 * 		通过手机号和用户名查询本地数据库，如果UUID不同，那么就将本地UUID设置为cmsUser的UUID
	 * 		如果创建时间为空，那么设置本地创建时间
	 * 		将会管的UUID记录到ext1字段中去
	 * @param cmsUser
	 * @return
	 * 		true: 如果本地存在用户名和手机号都一样的用户，则为true
	 * 		false: 如果本地不存在用户名和手机号都一样的用户，则为false
	 */
	private boolean setLocalUserUUIDAndCreateTime(UserVO cmsUser) {
		boolean flag = false;
		cmsUser.setExt1(cmsUser.getUuid());//将会管的UUID设置成本地扩展字段
		
		//解决会管用户名和手机号与运维工作站用户名和手机号相同，但是UUID不同 Start
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("loginName", cmsUser.getLoginName());
		paramsMap.put("phone", cmsUser.getPhone());
		UserVO localUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户名和手机号查询
		if (localUser != null) {
			if (!localUser.getUuid().equals(cmsUser.getUuid())) {//UUID不同
				cmsUser.setUuid(localUser.getUuid());//设置成本地UUID
			}
			flag = true;
		}
		//解决会管用户名和手机号与运维工作站用户名和手机号相同，但是UUID不同 End
		
		//解决会管创建时间没有传过来的问题 Start
		if (StringUtil.isNull(cmsUser.getCreateTime())) {
			cmsUser.setCreateTime(TimeUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
		}
		//解决会管创建时间没有传过来的问题 End
		
		return flag;
	}
	
	/**
	 * 同步流媒体用户
	 * @param otherPlatformId 平台ID
	 * @param token 访问令牌
	 * @return
	 */
	@Override
	public ReturnData syncUserForLmt(String otherPlatformId, String token) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> lmtParamsMap = new HashMap<String, Object>();//调用流媒体同步用户信息的接口传递的参数Map
		JSONObject jsonObjectResult = new JSONObject();//调用流媒体获取用户信息的结果
		List<UserVO> lmtUserListAll = new ArrayList<UserVO>();//存储流媒体所有用户信息的List
		Set<UserVO> lmtSamePhoneUserSet = new LinkedHashSet<UserVO>();//存储流媒体用户名相同，手机号也相同的数据
		List<SyncUserResultEntity> otherDataList = new ArrayList<SyncUserResultEntity>();//被同步平台失败用户
		List<SyncUserResultEntity> localDataList = new ArrayList<SyncUserResultEntity>();//双方平台同步失败用户
		Map<String, Object> extraDataMap = new HashMap<String, Object>();//同步结果数量Map
		List<Map<String, Object>> userRoleRelList = new ArrayList<Map<String, Object>>();//数据中心数据看用户角色关系List
		int totalLmtUserCount = 0;//流媒体用户的总数量
//		String currentTime = TimeUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss");//当前系统时间字符串
		
		//查询流媒体配置信息: 调用流媒体同步用户信息的接口使用的参数 Start
		ServerConfig serverConfigParams = new ServerConfig();
		serverConfigParams.setOtherPlatformId(otherPlatformId);
		ServerConfig serverConfig = this.serverConfigService.get(serverConfigParams);
		
		if (serverConfig == null) {
			LOGGER.warn("流媒体配置信息为空，无法满足流媒体用户同步参数的接口传递");
			return returnData.returnError("请先配置流媒体信息");
		}
		if (StringUtil.isNull(serverConfig.getToken())) {
			LOGGER.warn("流媒体配置信息的Token为空，无法满足流媒体用户同步参数的接口传递, 配置信息为: {}", JSONObject.toJSONString(serverConfig));
			return returnData.returnError("请先配置流媒体Token信息");
		}
		if (StringUtil.isNull(serverConfig.getPlatformId())) {
			LOGGER.warn("流媒体配置信息的平台ID为空，无法满足流媒体用户同步参数的接口传递, 配置信息为: {}", JSONObject.toJSONString(serverConfig));
			return returnData.returnError("请先配置流媒体平台ID信息");
		}
		//查询流媒体配置信息: 调用流媒体同步用户信息的接口使用的参数 End
		
		
		//调用流媒体获取用户信息接口 Start
		lmtParamsMap.put("pageIndex", 1);//页码
		lmtParamsMap.put("pageSize", 1000);//页大小
		lmtParamsMap.put("token", serverConfig.getToken());//参数Token
		lmtParamsMap.put("platformId", serverConfig.getPlatformId());//参数平台ID
		
		try {
			LOGGER.info("调用流媒体地址为：{}", serverConfig.getLmtUrl() + 
					String.format(WsConstants.URL_LMT_PLATFORMUSER_GETUSERLIST));
			jsonObjectResult  = RestTemplateUtil.postForObject(serverConfig.getLmtUrl() + 
					String.format(WsConstants.URL_LMT_PLATFORMUSER_GETUSERLIST), lmtParamsMap, JSONObject.class);
		} catch (Exception e) {
			LOGGER.warn("无法连接流媒体服务器, 调用的流媒体信息参数为: {}", JSONObject.toJSONString(lmtParamsMap));
			return returnData.returnError("无法连接流媒体服务器, 请联系流媒体管理员");
		}
		
		if (jsonObjectResult == null || StringUtil.isNotNull(jsonObjectResult.getString("errmsg"))) {
			LOGGER.warn("获取流媒体用户信息失败, 流媒体返回的参数是: {}", jsonObjectResult.toJSONString());
			return returnData.returnError("同步流媒体用户信息失败: " + jsonObjectResult.getString("errmsg"));
		}
		
		if(jsonObjectResult.getInteger("state").equals(0)){//获取流媒体用户信息成功
			Integer recordCount = jsonObjectResult.getJSONObject("data").getInteger("recordCount");//流媒体总条数
			Integer count = recordCount % 1000 == 0 ? recordCount / 1000 : (recordCount / 1000) + 1;//计算下一页
			//通过计算的分页循环调用流媒体获取用户信息的接口, 并将用户信息的List封装成一个存储所有流媒体用户信息的List Start
			for(int i = 1; i <= count; i++) {//循环调用用户接口
				if(i != 1){
					lmtParamsMap.put("pageIndex", i);//页码
					lmtParamsMap.put("pageSize", 1000);//页大小
					lmtParamsMap.put("token", serverConfig.getToken());//参数Token
					lmtParamsMap.put("platformId", serverConfig.getPlatformId());//参数平台ID
					try {
						jsonObjectResult  = RestTemplateUtil.postForObject(serverConfig.getLmtUrl() + 
								String.format(WsConstants.URL_LMT_PLATFORMUSER_GETUSERLIST), lmtParamsMap, JSONObject.class);
					} catch (Exception e) {
						LOGGER.warn("无法连接流媒体服务器, 调用的流媒体信息参数为: {}", JSONObject.toJSONString(lmtParamsMap));
						return returnData.returnError("无法连接流媒体服务器, 请联系流媒体管理员");
					}
					
					if (jsonObjectResult == null || StringUtil.isNotNull(jsonObjectResult.getString("errmsg"))) {
						LOGGER.warn("获取流媒体用户信息失败, 流媒体返回的参数是: {}", jsonObjectResult.toJSONString());
						return returnData.returnError("同步流媒体用户信息失败: " + jsonObjectResult.getString("errmsg"));
					}
				}
				JSONObject lmtUserJSONObject = jsonObjectResult.getJSONObject("data");//流媒体用户数据
				JSONArray userJsonArray = lmtUserJSONObject.getJSONArray("list");//获取用户信息的JSONArray
				if (userJsonArray == null || userJsonArray.size() <= 0) {
					LOGGER.warn("获取流媒体用户信息失败, 流媒体返回的参数是: {}", jsonObjectResult.toJSONString());
					return returnData.returnError("同步流媒体用户信息失败, 流媒体返回的用户List为空");
				}
				List<UserVO> userList = userJsonArray.toJavaList(UserVO.class);
				for(UserVO user:userList){
					String loginName =user.getLoginName();
					String loginPwd =user.getLoginPwd();
					//校验密码加密方式，如果是md5加密，则根据加密规则sm3（login_name+md5（login_pwd））对密码加密后与数据库返回密码进行比对
					if(loginPwd.length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
						String sm3Pwd = Sm3Utils.encrypt(loginName+loginPwd);//最终的用户密码
						user.setMd5Pwd(loginPwd);
						user.setLoginPwd(sm3Pwd);
					}
				}
				
				lmtUserListAll.addAll(userList);
				totalLmtUserCount = lmtUserListAll.size();//流媒体用户的总数量
			}
			//通过计算的分页循环调用流媒体获取用户信息的接口, 并将用户信息的List封装成一个存储所有流媒体用户信息的List End
		}
		//调用流媒体获取用户信息接口 End
		
		//计算流媒体用户用户名相同(流媒体的主键),手机号也相同的数据 Start
		for (int i = 0; i < lmtUserListAll.size() - 1; i ++) {
			for (int j = lmtUserListAll.size() - 1; j > i; j --) {
				if (lmtUserListAll.get(i).getPhone().equals(lmtUserListAll.get(j).getPhone())) {
					lmtSamePhoneUserSet.add(lmtUserListAll.get(i));
					lmtSamePhoneUserSet.add(lmtUserListAll.get(j));
				}
			}
		}
		//计算流媒体用户用户名相同(流媒体的主键),手机号也相同的数据 End
		
		//被同步平台(流媒体),用户的手机号相同处理逻辑 Start
		if (lmtSamePhoneUserSet != null && lmtSamePhoneUserSet.size() > 0) {//流媒体平台手机号相同的用户
			for (UserVO samePhoneUser : lmtSamePhoneUserSet) {
				SyncUserResultEntity syncUserResult = 
						this.generateSyncUserResult(null, samePhoneUser.getLoginName(), samePhoneUser.getPhone(), samePhoneUser.getCreateTime(), REASON_SAME_PHONE);
				otherDataList.add(syncUserResult);
			}
			lmtUserListAll.removeAll(lmtSamePhoneUserSet);//去除号码重复的用户
		}
		//被同步平台(流媒体),用户的手机号相同处理逻辑 End
		
		//双方平台同步(流媒体和本平台)失败的处理逻辑 Start
		Iterator<UserVO> it = lmtUserListAll.iterator();
		while (it.hasNext()){
			UserVO lmtUser = it.next();
			
			if (StringUtils.isBlank(lmtUser.getPhone())) {//流媒体手机号为空
				it.remove();
				otherDataList.add(this.generateSyncUserResult(null, lmtUser.getLoginName(), lmtUser.getPhone(), lmtUser.getCreateTime(), REASON_CMS_USER_NULL_PHONE));
				continue;
			}
			
			//通过用户登录名或者电话号码查询用户信息
			List<UserVO> userList = this.userDao.selectUserByUniqueKeyWithOr(lmtUser);
			
			if(userList != null && userList.size() > 0) {
				for (UserVO user : userList) {
					if (StringUtil.isNotNull(lmtUser.getPhone()) && StringUtil.isNotNull(user.getPhone())) {
						//流媒体用户与数据中心用户登录名一样，但是手机号不一样
						if ((lmtUser.getLoginName().equals(user.getLoginName())) && !(lmtUser.getPhone().equals(user.getPhone()))) {
							it.remove();
							localDataList.add(this.generateSyncUserResult(user, lmtUser.getLoginName(), lmtUser.getPhone(), lmtUser.getCreateTime(), REASON_SAME_LOGIN_NAME));
							break;
						}
						
						//流媒体用户与数据中心用户登录名不一样，但是手机号一样
						if (!(lmtUser.getLoginName().equals(user.getLoginName())) && (lmtUser.getPhone().equals(user.getPhone()))) {
							it.remove();
							localDataList.add(this.generateSyncUserResult(user, lmtUser.getLoginName(), lmtUser.getPhone(), lmtUser.getCreateTime(), REASON_SAME_PHONE));
							break;
						}
					}
					if (!user.getUuid().equals(lmtUser.getUuid())) {//以数据库的用户UUID为准
						lmtUser.setUuid(user.getUuid());
					}
					if (StringUtil.isNull(lmtUser.getPermission())) {//以数据库的permission为准
						lmtUser.setPermission(user.getPermission());
					}
				}
			}
			
			//补齐数据 Start
			if (StringUtil.isNull(lmtUser.getUuid())) {
				lmtUser.setUuid(StringUtil.get32UUID());//设置用户UUID
			}
			if (StringUtil.isNull(lmtUser.getSource())) {
				lmtUser.setSource(lmtUser.getPlatformId());//将平台设置为用户的来源
			}
			if (StringUtil.isNotNull(lmtUser.getRealName())) {
				lmtUser.setName(lmtUser.getRealName());//用户真实姓名
			}
			if (StringUtil.isNull(lmtUser.getPermission())) {
				lmtUser.setPermission("0,0,0,0,0,0,0,0,0");//只是流媒体用户设置视联汇权限为空，防止会管同步数据中心用户的时候出现异常问题
			}
			//补齐数据 End
		}
		
		//将用户的手机号相同List存放数据库，用于导出Excel
		this.jedisDao.setList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_LIUMEITI_OTHER), 
				otherDataList, TimeUtil.getSecondsByMinute(null));
		//将用户的手机号相同用户名不同或者用户名相同手机号不同的结果List存放数据库，用于导出Excel
		this.jedisDao.setList(this.getRedisKey(CommonConstrant.PREFIX_SYNC_USER, GlobalConstants.SYNC_USER_LIUMEITI_LOCAL), 
				localDataList, TimeUtil.getSecondsByMinute(null));
		//双方平台同步(流媒体和本平台)失败的处理逻辑 End
		
		//批量添加/更新用户信息
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		if (lmtUserListAll != null && lmtUserListAll.size() > 0) {
			this.userDao.insertOrUpdateUserBatch(lmtUserListAll);
			//批量添加用户角色关系 Start
			paramsMap.put("platformId", otherPlatformId);
			TRoleVO lmtDefaultRole = roleDao.selectRoleIdByplatformId(paramsMap);//根据平台ID查询默认角色
			for (UserVO lmtUser : lmtUserListAll) {
				Map<String, Object> userRoleFixMap = new HashMap<String, Object>();//流媒体固定角色ID与用户的关系Map
				userRoleFixMap.put(USER_ID_STR, lmtUser.getUuid());//用户ID
				userRoleFixMap.put(ROLE_ID_STR, lmtUser.getRoleIds());//固定角色ID(流媒体传递)

				Map<String, Object> userRoleDefaultMap = new HashMap<String, Object>();//流媒体默认角色ID与用户的关系Map
				userRoleDefaultMap.put(USER_ID_STR, lmtUser.getUuid());//用户ID
				userRoleDefaultMap.put(ROLE_ID_STR, lmtDefaultRole.getUuid());//默认角色ID
				userRoleRelList.add(userRoleFixMap);
				userRoleRelList.add(userRoleDefaultMap);
			}
			if (userRoleRelList != null && userRoleRelList.size() > 0) {
				this.userDao.insertUserRoleRelBatch(userRoleRelList);
			}
			//批量添加用户角色关系 End
		}

		extraDataMap.put(OTHER_DATA_LIST_KEY, otherDataList);//被同步平台失败用户的List
		extraDataMap.put(LOCAL_DATA_LIST_KEY, localDataList);//双方平台同步失败用户
		extraDataMap.put("userSyncCount", totalLmtUserCount);//用户同步的总数量
		extraDataMap.put("successUserSyncCount", lmtUserListAll.size());//同步成功数量
		extraDataMap.put("errorUserSyncCount", totalLmtUserCount - lmtUserListAll.size());//同步失败数量
		extraDataMap.put(GlobalConstants.EXPORT_TYPE_KEY, GlobalConstants.EXPORT_TYPE_LIUMEITI_SYSTEM);//导出会管平台的系统类型
		
		//发送操作日志消息: 异步
		this.rabbitProvider.sendLogMessage(LogTypeConstrant.SYNC_HUIGUAN_USER, "同步流媒体用户成功", true, null, token);
		return returnData.returnResult(0, "同步成功", null, null, extraDataMap);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Deprecated
	public ReturnData syncUserListForLmt(String otherPlatformId) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> extraMap = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		List<SyncUserResultEntity> localList = new ArrayList<SyncUserResultEntity>();
		List<SyncUserResultEntity> otherList = new ArrayList<SyncUserResultEntity>();
		try {
			int success =0;
			int fail =0;
			ServerConfig param = new ServerConfig();
			param.setOtherPlatformId(otherPlatformId);
			ServerConfig config =serverConfigService.get(param);
			if(config==null){
				return returnData.returnError("流媒体配置信息不能为空");
			}
			params.put("pageIndex", 1);
			params.put("pageSize", 1000);
			params.put("token", config.getToken());
			params.put("platformId", config.getPlatformId());
			this.LOGGER.info("获取流媒体用户信息参数："+JSON.toJSONString(params));
			Map<String, Object>  result =RestClient.post(config.getLmtUrl() + String.format(WsConstants.URL_LMT_PLATFORMUSER_GETUSERLIST), null, params);
			if(StringUtil.isNotNull((String)result.get("errmsg"))){
				return returnData.returnError(result.get("errmsg")+"同步流媒体用户信息失败");
			}
			if((Integer)result.get("state")==0){
				Map<String, Object> data =(Map<String, Object>)result.get("data");
				Integer recordCount =(Integer)data.get("recordCount");
				Integer count = recordCount%1000==0?recordCount/1000:(recordCount/1000)+1;
				for(int i=1;i<=count;i++){
					if(i!=1){
						params.clear();
						params.put("pageIndex", i);
						params.put("pageSize", 1000);
						params.put("token", config.getToken());
						params.put("platformId", config.getPlatformId());
					    result =RestClient.post(config.getLmtUrl() + String.format(WsConstants.URL_LMT_PLATFORMUSER_GETUSERLIST), null, params);
					}
				    data =(Map<String, Object>)result.get("data");
					List<Object> list =(List<Object>)data.get("list");
					System.out.println("用户List: " + JSONObject.toJSONString(list));
					for(int j=0;j<list.size();j++){
						UserVO user = JSON.parseObject(JSON.toJSONString(list.get(j)), UserVO.class);
						LmtUserVo lmtUser = JSON.parseObject(JSON.toJSONString(list.get(j)), LmtUserVo.class);
						user.setName(lmtUser.getRealName());
						user.setLoginPwd(user.getLoginPwd()==null?null:user.getLoginPwd().toLowerCase());
						ReturnData ret =	this.addUserForLmt(user,lmtUser, config);
						if(ret.getErrcode()==0){
							success++;
						}else{
							fail++;
							Map<String,Object> retMap =(Map<String,Object>)ret.getData();
							UserVO retUser =(UserVO)retMap.get("extra");
							SyncUserResultEntity localEntity = new SyncUserResultEntity();
							SyncUserResultEntity otherEntity = new SyncUserResultEntity();
							localEntity.setOtherLoginName(lmtUser.getLoginName());
							localEntity.setOtherPhone(lmtUser.getPhone());
							localEntity.setOtherCreateTime(DateUtil.long2String(lmtUser.getRegisterTime()));
							localEntity.setLocalLoginName(retUser.getLoginName());
							localEntity.setLocalPhone(retUser.getPhone());
							localEntity.setLocalCreateTime(retUser.getCreateTime());
							localEntity.setReason(ret.getErrmsg());
							otherEntity.setOtherLoginName(lmtUser.getLoginName());
							otherEntity.setOtherPhone(lmtUser.getPhone());
							otherEntity.setOtherCreateTime(DateUtil.long2String(lmtUser.getRegisterTime()));
							otherEntity.setReason(ret.getErrmsg());
							localList.add(localEntity);
							otherList.add(otherEntity);
							
						}
					}
				}
				this.jedisDao.setList(CommonConstrant.PREFIX_SYNC_USER+"_"+GlobalConstants.SYNC_USER_LIUMEITI_LOCAL,localList,TimeUtil.getSecondsByMinute(null));
				this.jedisDao.setList(CommonConstrant.PREFIX_SYNC_USER+"_"+GlobalConstants.SYNC_USER_LIUMEITI_OTHER,otherList,TimeUtil.getSecondsByMinute(null));
				extraMap.put("userSyncCount", recordCount);
				extraMap.put("successUserSyncCount", success);
				extraMap.put("errorUserSyncCount", fail);
				extraMap.put("localDataList", localList);
				extraMap.put("otherDataList", otherList);
				return returnData.returnResult(0, "同步流媒体用户信息成功", null, null, extraMap);
			}
			
		} catch (Exception e) {
			this.LOGGER.error("同步流媒体用户信息失败 ===== UserController ===== syncUserListForLmt =====> ", e);
			return returnData.returnError("同步流媒体用户信息失败");
		}
		return returnData.returnError("同步流媒体用户信息失败");
	}
	
	/**
	 * 流媒体添加用户的业务处理
	 * @param user 用户信息
	 * @param token 访问令牌
	 * @return
	 */
	@Override
	public ReturnData addUserForLmt(UserVO user, String token) {
		BaseReturn dataReturn = new BaseReturn();
		LOGGER.info("流媒体传递的用户信息为: " + JSONObject.toJSONString(user));
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("loginName", user.getLoginName());
		paramsMap.put("phone", user.getPhone());
		UserVO localUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户名和手机号联合查询用户是否存在
		
		//兼容流媒体老版本，判断密码加密方式
		//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
			String md5pwd = user.getLoginPwd();
			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
			user.setLoginPwd(loginPwd);
			user.setMd5Pwd(md5pwd);
		}
		
		//设置流媒体用户角色信息 Start
		paramsMap.clear();
		paramsMap.put("platformId", user.getSource());
		TRoleVO lmtDefaultRole = roleDao.selectRoleIdByplatformId(paramsMap);//根据平台ID查询默认角色
		String roleIds = user.getRoleIds();//用户角色
		if (lmtDefaultRole != null) {
			roleIds = roleIds + "," + lmtDefaultRole.getUuid();
		} else {
			LOGGER.warn("该平台: {} 没有默认角色", user.getSource());
		}
		//设置流媒体用户角色信息 End
		
		if (localUser != null) {//如果用户名和手机号的用户信息存在,则更新用户信息
			//补齐数据
			user.setPermission(localUser.getPermission());//为了兼容会管同步用户功能
			user.setUuid(localUser.getUuid());//以本地数据库的UUID为准
			this.userDao.updateUser(user);//更新用户基本信息
		} else {//走正常添加逻辑
			//判断用户名或者手机号是否存在该用户 Start
			List<UserVO> sameUserList = this.userDao.selectUserByUniqueKeyWithOr(user);//查询是否有重复的手机号码或者重复的用户名
			if (sameUserList != null && sameUserList.size() > 0) {
				throw new BusinessException("有重复的手机号或用户名,用户名为:{}" + user.getLoginName() + ", 手机号为:{}" + user.getPhone());
			}
			//判断用户名或者手机号是否存在该用户 End
			user.setPermission("0,0,0,0,0,0,0,0,0");//为了兼容会管同步用户功能
			this.userDao.insertUser(user);
		}
		//添加用户角色关系
		paramsMap.clear();
		paramsMap.put("userId", user.getUuid());
		paramsMap.put("roleIds", roleIds.split(","));
		this.userDao.insertUserRoleRel(paramsMap);//添加用户角色关系
		
		return dataReturn.returnResult(0, "添加用户成功");
	}

	/**
	 * 重置用户的密码
	 * @param user {"phone":"","verifiCode":"","loginPwd":""}
	 * 		phone: 手机号; verifiCode: 验证码; loginPwd: 密码，必须为MD5加密后的密码
	 * @return
	 */
	@Override
	public ReturnData resetPassword(UserVO user) {
		BaseReturn dataReturn = new BaseReturn();
		String code = this.jedisDao.get(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()));//获取缓存中的验证码
		if (StringUtil.isNull(code)) {
			LOGGER.warn("验证码已经过期, 请重新获取验证码");
			return dataReturn.returnError("验证码已过期,请重新获取验证码");
		}

		if (!code.equals(user.getVerifiCode())) {
			LOGGER.warn("验证码不正确, 正确验证码为: {}, 用户输入的验证码为: {}", code, user.getVerifiCode());
			return dataReturn.returnError("验证码不正确");
		}

		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("phone", user.getPhone());
		int userCount = this.userDao.selectUserCountByUniqueKey(paramsMap);//用户数量
		if (userCount < 1) {
			LOGGER.warn("用户不存在, 用户的手机号为: {}", user.getPhone());
			return dataReturn.returnError("用户不存在");
		}

		//更新用户的手机号
		user.setUpdateTime(TimeUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
		this.userDao.updateUserPasswordByPhone(user);
		return dataReturn.returnResult(WsConstants.OK, "重置密码成功");
	}

	/**
	 * 注册用户信息
	 * @param user 用户信息 {"loginName":"","name":"","loginPwd":"","verifiCode":"","areaId":"","roleIds":"","areaName":"","platformId":""}
	 * @return
	 */
	@Override
	public ReturnData userRegister(UserVO user) {
		BaseReturn dataReturn = new BaseReturn();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		LOGGER.info("P-Server/掌上通传递的用户信息为: {}", JSONObject.toJSONString(user));

		//校验验证码信息 Start
		String code = this.jedisDao.get(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()));//获取缓存中的验证码
		if (StringUtil.isNull(code)) {
			LOGGER.warn("验证码已经过期, 请重新获取验证码");
			return dataReturn.returnError("验证码已过期,请重新获取验证码");
		}

		if (!code.equals(user.getVerifiCode())) {
			LOGGER.warn("验证码不正确, 正确验证码为: {}, 用户输入的验证码为: {}", code, user.getVerifiCode());
			return dataReturn.returnError("验证码不正确");
		}
		//校验验证码信息 End

		int userCount = 0;//用户数量
		//检查用户名是否重复 Start
		paramsMap.clear();
		paramsMap.put("loginName", user.getLoginName());
		userCount = this.userDao.selectUserCountByUniqueKey(paramsMap);//查询用户的数量
		if (userCount > 0) {
			LOGGER.warn("用户名称已经存在, 用户名称为: {}", user.getLoginName());
			return dataReturn.returnError("用户名已存在");
		}
		//检查用户名是否重复 End

		//检查手机号是否重复 Start
		paramsMap.clear();
		paramsMap.put("phone", user.getPhone());
		userCount = this.userDao.selectUserCountByUniqueKey(paramsMap);
		if (userCount > 0) {
			LOGGER.warn("用户的手机号已存在, 手机号为: {}", user.getPhone());
			return dataReturn.returnError("[" + user.getPhone() + "]手机号已存在");
		}
		//检查手机号是否重复 End

		user.setSource(user.getPlatformId());
		//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
			String md5pwd = user.getLoginPwd();
			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
			user.setLoginPwd(loginPwd);
			user.setMd5Pwd(md5pwd);
		}
		this.userDao.insertUser(user);//执行插入数据库
		/** 添加用户与行业归属的关系 Start */
		if (StringUtil.isNotNull(user.getIndustryId())) {
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			paramsMap.put("industryIds", user.getIndustryId().split(","));
			this.industryDao.insertUserIndustry(paramsMap);
		}
		/** 添加用户与行业归属的关系 End */

		/** 添加用户与角色的关系 Start */
		if (StringUtil.isNotNull(user.getRoleIds())) {
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			paramsMap.put("roleIds", user.getRoleIds().split(","));
			this.userDao.insertUserRoleRel(paramsMap);
		}
		/** 添加用户与角色的关系 End */
		return dataReturn.returnResult(WsConstants.OK, "添加成功");
	}

	/**
	 * 通过用户的主键ID查询用户的密码
	 * 
	 * @param userId
	 *            用户主键ID
	 * @return 用户的密码
	 */
	@Override
	public String getUserPwdByUserId(String userId) {
		final String pwd = this.userDao.selectPwdById(userId);
		if (StringUtils.isBlank(pwd)) {
			LOGGER.warn("没有找到该用户或者该用户的密码为空, 用户的主键ID是: {}", userId);
		}
		return pwd;
	}

	@Override
	public ReturnData sendCodeByLoginNameAndLoginPwd(UserVO userParam) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> paramsMap = new HashMap<>();// 向DAO传递的参数Map
		Map<String, Object> extraData = new HashMap<>();// 结果Map
		extraData.put("isPwdExpires", false);
		extraData.put("countdown", 0);
		paramsMap.put("loginName", userParam.getLoginName());
		/** 校验用户信息 Start */
		UserVO user = this.userDao.selectUserByUniqueKey(paramsMap);// 查询用户信息
		if (user == null) {// 用户不存在
			return returnData.returnError("用户不存在");
		}
		// 验证用户密码是否过期
		Date startModifyDate = user.getPwdModifyTime();
		if(startModifyDate==null){
			Date currentDate =new Date();
			startModifyDate = currentDate ;
			UserVO params = new UserVO();
			params.setUuid(user.getUuid());
			params.setPwdModifyTime(currentDate);
			this.userDao.updateUser(params);// 更新用户基本信息
		}
		Date endModifyDate = DateUtil.string2Date(DateUtil.addDateDays(startModifyDate, modityDay));
		if (!DateUtil.isEffectiveDate(new Date(), startModifyDate, endModifyDate)) {
			extraData.put("isPwdExpires", true);
			return returnData.returnResult(1, "账号密码已过期，请修改密码后重试", null, null, extraData);
		}
		int failNum = user.getFailNum() == null ? 0 : user.getFailNum();
		ReturnData resultCheck =this.failCheckLoginHandle(user,userParam,1);
		if(resultCheck.getErrcode()!=0){
			return resultCheck;
		}
		if(failNum!=0){
			UserVO params = new UserVO();
			params.setUuid(user.getUuid());
			params.setFailNum(0);
			this.userDao.updateUser(params);//密码验证成功重置次数
		}
		ReturnData result = this.sendVirificationCode(userParam, 2);
		if (result.getErrcode() != 0) {
			return returnData.returnError(result.getErrmsg());
		}

		return returnData.returnResult(0, "发送验证码成功", null, null, null);
	}
	
	/**
	 * 限制登录检查
	 * 
	 * @param user  用户信息
	 * @param checkType   1：验证用户名密码 2：验证用户名密码和验证码
	 * @return
	 */
	public ReturnData failCheckLoginHandle(UserVO user, UserVO userParam, int checkType) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> extraData = new HashMap<String, Object>();// 结果Map
		extraData.put("isPwdExpires", false);
		extraData.put("countdown", 0);
		String typeName = "";
		if (checkType == 1) {
			typeName = "用户名或密码";
		}else{
			typeName = "密码或验证码";
		}
		int failNum = user.getFailNum() == null ? 0 : user.getFailNum();
		int surplusNum = limitNum - (failNum + 1) > 0 ? limitNum - (failNum + 1) : 0;// 剩余登录次数
		if (surplusNum == 0) {
			String limitDate = DateUtil.date2String(user.getLimitLoginTime());
			String currentDate = DateUtil.date2String(new Date());
			if (limitDate != null) {
				if (DateUtil.compareToCurTime(limitDate) < 0) {
					long time = DateUtil.getMarginSeconds(currentDate, limitDate);
					extraData.put("countdown", time);
					return returnData.returnResult(1, "此账号已被限制登录，请在" + time + "秒之后重试", null, null,
							extraData);
				} else {
					// 验证用户名密码
					if (checkType == 1) {
						if (user.getLoginPwd().equalsIgnoreCase(userParam.getLoginPwd())) {
							return returnData.returnSuccess("验证成功");
						} 
					// 验证用户名密码和验证码
					} else {
						if (user.getLoginPwd().equalsIgnoreCase(userParam.getLoginPwd())) {
							String code = this.jedisDao
									.get(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()));
							if (userParam.getVerifiCode().equals(code)) {
								return returnData.returnSuccess("验证成功");
							} else {
								typeName = "验证码";
							}
						} 

					}
				}
			}
			String limitLoginTime = DateUtil.addDateMinute(new Date(), limitTime);
			UserVO param = new UserVO();
			param.setUuid(user.getUuid());
			param.setFailNum(failNum + 1);
			param.setLimitLoginTime(DateUtil.string2Date(limitLoginTime));
			this.userDao.updateUser(param);// 更新用户基本信息
			long time = limitTime * 60;
			extraData.put("countdown", time);
			return returnData.returnResult(1, typeName + "错误此账号已被限制登录，请在" + time + "秒之后重试", null, null, extraData);
        //不超过次数逻辑
		} else {
			if (checkType == 1) {
				// 验证用户名密码
				if (user.getLoginPwd().equalsIgnoreCase(userParam.getLoginPwd())) {
					return returnData.returnSuccess("验证成功");
				} 

			} else {
				if (user.getLoginPwd().equalsIgnoreCase(userParam.getLoginPwd())) {
					String code = this.jedisDao
							.get(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()));
					if (userParam.getVerifiCode().equals(code)) {
						return returnData.returnSuccess("验证成功");
					} else {
						typeName = "验证码";
					}
				} else {
					typeName = "用户名或密码";
				}

			}
			UserVO params = new UserVO();
			params.setUuid(user.getUuid());
			params.setFailNum(failNum + 1);
			this.userDao.updateUser(params);// 更新失败次数
			return returnData.returnError(typeName + "错误，此账号仅剩" + surplusNum + "次登录尝试机会");
		}

	}

	@Override
	public ReturnData sendCodeByPhone(UserVO user) {
		BaseReturn returnData = new BaseReturn();// 返回的数据
		Map<String, Object> extraData = new HashMap<String,Object>();// 结果Map
		if (!accessResend(user.getPhone())) {// 校验在一分钟内只能发送一次短信验证码
			this.LOGGER.error("UserServiceImpl ----- sendCodeByPhone ---- 一分钟内只能发送一次短信");
			return returnData.returnError("一分钟内只能发送一次短信");
		}
		String verifiCode = RandomUtil.getRandomNum(4);//获取4位数字验证码
        String verifiMsg  ="【视联动力】" + "您的短信验证码为: " + verifiCode + " , 有效期为 1 分钟, 如非您本人操作, 请忽略!";
		int status = SmsAgentUtil.post(user.getPhone(),verifiMsg );// 发送到手机上
		if (status != 1) {// 发送失败
			this.LOGGER.error("UserServiceImpl ----- sendCodeByPhone ---- 发送验证码失败, 失败状态码{}:" + status);
			return returnData.returnError("发送验证码失败");
		}
		this.jedisDao.set(getRedisKey(CommonConstrant.PREFIX_VERIFICATION_CODE, user.getPhone()), verifiCode,
				TimeUtil.getSecondsByMinute(CommonConstrant.VERIFICATION_CODE_TIMEOUT_MINUTES));// 将验证码存储Redis，失效时间为1分钟
		extraData.put("verifiCode", verifiCode);
		extraData.put("verifiMsg", verifiMsg);
		return returnData.returnResult(0, "发送成功", null, null, extraData);
	}

	@Override
	public ReturnData updateUserPwd(UserVO userParam) {
		BaseReturn returnData = new BaseReturn();// 返回的数据
		Map<String, Object> paramsMap = new HashMap<String,Object>();// 向DAO传递的参数Map
		paramsMap.put("loginName", userParam.getLoginName());
		/** 校验用户信息 Start */
		UserVO user = this.userDao.selectUserByUniqueKey(paramsMap);// 查询用户信息
		if (user == null) {// 用户不存在
			return returnData.returnError("用户不存在");
		}
		if (!user.getLoginPwd().equalsIgnoreCase(userParam.getLoginPwd())) {// 密码不对
			return returnData.returnError("用户名密码不匹配");
		}
		UserVO param = new UserVO();
		Date currentDate = new Date();
		param.setUuid(user.getUuid());
		param.setLoginPwd(userParam.getNewPwd());
		param.setMd5Pwd(userParam.getMd5Pwd());
		param.setPwdModifyTime(currentDate);
		int count  =this.userDao.updateUser(param);
		if(count>0){
			return returnData.returnResult(0, "密码修改成功");
		}
		return returnData.returnError("密码修改失败");
	}

	/**
	 *  Description:更新旧用户的加密方式为sm3
	 *  @author  ==zyf==
	 *  @date 2019年11月18日 下午2:23:42 
	 *  @return
	 */
	@Override
	public ReturnData updateHistoryUser() {
		BaseReturn dataReturn = new BaseReturn();
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("md5", 32);
		//获取所有用户旧用户
		List<UserVO> list = userDao.selectOldUser(map);
		String md5Pwd = "" ;
		String loginPwd = "" ;
		//将所有旧数据用户密码加密方式更新为sm3加密
		List<UserVO> list2 = new ArrayList<UserVO>();
		for (UserVO userVO : list) {
			md5Pwd = userVO.getLoginPwd().trim() ;
			//将原有md5的密码更新至数据库md5_pwd字段供运维工作站同步用户使用
			userVO.setMd5Pwd(md5Pwd);
			//将旧md5数据密码更新为sm3加密
			loginPwd = Sm3Utils.encrypt(userVO.getLoginName()+userVO.getLoginPwd());
			userVO.setLoginPwd(loginPwd);
			list2.add(userVO);
		}
		if(list != null && list.size() > 0){
			userDao.updateBatchUser(list2);
		}
		return dataReturn.returnResult(WsConstants.OK, "更新成功");
	}

}
