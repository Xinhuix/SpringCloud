package com.visionvera.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.ServerConfig;
import com.visionvera.bean.datacore.SyncUserResultEntity;
import com.visionvera.bean.datacore.TRegionb;
import com.visionvera.bean.datacore.TRoleVO;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.PlatformTypeConstrant;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.dao.authentication.IndustryDao;
import com.visionvera.dao.authentication.RegionDao;
import com.visionvera.dao.authentication.RoleDao;
import com.visionvera.dao.authentication.UserDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.PushUserService;
import com.visionvera.service.ServerConfigService;
import com.visionvera.service.SyncUserService;
import com.visionvera.util.Sm3Utils;
import com.visionvera.util.StringUtil;
import com.visionvera.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 同步P-Server、网管、会易通、一机一档用户业务
 */
@Service
@Transactional(transactionManager = "transactionManager_authentication", rollbackFor = Exception.class,
        propagation = Propagation.REQUIRED)
public class SyncUserServiceImpl implements SyncUserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncUserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private IndustryDao industryDao;

    @Autowired
    private ServerConfigService serverConfigService;

    @Autowired
    private JRedisDao jedisDao;

    @Autowired
    private PushUserService pushUserService;

    @Autowired
    private RegionDao regionDao;

    /** 存储用户角色关系用户Key */
    private final String USER_ID_STR = "userId";

    /** 存储用户角色关系角色Key */
    private final String ROLE_ID_STR = "roleId";

    /** 删除用户角色关联关系用户ID集合的Key */
    private final String USER_ID_LIST_STR = "userIdList";

    /** 删除用户角色关联关系角色ID集合的Key */
    private final String ROLE_ID_LIST_STR = "roleIdList";

    /** 删除用户角色关联关系用户IDS集合的Key:roleIds */
    private final String ROLE_IDS_LIST_STR = "roleIds";

    /** 调用其他平台的用户信息，该参数表示传递的页大小 */
    private final Integer PAGE_SIZE_PARAMS = 1000;

    /** 调用其他平台的用户信息，该参数表示从第1页开始获取 */
    private final Integer PAGE_NUM_PARAMS = 1;

    /** 失败原因：手机号相同,用户名不同 */
    private final String REASON_SAME_PHONE = "手机号相同,用户名不同";

    /** 失败原因：用户名相同,手机号不同 */
    private final String REASON_SAME_LOGIN_NAME = "用户名相同,手机号不同";

    /** 失败原因：手机号为空 */
    private final String REASON_USER_NULL_PHONE = "手机号为空";

    /** 失败原因：手机号格式错误 */
    private final String REASON_USER_ERROR_PHONE = "手机号格式错误";

    /** 失败原因：用户名 */
    private final String REASON_USER_NULL_LOGIN_NAME = "用户名为空";

    /** 失败原因：用户真实姓名为空 */
    private final String REASON_USER_NULL_REAL_NAME = "真实姓名为空";

    /** 失败原因：手机号相同 */
    private final String REASON_USER_SAME_PHONE = "手机号相同";

    /** 失败原因：行政区域ID为空 */
    private final String REASON_USER_NULL_REGION_ID = "所属区域ID为空";

    /** 失败原因：行政区域名称为空 */
    private final String REASON_USER_NULL_REGION_NAME = "所属区域名称为空";

    /** 失败原因：密码为空 */
    private final String REASON_USER_NULL_LOGIN_PWD = "密码为空";

    /** 失败原因：所属区域非国标 */
    private final String REASON_REGION_NOT_NATIONAL_STANDARD = "所属区域非国标";

    /** 被同步平台失败用户，返回给前端的Key */
    private final String OTHER_DATA_LIST_KEY = "otherDataList";

    /** 双方平台同步失败用户，返回给前端的Key */
    private final String LOCAL_DATA_LIST_KEY = "localDataList";

    /** 默认的退出次数: 3 */
    private final int DEFAULT_RETURN_COUNT = 3;


    /**
     * P-Server添加用户
     * @param user 用户信息
     * @return 返回信息
     */
    @Override
    public ReturnData addUserForPServer(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        LOGGER.info("P-Server传递的用户信息为: {}", JSONObject.toJSONString(user));

        int userCount = 0;//用户数量

        //校验用户名和手机号是否存在，如果存在，则更新该用户 Start
        paramsMap.clear();
        paramsMap.put("phone", user.getPhone());
        UserVO dataUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户登录名和手机号查询用户
        if (dataUser != null) {
        	//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
    		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
    			String md5pwd = user.getLoginPwd();
    			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
    			user.setLoginPwd(loginPwd);
    			user.setMd5Pwd(md5pwd);
    		}
        	
            //执行更新操作
            user.setUuid(dataUser.getUuid());
            this.userDao.updateUser(user);

            /* 更新用于与角色的关系 Start */
            //根据用户UUID删除用户对应的角色关系 Start
            List<String> userIdList = new ArrayList<String>();//用户Id集合
            List<String> roleIdList = new ArrayList<String>();//角色Id集合
            userIdList.add(user.getUuid());
            roleIdList.add(user.getRoleIds());
            paramsMap.clear();
            paramsMap.put(USER_ID_LIST_STR, userIdList);
            paramsMap.put(ROLE_ID_LIST_STR, roleIdList);
            this.userDao.deleteUserRoleBatchByUserIdAndRoleId(paramsMap);
            //根据用户UUID删除用户对应的角色关系 End

            //添加用户与角色的关系 Start
            if (StringUtil.isNotNull(user.getRoleIds())) {
                String roleIds = user.getRoleIds();//用户角色
                paramsMap.clear();
                paramsMap.put("userId", user.getUuid());
                paramsMap.put("roleIds", roleIds.split(","));
                this.userDao.insertUserRoleRel(paramsMap);//添加
            }
            //添加用户与角色的关系 End
            /* 更新用于与角色的关系 End */
            return dataReturn.returnResult(WsConstants.OK, "添加用户成功");
        }
        //校验用户名和手机号是否存在，如果存在，则更新该用户 End

        user.setSource(user.getPlatformId());
        user.setUuid(StringUtil.get32UUID());
        this.userDao.insertUserWithoutUUID(user);//执行插入数据库
        /* 添加用户与行业归属的关系 Start */
        if (StringUtil.isNotNull(user.getIndustryId())) {
            paramsMap.clear();
            paramsMap.put("userId", user.getUuid());
            paramsMap.put("industryIds", user.getIndustryId().split(","));
            this.industryDao.insertUserIndustry(paramsMap);
        }
        /* 添加用户与行业归属的关系 End */

        /* 添加用户与角色的关系 Start */
        if (StringUtil.isNotNull(user.getRoleIds())) {
            paramsMap.clear();
            paramsMap.put("userId", user.getUuid());
            paramsMap.put("roleIds", user.getRoleIds().split(","));
            this.userDao.insertUserRoleRel(paramsMap);
        }
        /* 添加用户与角色的关系 End */
        return dataReturn.returnResult(WsConstants.OK, "添加用户成功");
    }

    /**
     * 通过多个手机号获取这些用户的基本信息
     * @param user {"phone":"13112341234,13412341234"} 多个手机号使用英文逗号","隔开
     * @param pageNum 页码
     * @param pageSize 页大小，为-1表示不分页
     * @return 返回信息
     */
    @Override
    public PageInfo<UserVO> getUserForPServer(UserVO user, Integer pageNum, Integer pageSize) {
        if (!pageSize.equals(-1)) {
            PageHelper.startPage(pageNum, pageSize);
        }
        String[] phoneArr = null;
        if (StringUtil.isNotNull(user.getPhone())) {
            phoneArr = user.getPhone().split(",");
        }

        List<UserVO> userList = this.userDao.selectUserByPhonesForPServer(phoneArr);
        PageInfo<UserVO> userInfo = new PageInfo<>(userList);
        return userInfo;
    }

    /**
     * P-Server根据用户手机号修改用户信息
     * @param user {"phone":"","loginPwd":"","areaId":"","areaName":""}
     * @return
     */
    public ReturnData editUserForPServer(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        //确定大小, 最大为2，所以避免自动扩容创建大小为3的Map
        Map<String, Object> paramsMap = new HashMap<String, Object>(3);
        LOGGER.info("PServer传递的需要修改的用户信息为: {}", JSONObject.toJSONString(user));
        paramsMap.clear();
        paramsMap.put("phone", user.getPhone());
        UserVO dataUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户手机号查询用户
        if (dataUser != null) {
        	//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
    		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
    			String md5pwd = user.getLoginPwd();
    			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
    			user.setLoginPwd(loginPwd);
    			user.setMd5Pwd(md5pwd);
    		}
    		
            user.setName(null);//不更新真实用户姓名
            //执行更新操作
            user.setUuid(dataUser.getUuid());
            this.userDao.updateUser(user);
            return dataReturn.returnResult(WsConstants.OK, "修改用户成功");
        } else {
            LOGGER.warn("没有该用户信息");
            return dataReturn.returnResult(WsConstants.ERROR, "没有该用户");
        }
    }

    /**
     * 添加用户：提供给网管使用
     * @param user 用户信息
     * 由于业务逻辑，网管删除的用户并不会真正的删除，所以如果存在该用户(通过用户名和手机号联合查询表示该用户存在)则更新
     * @return 返回信息
     */
    @Override
    public ReturnData addUserForNetManager(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        //确定大小, 最大为2，所以避免自动扩容创建大小为3的Map
        Map<String, Object> paramsMap = new HashMap<String, Object>(3);
        LOGGER.info("网管传递的用户信息为: {}", JSONObject.toJSONString(user));

        paramsMap.put("loginName", user.getLoginName());
        paramsMap.put("phone", user.getPhone());
        UserVO localUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户名和手机号联合查询用户是否存在
        if (localUser != null) {//用户已经存在，更新用户
            LOGGER.info("数据库中原来用户的信息是: {}", JSONObject.toJSONString(localUser));
            user.setName(null);//不更新真实用户的姓名
            user.setPermission(localUser.getPermission());//为了兼容会管同步用户功能
            user.setUuid(localUser.getUuid());//以本地数据库的UUID为准
        	//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
    		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
    			String md5pwd = user.getLoginPwd();
    			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
    			user.setLoginPwd(loginPwd);
    			user.setMd5Pwd(md5pwd);
    		}
            this.userDao.updateUser(user);//更新用户基本信息
        } else {//用户不存在, 添加用户
            //检查用户名是否重复 Start
            UserVO paramUser = new UserVO();
            paramUser.setLoginName(user.getLoginName());
            paramUser.setPhone(user.getPhone());
            List<UserVO> userList = this.userDao.selectUserByUniqueKeyWithOr(paramUser);//查询用户的数量

            if (userList != null && userList.size() > 0) {
                for (UserVO localU : userList) {
                    if (localU.getLoginName().equals(user.getLoginName()) &&
                            !localU.getPhone().equals(user.getPhone())) {
                        LOGGER.warn("网管传递的用户名和本地的用户名相同, 但是手机号不同, 不允许插入. " +
                                        "网管传递的用户名和手机号为: {}:::::{}, 本地的用户名和手机号为: {}:::::{}",
                                user.getName(), user.getPhone(), localU.getName(), localU.getPhone());
                        return dataReturn.returnError("添加失败: 用户名相同,手机号不同");
                    } else if (!localU.getLoginName().equals(user.getLoginName()) &&
                            localU.getPhone().equals(user.getPhone())) {
                        LOGGER.warn("网管传递的手机号和本地的手机号相同, 但是用户名不同, 不允许插入. " +
                                        "网管传递的用户名和手机号为: {}:::::{}, 本地的用户名和手机号为: {}:::::{}",
                                user.getName(), user.getPhone(), localU.getName(), localU.getPhone());
                        return dataReturn.returnError("添加失败: 手机号相同,用户名不同");
                    }
                    //其他情况均允许添加用户的操作
                }
            }
            //添加用户信息
            user.setSource(user.getPlatformId());
            user.setPermission("0,0,0,0,0,0,0,0,0");//为了兼容会管同步用户功能
            user.setUuid(StringUtil.get32UUID());//生成本地主键ID
            this.userDao.insertUserWithoutUUID(user);//执行插入数据库
        }

        //添加用户与角色的关系: 通过平台查询默认角色，然后添加用户-角色关系
        this.addUserDefaultRoleRel(user.getUuid(), user.getPlatformId());

        return dataReturn.returnResult(WsConstants.OK, "添加用户成功");
    }

    /**
     * 修改用户：提供给网管使用
     * @param user 用户信息
     * @return 返回信息
     */
    @Override
    public ReturnData editUserForNetManager(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        Map<String, Object> paramsMap = new HashMap<String, Object>(3);//本次只使用两个Entry数组的长度，不需要进行自动扩容
        LOGGER.info("网管传递的用户信息为: {}", JSONObject.toJSONString(user));

        paramsMap.clear();
        paramsMap.put("loginName", user.getLoginName());
        paramsMap.put("phone", user.getPhone());
        UserVO dataUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户登录名和手机号查询用户
        if (dataUser != null) {
            user.setName(null);//不更新真实用户姓名
            //执行更新操作
            user.setUuid(dataUser.getUuid());
        	//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
    		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
    			String md5pwd = user.getLoginPwd();
    			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
    			user.setLoginPwd(loginPwd);
    			user.setMd5Pwd(md5pwd);
    		}
            int success = this.userDao.updateUser(user);
            if (success > 0) {//更新成功
                //添加用户与角色的关系: 通过平台查询默认角色，然后添加用户-角色关系
                this.addUserDefaultRoleRel(user.getUuid(), user.getPlatformId());
            }
            return dataReturn.returnResult(WsConstants.OK, "修改用户成功");
        } else {
            LOGGER.warn("没有该用户信息");
            return dataReturn.returnResult(WsConstants.ERROR, "没有该用户信息");
        }
    }

    /**
     * 删除用户：提供给网管使用
     * @param user 用户信息
     * @return 返回信息
     */
    @Override
    public ReturnData delUserForNetManager(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        //可以确定本次最多使用2个长度的Map，所以为了避免其自动扩容使用长度为3
        Map<String, Object> paramsMap = new HashMap<String, Object>(3);
        LOGGER.info("网管传递的用户信息为: {}", JSONObject.toJSONString(user));

        paramsMap.clear();
        paramsMap.put("loginName", user.getLoginName());
        UserVO localUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户登录名查询用户
        if (localUser == null) {
            LOGGER.warn("本地没有该用户, 用户名为: {}", user.getName());
            return dataReturn.returnError("没有该用户");
        }

        //通过平台ID查询对应的角色，删除该用户对应的平台的角色
        return this.delUserRoleRelByPlatformId(localUser.getUuid(), user.getPlatformId());
    }

    /**
     * 添加用户：提供给会易通使用
     * 由于业务逻辑，会易通删除的用户并不会真正的删除，所以如果存在该用户(通过用户名和手机号联合查询表示该用户存在)则更新
     * @param user 用户信息
     * @return 返回信息
     */
    @Override
    public ReturnData addUserForHYT(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        //可以确定本次最多使用2个长度的Map，所以为了避免其自动扩容使用长度为3
        Map<String, Object> paramsMap = new HashMap<String, Object>(3);
        LOGGER.info("会易通传递的用户信息为: {}", JSONObject.toJSONString(user));

//		paramsMap.put("loginName", user.getLoginName());
        paramsMap.put("phone", user.getPhone());
        UserVO localUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过手机号联合查询用户是否存在
        if (localUser != null) {//用户已经存在，更新用户
            LOGGER.info("数据库中原来用户的信息是: {}", JSONObject.toJSONString(localUser));
            user.setName(null);//不更新用户的真实姓名
            user.setPermission(localUser.getPermission());//为了兼容会管同步用户功能
            user.setUuid(localUser.getUuid());//以本地数据库的UUID为准
        	//如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
    		if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
    			String md5pwd = user.getLoginPwd();
    			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
    			user.setLoginPwd(loginPwd);
    			user.setMd5Pwd(md5pwd);
    		}
            this.userDao.updateUser(user);//更新用户基本信息
        } else {//用户不存在, 添加用户
            //检查用户名是否重复 Start
            UserVO paramUser = new UserVO();
            paramUser.setLoginName(user.getLoginName());
            paramUser.setPhone(user.getPhone());
            List<UserVO> userList = this.userDao.selectUserByUniqueKeyWithOr(paramUser);//查询用户的数量

            if (userList != null && userList.size() > 0) {
                for (UserVO localU : userList) {
                    if (localU.getLoginName().equals(user.getLoginName()) &&
                            !localU.getPhone().equals(user.getPhone())) {
                        LOGGER.warn("会易通传递的用户名和本地的用户名相同, 但是手机号不同, 不允许插入. " +
                                        "会易通传递的用户名和手机号为: {}:::::{}, 本地的用户名和手机号为: {}:::::{}",
                                user.getName(), user.getPhone(), localU.getName(), localU.getPhone());
                        return dataReturn.returnError("添加失败: 用户名相同,手机号不同");
                    } else if (!localU.getLoginName().equals(user.getLoginName()) &&
                            localU.getPhone().equals(user.getPhone())) {//会易通不校验这种情况
                        LOGGER.warn("会易通传递的手机号和本地的手机号相同, 但是用户名不同, 不允许插入. " +
                                        "会易通传递的用户名和手机号为: {}:::::{}, 本地的用户名和手机号为: {}:::::{}",
                                user.getName(), user.getPhone(), localU.getName(), localU.getPhone());
                        user.setLoginName(localU.getLoginName());//以本地用户名为准
                    }
                    //其他情况均允许添加用户的操作
                }
            }
            //添加用户信息
            user.setSource(user.getPlatformId());
            user.setPermission("0,0,0,0,0,0,0,0,0");//为了兼容会管同步用户功能
            user.setUuid(StringUtil.get32UUID());//生成本地主键ID
        	if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
    			String md5pwd = user.getLoginPwd();
    			String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
    			user.setLoginPwd(loginPwd);
    			user.setMd5Pwd(md5pwd);
    		}
            this.userDao.insertUserWithoutUUID(user);//执行插入数据库
        }

        //添加用户与角色的关系: 通过平台查询默认角色，然后添加用户-角色关系
        this.addUserDefaultRoleRel(user.getUuid(), user.getPlatformId());
        return dataReturn.returnResult(WsConstants.OK, "添加用户成功");
    }

    /**
     * 修改用户：提供给会易通使用
     * @param user 用户信息
     * @return 返回信息
     */
    @Override
    public ReturnData editUserForHYT(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        //可以确定本次最多使用2个长度的Map，所以为了避免其自动扩容使用长度为3
        Map<String, Object> paramsMap = new HashMap<String, Object>(3);
        LOGGER.info("会易通传递的用户信息为: {}", JSONObject.toJSONString(user));

        paramsMap.clear();
//		paramsMap.put("loginName", user.getLoginName());
        paramsMap.put("phone", user.getPhone());
        UserVO dataUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户手机号查询用户
        if (dataUser != null) {
            //执行更新操作
            user.setUuid(dataUser.getUuid());
            //如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
            if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
            	String md5pwd = user.getLoginPwd();
            	String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
            	user.setLoginPwd(loginPwd);
            	user.setMd5Pwd(md5pwd);
            }
            int success = this.userDao.updateUser(user);
            if (success > 0) {//更新用户信息成功
                //添加用户与角色的关系: 通过平台查询默认角色，然后添加用户-角色关系
                this.addUserDefaultRoleRel(user.getUuid(), user.getPlatformId());
            }
            return dataReturn.returnResult(WsConstants.OK, "修改用户成功");
        } else {
            LOGGER.warn("没有该用户信息");
            return dataReturn.returnResult(WsConstants.ERROR, "没有用户信息");
        }
    }

    /**
     * 删除用户：提供给会易通使用
     * @param user 用户信息
     * @return 返回信息
     */
    @Override
    public ReturnData delUserForHYT(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        LOGGER.info("会易通传递的用户信息为: {}", JSONObject.toJSONString(user));
        if (StringUtils.isBlank(user.getPhone())) {
            user.setPhone(user.getLoginName());//会易通的用户名和手机号一样
        }
        paramsMap.clear();
        paramsMap.put("phone", user.getPhone());
        UserVO localUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户登录名查询用户
        if (localUser == null) {
            LOGGER.warn("本地没有该用户, 手机号为: {}", user.getPhone());
            return dataReturn.returnError("没有该用户");
        }
        //通过平台ID查询对应的角色，删除该用户对应的平台的角色
        return this.delUserRoleRelByPlatformId(localUser.getUuid(), user.getPlatformId());
    }

    /**
     * 添加用户：提供给一机一档使用
     * 由于业务逻辑，一机一档删除的用户并不会真正的删除，所以如果存在该用户(通过用户名和手机号联合查询表示该用户存在)则更新
     * @param user 用户信息
     * @return 返回信息
     */
    public ReturnData addUserForVSDC(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        Map<String, Object> paramsMap = new HashMap<String, Object>(2, 1);//确定大小,无需创建更大空间的Map
        LOGGER.info("一机一档传递的用户信息为: {}", JSONObject.toJSONString(user));

        /* 处理用户的所属区域是否是国标和所属区域名称问题(一机一档的区域名称可能不是国标) Start */
        if (!CommonConstrant.WHOLE_COUNTRY_REGION_ID.equals(StringUtil
                .getCompleteString(user.getAreaId(), 12))) {//全国的用户无需查询
            final TRegionb region = this.regionDao.selectRegionById(StringUtil
                    .getCompleteString(user.getAreaId(), 12));
            if (region != null) {//是国标的数据，将国标的区域名称设置到参数中
                user.setAreaName(region.getName());
            } else {//不是国标的数据，不允许插入
                LOGGER.error("用户的信息中的行政区域不是国标的数据, 具体信息请看上述日志");
                return dataReturn.returnError("所属区域非国标数据, 无法添加用户");
            }
        }
        /* 处理用户的所属区域是否是国标和所属区域名称问题(一机一档的区域名称可能不是国标) End */

//		paramsMap.put("loginName", user.getLoginName());
        paramsMap.put("phone", user.getPhone());
        UserVO localUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过手机号联合查询用户是否存在
        //如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
        if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
        	String md5pwd = user.getLoginPwd();
        	String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
        	user.setLoginPwd(loginPwd);
        	user.setMd5Pwd(md5pwd);
        }
        if (localUser != null) {//用户已经存在，更新用户
            LOGGER.info("数据库中原来用户的信息是: {}", JSONObject.toJSONString(localUser));
            user.setPermission(localUser.getPermission());//为了兼容会管同步用户功能
            user.setUuid(localUser.getUuid());//以本地数据库的UUID为准
            this.userDao.updateUser(user);//更新用户基本信息
        } else {//用户不存在, 添加用户
            //检查用户名是否重复 Start
            UserVO paramUser = new UserVO();
            paramUser.setLoginName(user.getLoginName());
            paramUser.setPhone(user.getPhone());
            List<UserVO> userList = this.userDao.selectUserByUniqueKeyWithOr(paramUser);//查询用户
            if (userList != null && userList.size() > 0) {
                for (UserVO localU : userList) {
                    if (localU.getLoginName().equals(user.getLoginName()) &&
                            !localU.getPhone().equals(user.getPhone())) {
                        LOGGER.warn("一机一档传递的用户名和本地的用户名相同, 但是手机号不同, 不允许插入. " +
                                        "一机一档传递的用户名和手机号为: {}:::::{}, 本地的用户名和手机号为: {}:::::{}",
                                user.getName(), user.getPhone(), localU.getName(), localU.getPhone());
                        return dataReturn.returnError("添加失败: 用户名相同,手机号不同");
                    } else if (!localU.getLoginName().equals(user.getLoginName()) &&
                            localU.getPhone().equals(user.getPhone())) {//一机一档不验证这种情况
                        LOGGER.warn("一机一档传递的手机号和本地的手机号相同, 但是用户名不同, 不允许插入. " +
                                        "一机一档传递的用户名和手机号为: {}:::::{}, 本地的用户名和手机号为: {}:::::{}",
                                user.getName(), user.getPhone(), localU.getName(), localU.getPhone());
                        user.setLoginName(localU.getLoginName());//以本地登录名为准
                    }
                    //其他情况均允许添加用户的操作
                }
            }
            //添加用户信息
            user.setSource(user.getPlatformId());
            user.setPermission("0,0,0,0,0,0,0,0,0");//为了兼容会管同步用户功能
            user.setUuid(StringUtil.get32UUID());//生成本地主键ID
            this.userDao.insertUserWithoutUUID(user);//执行插入数据库
        }

        //添加用户与角色的关系: 通过平台查询默认角色，然后添加用户-角色关系
        this.addUserDefaultRoleRel(user.getUuid(), user.getPlatformId());
        return dataReturn.returnResult(WsConstants.OK, "添加用户成功");
    }

    /**
     * 修改用户：提供给一机一档使用
     * @param user 用户信息
     * @return 返回信息
     */
    @Override
    public ReturnData editUserForVSDC(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        LOGGER.info("一机一档传递的用户信息为: {}", JSONObject.toJSONString(user));

        /* 处理用户的所属区域是否是国标和所属区域名称问题(一机一档的区域名称可能不是国标) Start */
        if(StringUtils.isNotBlank(user.getAreaId())) {//修改用户的话可能不修改所属区域
            if (!CommonConstrant.WHOLE_COUNTRY_REGION_ID.equals(StringUtil
                    .getCompleteString(user.getAreaId(), 12))) {//全国的用户无需查询
                final TRegionb region = this.regionDao.selectRegionById(StringUtil
                        .getCompleteString(user.getAreaId(), 12));
                if (region != null) {//是国标的数据，将国标的区域名称设置到参数中
                    user.setAreaName(region.getName());
                } else {//不是国标的数据，不允许插入
                    LOGGER.error("用户的信息中的行政区域不是国标的数据, 具体信息请看上述日志");
                    return dataReturn.returnError("所属区域非国标数据, 无法添加用户");
                }
            }
        }
        /* 处理用户的所属区域是否是国标和所属区域名称问题(一机一档的区域名称可能不是国标) End */

        paramsMap.clear();
//		paramsMap.put("loginName", user.getLoginName());
        paramsMap.put("phone", user.getPhone());
        UserVO dataUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户登录名和手机号查询用户
        if (dataUser != null) {
            //执行更新操作
            user.setUuid(dataUser.getUuid());
            //如果传递过来的密码是md5值，则需要对其加密并重新赋值给用户
            if(user.getLoginPwd().length() == 32){//按照密码长度判断加密方式 md5为32位  sm3是64位
            	String md5pwd = user.getLoginPwd();
            	String loginPwd = Sm3Utils.encrypt(user.getLoginName()+user.getLoginPwd());//最终的用户密码
            	user.setLoginPwd(loginPwd);
            	user.setMd5Pwd(md5pwd);
            }
            int success = this.userDao.updateUser(user);
            if (success > 0) {//更新用户信息成功
                //添加用户与角色的关系: 通过平台查询默认角色，然后添加用户-角色关系
                this.addUserDefaultRoleRel(user.getUuid(), user.getPlatformId());
            }
            return dataReturn.returnResult(WsConstants.OK, "修改用户成功");
        } else {
            LOGGER.warn("没有该用户信息");
            return dataReturn.returnResult(WsConstants.ERROR, "没有该用户信息");
        }
    }

    /**
     * 删除用户：提供给一机一档使用
     * @param user 用户信息
     * @return 返回信息
     */
    @Override
    public ReturnData delUserForVSDC(UserVO user) {
        BaseReturn dataReturn = new BaseReturn();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        LOGGER.info("一机一档传递的用户信息为: {}", JSONObject.toJSONString(user));
        if (StringUtils.isBlank(user.getPhone())) {
            user.setPhone(user.getLoginName());//一机一档的用户名和手机号一样
        }
        paramsMap.clear();
        paramsMap.put("phone", user.getPhone());
        UserVO localUser = this.userDao.selectUserByUniqueKey(paramsMap);//通过用户登录名查询用户
        if (localUser == null) {
            LOGGER.warn("本地没有该用户, 手机号为: {}", user.getPhone());
            return dataReturn.returnError("没有该用户");
        }

        //通过平台ID查询对应的角色，删除该用户对应的平台的角色
        return this.delUserRoleRelByPlatformId(localUser.getUuid(), user.getPlatformId());
    }

    /**
     * 添加用户-默认角色的关系
     * @param userId 用户ID
     * @param  platformId 平台ID
     * @return 添加成功的数量
     */
    private int addUserDefaultRoleRel(String userId, String platformId) {
        //可以确定本次最多使用2个长度的Map，所以为了避免其自动扩容使用长度为3
        Map<String, Object> paramsMap = new HashMap<String, Object>(3);
        /* 添加用户与角色的关系 Start */
        StringBuilder roleIdStringBuilder = new StringBuilder();
        //查询平台的默认角色ID Start
        TRoleVO role = this.roleDao.selectDefaultRoleWithPermissionIdsByPlatformId(platformId);
        if (role == null) {
            LOGGER.info("平台的默认角色为空，平台是: {}", platformId);
        } else {
            roleIdStringBuilder.append(role.getUuid()).append(",");//该平台默认角色的ID
        }
        //查询平台的默认角色ID End

        paramsMap.clear();
        paramsMap.put("userId", userId);
        paramsMap.put("roleIds", roleIdStringBuilder.toString().split(","));
        return this.userDao.insertUserRoleRel(paramsMap);
        /* 添加用户与角色的关系 End */
    }

    /**
     * 删除用于对应的平台的角色的关系
     * @param userId 用户ID
     * @param platformId 平台ID
     * @return 返回信息
     */
    private ReturnData delUserRoleRelByPlatformId(String userId, String platformId) {
        BaseReturn dataReturn = new BaseReturn();
        //可以确定本次最多使用2个长度的Map，所以为了避免其自动扩容使用长度为3
        Map<String, Object> paramsMap = new HashMap<String, Object>(3);
        /* 删除用户与对应平台的角色的关系 Start */
        List<String> roleIdList = new ArrayList<String>();//要删除的角色的ID的集合
        //查询平台的默认角色ID Start
        //默认角色每个平台最多只能有一个
        TRoleVO role = this.roleDao.selectDefaultRoleWithPermissionIdsByPlatformId(platformId);
        if (role == null) {
            LOGGER.info("删除失败,平台的默认角色为空，平台ID是: {}", platformId);
            return dataReturn.returnError("删除失败: 请联系管理员查看平台ID是否存在");
        }
        roleIdList.add(role.getUuid());
        //查询平台的默认角色ID End

        //删除用户对应的视联汇APP网管模块的权限
        paramsMap.clear();
        paramsMap.put(ROLE_IDS_LIST_STR, roleIdList);
        paramsMap.put(USER_ID_STR, userId);
        this.userDao.deleteUserRoleBatchByUserIdAndRoleIdList(paramsMap);
        /* 删除用户与对应平台的角色的关系 End */
        return dataReturn.returnResult(WsConstants.OK, "删除成功");
    }

    /**
     * 同步网管用户信息
     * @param token 访问令牌
     * @param otherPlatformId 平台ID
     * @param platformType 平台类别
     * @return 返回信息
     */
    @Override
    public ReturnData syncNetManagerUser(String token, String otherPlatformId, String platformType) {
        BaseReturn dataReturn = new BaseReturn();
        List<SyncUserResultEntity> otherDataList = new ArrayList<SyncUserResultEntity>();//被同步平台失败用户
        List<SyncUserResultEntity> localDataList = new ArrayList<SyncUserResultEntity>();//双方平台同步失败用户
        List<UserVO> updateUserList = new ArrayList<UserVO>();//需要更新的用户列表
        List<Map<String, Object>> userRoleList = new ArrayList<>();//需要插入的用户角色关系列表
        Map<String, Object> extraMap = new HashMap<>();//返回的数据
        int allUserSize = 0;//被同步平台的所有用户的数量

        //查询平台的默认角色ID Start
        TRoleVO role = this.roleDao.selectDefaultRoleWithPermissionIdsByPlatformId(otherPlatformId);
        if (role == null) {
            LOGGER.info("数据异常: 平台的默认角色为空，平台是: {}", otherPlatformId);
            return dataReturn.returnError("同步失败");
        }
        //查询平台的默认角色ID End
        //网管所有用户数量
        List<UserVO> otherAllUserList = this.getOtherAllUserListByPlatformId(otherPlatformId, platformType, token);
        if (otherAllUserList == null || otherAllUserList.size() == 0) {
            LOGGER.info("获取网管用户列表为空");
            return dataReturn.returnResult(WsConstants.OK, "获取网管用户列表为空");
        }

        /* 计算手机号重复的用户数据 Start */
        allUserSize = otherAllUserList.size();
        List<UserVO> samePhoneUserList = this.removeSamePhoneUserList(otherAllUserList);//相同手机号用户的用户列表
        if (samePhoneUserList != null && samePhoneUserList.size() > 0) {
            for (UserVO samePhoneUser : samePhoneUserList) {
                SyncUserResultEntity syncUserResult =
                        this.generateSyncUserResult(null, samePhoneUser.getLoginName(), samePhoneUser.getPhone(),
                                samePhoneUser.getCreateTime(), REASON_USER_SAME_PHONE);
                otherDataList.add(syncUserResult);//Excel表的数据:对方平台用户手机号相同的数据
            }
        }
        /* 计算手机号重复的用户数据 End */

        /* 计算手机号为空和网管平台与本平台用户不同(手机号相同用户名不同或者手机号不同用户名相同)的数据 Start */
        this.removeInvalidUser(otherAllUserList, otherDataList, localDataList,
                updateUserList, platformType, otherPlatformId);
        /* 计算手机号为空和网管平台与本平台用户不同(手机号相同用户名不同或者手机号不同用户名相同)的数据 End */


        //将用户的手机号相同List存放数据库，用于导出Excel
        this.jedisDao.setList(StringUtil.getRedisKey(CommonConstrant.PREFIX_SYNC_USER,
                GlobalConstants.SYNC_USER_NET_MANAGER_OTHER),otherDataList, TimeUtil.getSecondsByMinute(null));
        //将用户的手机号相同用户名不同或者用户名相同手机号不同的结果List存放数据库，用于导出Excel
        this.jedisDao.setList(StringUtil.getRedisKey(CommonConstrant.PREFIX_SYNC_USER,
                GlobalConstants.SYNC_USER_NET_MANAGER_LOCAL), localDataList, TimeUtil.getSecondsByMinute(null));

        /* 批量添加/更新用户信息和用户角色信息 Start */
        Map<String, Object> userRoleMap;//用户角色关系的Map

        //批量添加用户信息
        if (otherAllUserList != null && otherAllUserList.size() > 0) {
            this.userDao.insertUserBatch(otherAllUserList);
            for (UserVO insertUser : otherAllUserList) {//需要添加的用户
                userRoleMap = new HashMap<>();
                userRoleMap.put(USER_ID_STR, insertUser.getUuid());//用户ID
                userRoleMap.put(ROLE_ID_STR, role.getUuid());//默认角色ID
                userRoleList.add(userRoleMap);
            }
        }
        //批量更新用户信息
        if (updateUserList != null && updateUserList.size() > 0) {
            this.userDao.updateUserBatch(updateUserList);
            for (UserVO updateUser : updateUserList) {//需要更新的用户
                userRoleMap = new HashMap<>();
                userRoleMap.put(USER_ID_STR, updateUser.getUuid());//用户ID
                userRoleMap.put(ROLE_ID_STR, role.getUuid());//默认角色ID
                userRoleList.add(userRoleMap);
            }
        }
        //批量添加用户角色信息
        if (userRoleList != null && userRoleList.size() > 0) {
            this.userDao.insertUserRoleRelBatch(userRoleList);
        }
        /* 批量添加/更新用户信息和用户角色信息 End */

        //封装操作数量结果
        int successCount = otherAllUserList.size() + updateUserList.size();//同步成功数量
        extraMap.put(OTHER_DATA_LIST_KEY, otherDataList);//被同步平台失败用户的List
        extraMap.put(LOCAL_DATA_LIST_KEY, localDataList);//双方平台同步失败用户
        extraMap.put("userSyncCount", allUserSize);//用户同步的总数量
        extraMap.put("successUserSyncCount", successCount);//同步成功数量
        extraMap.put("errorUserSyncCount", (allUserSize - successCount));//同步失败数量
        extraMap.put(GlobalConstants.EXPORT_TYPE_KEY,
                GlobalConstants.EXPORT_TYPE_NET_MANAGER_SYSTEM);//导出网管平台的系统类型
        LOGGER.info("总条数: " + allUserSize + " 成功条数: " + successCount + " 失败条数: " + (otherDataList.size() + localDataList.size()));
        return dataReturn.returnResult(WsConstants.OK, "同步成功", null, null, extraMap);
    }

    /**
     * 同步一机一档用户信息
     * @param token 访问令牌
     * @param otherPlatformId 平台ID
     * @param platformType 平台类别
     * @return
     */
    public ReturnData syncVSDCUser(String token, String otherPlatformId, String platformType) {
        BaseReturn dataReturn = new BaseReturn();
        List<SyncUserResultEntity> otherDataList = new ArrayList<SyncUserResultEntity>();//被同步平台失败用户
        List<SyncUserResultEntity> localDataList = new ArrayList<SyncUserResultEntity>();//双方平台同步失败用户
        List<UserVO> updateUserList = new ArrayList<UserVO>();//需要更新的用户列表
        List<Map<String, Object>> userRoleList = new ArrayList<>();//需要插入的用户角色关系列表
        Map<String, Object> extraMap = new HashMap<>();//返回的数据
        int allUserSize = 0;//被同步平台的所有用户的数量

        //查询平台的默认角色ID Start
        TRoleVO role = this.roleDao.selectDefaultRoleWithPermissionIdsByPlatformId(otherPlatformId);
        if (role == null) {
            LOGGER.info("数据异常: 平台的默认角色为空，平台是: {}", otherPlatformId);
            return dataReturn.returnError("同步失败");
        }
        //查询平台的默认角色ID End
        //一机一档所有用户数量
        List<UserVO> otherAllUserList = this.getOtherAllUserListByPlatformId(otherPlatformId, platformType, token);
        if (otherAllUserList == null || otherAllUserList.size() == 0) {
            LOGGER.info("一机一档返回的数据为空");
            return dataReturn.returnResult(WsConstants.OK, "获取不到一机一档用户数据");
        }
        /* 计算手机号重复的用户数据 Start */
        allUserSize = otherAllUserList.size();
        List<UserVO> samePhoneUserList = this.removeSamePhoneUserList(otherAllUserList);//相同手机号用户的用户列表
        if (samePhoneUserList != null && samePhoneUserList.size() > 0) {
            for (UserVO samePhoneUser : samePhoneUserList) {
                SyncUserResultEntity syncUserResult =
                        this.generateSyncUserResult(null, samePhoneUser.getLoginName(),
                                samePhoneUser.getPhone(),  samePhoneUser.getCreateTime(), REASON_USER_SAME_PHONE);
                otherDataList.add(syncUserResult);//Excel表的数据:对方平台用户手机号相同的数据
            }
        }
        /* 计算手机号重复的用户数据 End */

        /* 计算去除无效的数据 Start */
        this.removeInvalidUser(otherAllUserList, otherDataList,
                localDataList, updateUserList, platformType, otherPlatformId);
        /* 计算去除无效的数据 End */

        //将用户的手机号相同List存放数据库，用于导出Excel
        this.jedisDao.setList(StringUtil.getRedisKey(CommonConstrant.PREFIX_SYNC_USER,
                GlobalConstants.SYNC_USER_VSDC_OTHER), otherDataList, TimeUtil.getSecondsByMinute(null));
        //将用户的手机号相同用户名不同或者用户名相同手机号不同的结果List存放数据库，用于导出Excel
        this.jedisDao.setList(StringUtil.getRedisKey(CommonConstrant.PREFIX_SYNC_USER,
                GlobalConstants.SYNC_USER_VSDC_LOCAL), localDataList, TimeUtil.getSecondsByMinute(null));

        /* 批量添加/更新用户信息和用户角色信息 Start */
        Map<String, Object> userRoleMap;//用户角色关系Map

        //批量添加用户信息
        if (otherAllUserList != null && otherAllUserList.size() > 0) {
            this.userDao.insertUserBatch(otherAllUserList);
            for (UserVO insertUser : otherAllUserList) {//需要添加的用户
            	if(StringUtils.isBlank(insertUser.getAreaName()) || insertUser.getAreaName().equals("无")){
            		insertUser.setAreaName("全国");
            	}
                userRoleMap = new HashMap<>();
                userRoleMap.put(USER_ID_STR, insertUser.getUuid());//用户ID
                userRoleMap.put(ROLE_ID_STR, role.getUuid());//默认角色ID
                userRoleList.add(userRoleMap);
            }
        }
        //批量更新用户信息
        if (updateUserList != null && updateUserList.size() > 0) {
            this.userDao.updateUserBatch(updateUserList);
            for (UserVO updateUser : updateUserList) {//需要更新的用户
                userRoleMap = new HashMap<>();
                userRoleMap.put(USER_ID_STR, updateUser.getUuid());//用户ID
                userRoleMap.put(ROLE_ID_STR, role.getUuid());//默认角色ID
                userRoleList.add(userRoleMap);
            }
        }

        //批量添加用户角色信息
        if (userRoleList != null && userRoleList.size() > 0) {
            this.userDao.insertUserRoleRelBatch(userRoleList);
        }
        /* 批量添加/更新用户信息和用户角色信息 End */

        //封装操作数量结果
        int successCount = otherAllUserList.size() + updateUserList.size();//同步成功数量
        extraMap.put(OTHER_DATA_LIST_KEY, otherDataList);//被同步平台失败用户的List
        extraMap.put(LOCAL_DATA_LIST_KEY, localDataList);//双方平台同步失败用户
        extraMap.put("userSyncCount", allUserSize);//用户同步的总数量
        extraMap.put("successUserSyncCount", successCount);//同步成功数量
        extraMap.put("errorUserSyncCount", (allUserSize - successCount));//同步失败数量
        extraMap.put(GlobalConstants.EXPORT_TYPE_KEY, GlobalConstants.EXPORT_TYPE_VSDC_SYSTEM);//导出一机一档平台的系统类型
        return dataReturn.returnResult(WsConstants.OK, "同步成功", null, null, extraMap);
    }

    /**
     * 同步会易通用户信息
     * @param token 访问令牌
     * @param otherPlatformId 平台ID
     * @param platformType 平台类别
     * @return
     */
    @Override
    public ReturnData syncHYTUser(String token, String otherPlatformId, String platformType) {
        BaseReturn dataReturn = new BaseReturn();
        List<SyncUserResultEntity> otherDataList = new ArrayList<SyncUserResultEntity>();//被同步平台失败用户
        List<SyncUserResultEntity> localDataList = new ArrayList<SyncUserResultEntity>();//双方平台同步失败用户
        List<UserVO> updateUserList = new ArrayList<UserVO>();//需要更新的用户列表
        List<Map<String, Object>> userRoleList = new ArrayList<>();//需要插入的用户角色关系列表
        Map<String, Object> extraMap = new HashMap<>();//返回的数据
        int allUserSize = 0;//被同步平台的所有用户的数量

        //查询平台的默认角色ID Start
        TRoleVO role = this.roleDao.selectDefaultRoleWithPermissionIdsByPlatformId(otherPlatformId);
        if (role == null) {
            LOGGER.info("数据异常: 平台的默认角色为空，平台是: {}", otherPlatformId);
            return dataReturn.returnError("同步失败");
        }
        //查询平台的默认角色ID End

        List<UserVO> otherAllUserList = this.getOtherAllUserListByPlatformId(otherPlatformId,
                platformType, token);//会易通所有用户数量
        if (otherAllUserList == null || otherAllUserList.size() == 0) {
            LOGGER.info("会易通返回的数据为空");
            return dataReturn.returnResult(WsConstants.OK, "获取不到会易通用户数据");
        }
        /* 计算手机号重复的用户数据 Start */
        allUserSize = otherAllUserList.size();
        List<UserVO> samePhoneUserList = this.removeSamePhoneUserList(otherAllUserList);//相同手机号用户的用户列表
        if (samePhoneUserList != null && samePhoneUserList.size() > 0) {
            for (UserVO samePhoneUser : samePhoneUserList) {
                SyncUserResultEntity syncUserResult =
                        this.generateSyncUserResult(null, samePhoneUser.getLoginName(),
                                samePhoneUser.getPhone(), samePhoneUser.getCreateTime(), REASON_USER_SAME_PHONE);
                otherDataList.add(syncUserResult);//Excel表的数据:对方平台用户手机号相同的数据
            }
        }
        /* 计算手机号重复的用户数据 End */

        /* 计算手机号为空和会易通平台与本平台用户不同(手机号相同用户名不同或者手机号不同用户名相同)的数据 Start */
        this.removeInvalidUser(otherAllUserList, otherDataList,
                localDataList, updateUserList, platformType, otherPlatformId);
        /* 计算手机号为空和会易通平台与本平台用户不同(手机号相同用户名不同或者手机号不同用户名相同)的数据 End */

        //将用户的手机号相同List存放数据库，用于导出Excel
        this.jedisDao.setList(StringUtil.getRedisKey(CommonConstrant.PREFIX_SYNC_USER,
                GlobalConstants.SYNC_USER_HUIYITONG_OTHER), otherDataList, TimeUtil.getSecondsByMinute(null));
        //将用户的手机号相同用户名不同或者用户名相同手机号不同的结果List存放数据库，用于导出Excel
        this.jedisDao.setList(StringUtil.getRedisKey(CommonConstrant.PREFIX_SYNC_USER,
                GlobalConstants.SYNC_USER_HUIYITONG_LOCAL), localDataList, TimeUtil.getSecondsByMinute(null));

        /* 批量添加/更新用户信息和用户角色信息 Start */
        Map<String, Object> userRoleMap;//用户角色关系Map

        //批量添加用户信息
        if (otherAllUserList != null && otherAllUserList.size() > 0) {
            this.userDao.insertUserBatch(otherAllUserList);
            for (UserVO insertUser : otherAllUserList) {//需要添加的用户
                userRoleMap = new HashMap<>();
                userRoleMap.put(USER_ID_STR, insertUser.getUuid());//用户ID
                userRoleMap.put(ROLE_ID_STR, role.getUuid());//默认角色ID
                userRoleList.add(userRoleMap);
            }
        }
        //批量更新用户信息
        if (updateUserList != null && updateUserList.size() > 0) {
            this.userDao.updateUserBatch(updateUserList);
            for (UserVO updateUser : updateUserList) {//需要更新的用户
                userRoleMap = new HashMap<>();
                userRoleMap.put(USER_ID_STR, updateUser.getUuid());//用户ID
                userRoleMap.put(ROLE_ID_STR, role.getUuid());//默认角色ID
                userRoleList.add(userRoleMap);
            }
        }

        //批量添加用户角色信息
        if (userRoleList != null && userRoleList.size() > 0) {
            this.userDao.insertUserRoleRelBatch(userRoleList);
        }
        /* 批量添加/更新用户信息和用户角色信息 End */

        //封装操作数量结果
        int successCount = otherAllUserList.size() + updateUserList.size();//同步成功数量
        extraMap.put(OTHER_DATA_LIST_KEY, otherDataList);//被同步平台失败用户的List
        extraMap.put(LOCAL_DATA_LIST_KEY, localDataList);//双方平台同步失败用户
        extraMap.put("userSyncCount", allUserSize);//用户同步的总数量
        extraMap.put("successUserSyncCount", successCount);//同步成功数量
        extraMap.put("errorUserSyncCount", (allUserSize - successCount));//同步失败数量
        extraMap.put(GlobalConstants.EXPORT_TYPE_KEY,
                GlobalConstants.EXPORT_TYPE_HUIYITONG_SYSTEM);//导出会易通平台的系统类型
        return dataReturn.returnResult(WsConstants.OK, "同步成功", null, null, extraMap);
    }


    /**
     * 获取其他平台的用户信息。去除手机号重复
     * @param platformId 平台ID
     * @param platformType 平台类别。8表示网管、9表示会易通、10表示一机一档
     * @param localToken 本平台的token
     * @return
     */
    private List<UserVO> getOtherAllUserListByPlatformId(String platformId, String platformType, String localToken) {
        List<UserVO> allUserList = new ArrayList<UserVO>();//对应平台的所有用户List
        Map<String, Object> paramsMap = new HashMap<String, Object>();//接口的参数
        String token = "";

        final ServerConfig serverConfig = this.serverConfigService.getServerConfigByOtherPlatformId(platformId);
        if (serverConfig == null) {
            throw new BusinessException("请先配置平台信息");
        }

        String serverUrl = serverConfig.getUrl();//获取IP和端口号:http://ip:port
        String url = "";
        if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(platformType)) {//网管平台的URL
            url = serverUrl + WsConstants.SYNC_USER_FOR_NET_MANAGER_URL;//获取网管平台所有用户的URL
            paramsMap.clear();
            paramsMap.put("token", this.pushUserService.getOtherPlatformToken(platformId, localToken));//访问token
            paramsMap.put("pathparam", serverConfig.getLoginName());//网管用户ID
            paramsMap.put("pageNum", PAGE_NUM_PARAMS);//页码
            paramsMap.put("pageSize", PAGE_SIZE_PARAMS);//页大小
            allUserList = this.getAllNetManagerUserList(url, paramsMap, platformId,
                    localToken, this.DEFAULT_RETURN_COUNT);//网管所有的用户(循环调用获取所有的用户)
        } else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE.equals(platformType)) {//会易通平台的URL
            url = serverUrl + WsConstants.SYNC_USER_FOR_HYT_URL;//获取会易通所有用户的URL
            Map<String, String> hytParasmMap = new HashMap<>();//会易通的接口必须是String、String的
            hytParasmMap.put("hytToken", this.pushUserService.getOtherPlatformToken(platformId));//会易通鉴权token
            hytParasmMap.put("pageNo", String.valueOf(PAGE_NUM_PARAMS));//会易通页码
            hytParasmMap.put("pageSize", String.valueOf(PAGE_SIZE_PARAMS));//会易通页大小
            allUserList = this.getAllHYTUserList(url, hytParasmMap, platformId, this.DEFAULT_RETURN_COUNT);
        } else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE.equals(platformType)) {//一机一档平台的URL
            paramsMap.clear();
            url = serverUrl  + WsConstants.SYNC_USER_FOR_VSDC_URL;//获取一机一档所有用户的URL
            paramsMap.put("token", this.pushUserService.getOtherPlatformToken(platformId));//访问token
            paramsMap.put("pageNum", PAGE_NUM_PARAMS);//页码
            paramsMap.put("pageSize", PAGE_SIZE_PARAMS);//页大小
            allUserList = this.getAllVSDCUserList(url, paramsMap, platformId, this.DEFAULT_RETURN_COUNT);
        } else {
            LOGGER.error("用户平台类别不是网管、会易通、一机一档, 平台类别是: {}", platformType);
            throw new BusinessException("平台类别输入错误");
        }
        return allUserList;
    }

    /**
     * 获取网管所有的用户
     * @param url 网管URL
     * @param uriVariablesMap URL调用的参数
     * @param platformId 平台ID
     * @param localToken 本地token，调用网管接口需要使用
     * @param returnCount 当第一次调用获取用户数据时，TOEKN过期，需要递归调用。该值表示如果出现<=0的情况则退出整个方法
     * @return 所有的网管用户List
     */
    private List<UserVO> getAllNetManagerUserList(String url, Map<String, Object> uriVariablesMap,
                                                  String platformId, String localToken, int returnCount) {
        List<UserVO> allUserList = new ArrayList<UserVO>();//对应平台的所有用户List
        JSONObject resultJSONObject = new JSONObject();
        JSONArray allUserJSONArray = new JSONArray();//用户列表
        LOGGER.info("开始调用网管获取用户的接口, 接口URL是: {}, URL参数是: {}", url, JSONObject.toJSONString(uriVariablesMap));
        try {
            //获取网管用户的信息(第一页数据)
            resultJSONObject = RestTemplateUtil.getForObject(url, JSONObject.class, uriVariablesMap);
        } catch (Exception e) {
            throw new BusinessException("无法连接网管服务器", e);//Controller会有日志
        }

        /* 网管返回为空表示失败 Start */
        if (resultJSONObject == null || resultJSONObject.size() == 0) {
            throw new BusinessException("网管返回数据为空");
        }
        Integer ret = resultJSONObject.getInteger("ret");//返回结果，为0表示成功，为1表示失败
        if (ret == null) {//返回失败
            LOGGER.error("从网管获取用户信息失败, 网管返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("获取失败,网管返回信息: " + resultJSONObject.getString("msg"));
        }
        if (ret.equals(1)) {//网管的1表示token过期，需要重新登录获取token
            LOGGER.info("网管的TOKEN失效, 递归调用, 调用的标识数为: {}", returnCount);
            this.pushUserService.delOtherPlatformToken(platformId);//先删除本地缓存中存在的token
            //重新获取token
            uriVariablesMap.put("token", this.pushUserService.getOtherPlatformToken(platformId, localToken));
            if (returnCount > 0) {
                //递归调用
                return this.getAllNetManagerUserList(url, uriVariablesMap, platformId, localToken, --returnCount);
            } else {
                LOGGER.error("多次递归调用, 网管依然是TOKEN失效, 请检查网管系统的运行情况。");
                return null;
            }
        }
        if (!ret.equals(0)) {//返回失败。网管不为0表示失败
            LOGGER.error("从网管获取用户信息失败, 网管返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("获取失败,网管返回信息: " + resultJSONObject.getString("msg"));
        }
        if (resultJSONObject.getJSONObject("data") == null || resultJSONObject.getJSONObject("data").size() == 0) {
            LOGGER.error("从网管获取用户信息的data为空, 网管返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("网管返回数据为空");
        }
        if(resultJSONObject.getJSONObject("data").getJSONObject("extra") == null ||
                resultJSONObject.getJSONObject("data").getJSONObject("extra").size() == 0) {
            LOGGER.error("从网管获取用户信息的data.extra为空, 网管返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("网管返回数据为空");
        }
        if(resultJSONObject.getJSONObject("data").getJSONArray("items") == null ||
                resultJSONObject.getJSONObject("data").getJSONArray("items").size() == 0) {
            LOGGER.error("从网管获取用户信息的data.items为空, 网管返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("网管返回数据为空");
        }
        /* 网管返回为空表示失败 End */

        //总页数
        Integer totalPage = resultJSONObject.getJSONObject("data").getJSONObject("extra").getInteger("pages");
        if (totalPage == null) {
            LOGGER.error("从网管获取用户信息失败, 网管返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("网管返回数据为空");
        }

        //第一页数据
        allUserJSONArray.addAll(resultJSONObject.getJSONObject("data").getJSONArray("items"));

        /* 批量获取网管用户信息 Start */
        int loopCount = 0;//程序中if (ret.equals(1))存在永久循环(从对方平台获取token立马就过期),该标志表示如果出现超过3次就跳出循环
        for (int i = 2; i <= totalPage; i++) {//从第二页开始循环调用, 获取网管所有的用户信息
            try {
                uriVariablesMap.put("pageNum", i);//页码
                resultJSONObject.clear();//清空之前的数据
                //获取网管用户的所有信息(第i页数据)
                resultJSONObject = RestTemplateUtil.getForObject(url, JSONObject.class, uriVariablesMap);
            } catch (Exception e) {
                throw new BusinessException("无法连接网管服务器", e);//Controller会有日志
            }
            if (resultJSONObject == null || resultJSONObject.size() == 0) {//调用其他页用户数据
                LOGGER.warn("第 {} 页网管返回数据为空, 网管的返回信息是: {}", i, resultJSONObject.toJSONString());
                continue;//调用下一页
            }

            ret = resultJSONObject.getInteger("ret");//返回结果，为0表示成功，为1表示失败


            /* 网管返回数据为空或token失效的处理: 继续调用下一页 Start */
            if (ret == null) {//返回失败
                LOGGER.warn("获取网管第 {} 页用户信息失败, 网管的返回信息是: {}", i, resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (ret.equals(1)) {//网管的1表示token过期，需要重新登录获取token
                LOGGER.info("网管的TOKEN失效, 循环调用, 调用的标识数为: {}", loopCount);
                this.pushUserService.delOtherPlatformToken(platformId);//先删除本地缓存中存在的token
                //重新获取token
                uriVariablesMap.put("token", this.pushUserService.getOtherPlatformToken(platformId, localToken));
                i--;//token，需要重新登录，再次获取相同页的数据
                if ((++loopCount) > DEFAULT_RETURN_COUNT) {//出现3次获取token后立马就过期的现象跳出循环
                    LOGGER.error("多次循环调用, 网管依然是TOKEN失效, 请检查网管系统的运行情况。");
                    break;
                }
                continue;
            }
            if (!ret.equals(0)) {//返回失败
                LOGGER.warn("获取网管第 {} 页用户信息失败, 网管的返回信息是: {}", i, resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (resultJSONObject.getJSONObject("data") == null || resultJSONObject.getJSONObject("data").size() == 0) {
                LOGGER.warn("获取网管第 {} 页用户信息的data数据为空, 网管返回的信息是: {}", i, resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (resultJSONObject.getJSONObject("data").getJSONArray("items") == null ||
                    resultJSONObject.getJSONObject("data").getJSONArray("items").size() == 0) {
                LOGGER.warn("获取网管第 {} 页用户信息的data.items信息为空, 网管返回的信息是: {}", i,
                        resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            /* 网管返回数据为空或token失效的处理: 继续调用下一页 End */
            JSONArray userJSONArray = resultJSONObject.getJSONObject("data").getJSONArray("items");//用户列表
            allUserJSONArray.addAll(userJSONArray);
        }
        /* 批量获取网管用户信息 End */

        if (allUserJSONArray == null || allUserJSONArray.size() == 0) {
            throw new BusinessException("获取网管用户列表为空");
        }

        /* 将网管的用户信息转换成本地的用户User信息 Start */
        for (int i = 0; i < allUserJSONArray.size(); i++) {
            JSONObject userJSON = allUserJSONArray.getJSONObject(i);
            UserVO user = this.convertUserByJSONObject(userJSON, PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE);
            allUserList.add(user);
        }
        /* 将网管的用户信息转换成本地的用户User信息 End */
        LOGGER.info("结束调用网管获取用户的接口, 获取网管用户数据成功");
        return allUserList;
    }

    /**
     * 获取一机一档所有的用户
     * @param url 一机一档URL
     * @param uriVariablesMap URL调用的参数
     * @param platformId 对应平台的平台ID
     * @param returnCount 当第一次调用一机一档获取用户数据时，一机一档TOEKN过期，需要递归调用。该值表示如果出现<=0的情况则退出整个方法
     * @return 所有的网管用户List
     */
    private List<UserVO> getAllVSDCUserList(String url, Map<String, Object> uriVariablesMap,
                                            String platformId, int returnCount) {
        List<UserVO> allUserList = new ArrayList<UserVO>();//对应平台的所有用户List
        JSONObject resultJSONObject = new JSONObject();
        JSONArray allUserJSONArray = new JSONArray();//用户列表
        LOGGER.info("开始调用一机一档获取用户的接口, 接口的URL是: {}, URL参数是: {}", url,
                JSONObject.toJSONString(uriVariablesMap));
        try {
            //获取一机一档用户的信息(第一页数据)
            resultJSONObject = RestTemplateUtil.postForObjectByForm(url, JSONObject.class, uriVariablesMap);
        } catch (Exception e) {
            throw new BusinessException("无法连接一机一档服务器", e);//Controller会有日志
        }

        /* 一机一档返回为空表示失败 Start */
        if (resultJSONObject == null || resultJSONObject.size() == 0) {
            throw new BusinessException("一机一档返回数据为空");
        }
        Integer ret = resultJSONObject.getInteger("result");//返回结果，为0表示成功，其余表示失败
        if (ret == null) {
            LOGGER.error("从一机一档获取用户信息失败, 一机一档返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("获取失败,一机一档返回信息: " + resultJSONObject.getString("msg"));
        }
        if (ret.equals(4)) {//一机一档的4表示token过期，需要重新登录获取token
            LOGGER.info("一机一档的TOKEN失效, 递归调用, 调用的标识数为: {}", returnCount);
            this.pushUserService.delOtherPlatformToken(platformId);//先删除本地缓存中存在的token
            uriVariablesMap.put("token", this.pushUserService.getOtherPlatformToken(platformId));//重新获取token
            if (returnCount > 0) {
                //递归调用
                return this.getAllVSDCUserList(url, uriVariablesMap, platformId, --returnCount);
            } else {
                LOGGER.error("多次递归调用, 一机一档依然是TOKEN失效, 请检查一机一档系统的运行情况。");
                return null;
            }
        }
        if (!ret.equals(0)) {//返回失败。一机一档不为0表示失败
            LOGGER.error("从一机一档获取用户信息失败, 一机一档返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("获取失败,一机一档返回信息: " + resultJSONObject.getString("msg"));
        }
        if (resultJSONObject.getJSONObject("data") == null || resultJSONObject.getJSONObject("data").size() == 0) {
            LOGGER.error("从一机一档获取用户信息的data为空, 一机一档返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("一机一档返回数据为空");
        }
        if(resultJSONObject.getJSONObject("data").getJSONObject("extra") == null ||
                resultJSONObject.getJSONObject("data").getJSONObject("extra").size() == 0) {
            LOGGER.error("从一机一档获取用户信息的data.extra为空, 一机一档返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("一机一档返回数据为空");
        }
        if(resultJSONObject.getJSONObject("data").getJSONArray("items") == null ||
                resultJSONObject.getJSONObject("data").getJSONArray("items").size() == 0) {
            LOGGER.error("从一机一档获取用户信息的data.items为空, 一机一档返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("一机一档返回数据为空");
        }
        /* 一机一档返回为空表示失败 End */

        //总页数
        Integer totalPage = resultJSONObject.getJSONObject("data").getJSONObject("extra").getInteger("totalPage");
        if (totalPage == null) {
            LOGGER.error("从一机一档获取用户信息失败, 一机一档返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("一机一档返回数据为空");
        }

        //第一页数据
        allUserJSONArray.addAll(resultJSONObject.getJSONObject("data").getJSONArray("items"));

        /* 批量获取一机一档用户信息 Start */
        int loopCount = 0;//程序中if (ret.equals(4))存在永久循环(从对方平台获取token立马就过期),该标志表示如果出现超过3次就跳出循环
        for (int i = 2; i <= totalPage; i++) {//从第二页开始循环调用, 获取一机一档所有的用户信息
            try {
                uriVariablesMap.put("pageNum", i);//改变页码,多次调用
                resultJSONObject.clear();//清空之前的数据
                //获取一机一档用户的信息(第i页数据)
                resultJSONObject = RestTemplateUtil.postForObjectByForm(url, JSONObject.class, uriVariablesMap);
            } catch (Exception e) {
                throw new BusinessException("无法连接一机一档服务器", e);//Controller会有日志
            }
            if (resultJSONObject == null || resultJSONObject.size() == 0) {//调用其他页用户数据
                LOGGER.warn("第 {} 页一机一档返回数据为空, 一机一档的返回信息是: {}", i, resultJSONObject.toJSONString());
                continue;//调用下一页
            }

            ret = resultJSONObject.getInteger("result");//返回结果，为0表示成功，其余表示失败
            /* 一机一档返回数据为空的处理: 继续调用下一页 Start */
            if (ret == null) {
                LOGGER.warn("获取一机一档第 {} 页用户信息失败, 一机一档的返回信息是: {}", i, resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (ret.equals(4)) {//一机一档的4表示token过期，需要重新登录获取token
                LOGGER.info("一机一档的TOKEN失效, 循环调用, 调用的标识数为: {}", loopCount);
                this.pushUserService.delOtherPlatformToken(platformId);//先删除本地缓存中存在的token
                uriVariablesMap.put("token", this.pushUserService.getOtherPlatformToken(platformId));//重新获取token
                i--;//token，需要重新登录，再次获取相同页的数据
                if ((++loopCount) > DEFAULT_RETURN_COUNT) {//出现3次获取token后立马就过期的现象跳出循环
                    LOGGER.error("多次循环调用, 一机一档依然是TOKEN失效, 请检查一机一档系统的运行情况。");
                    break;
                }
                continue;
            }
            if (!ret.equals(0)) {//返回失败。一机一档不为0表示失败
                LOGGER.error("从一机一档获取用户信息失败, 一机一档返回的数据为: {}", resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (resultJSONObject.getJSONObject("data") == null || resultJSONObject.getJSONObject("data").size() == 0) {
                LOGGER.warn("获取一机一档第 {} 页用户信息的data数据为空, 一机一档返回的信息是: {}", i,
                        resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (resultJSONObject.getJSONObject("data").getJSONArray("items") == null ||
                    resultJSONObject.getJSONObject("data").getJSONArray("items").size() == 0) {
                LOGGER.warn("获取一机一档第 {} 页用户信息的data.items信息为空, 一机一档返回的信息是: {}", i,
                        resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            /* 一机一档返回数据为空的处理: 继续调用下一页 End */
            JSONArray userJSONArray = resultJSONObject.getJSONObject("data").getJSONArray("items");//用户列表
            allUserJSONArray.addAll(userJSONArray);
        }
        /* 批量获取一机一档用户信息 End */

        if (allUserJSONArray == null || allUserJSONArray.size() == 0) {
            throw new BusinessException("获取一机一档用户列表为空");
        }

        /* 将一机一档的用户信息转换成本地的用户User信息 Start */
        for (int i = 0; i < allUserJSONArray.size(); i++) {
            JSONObject userJSON = allUserJSONArray.getJSONObject(i);
            UserVO user = this.convertUserByJSONObject(userJSON, PlatformTypeConstrant.VSDC_PLATFORM_TYPE);
            allUserList.add(user);
        }
        /* 将一机一档的用户信息转换成本地的用户User信息 End */
        LOGGER.info("结束调用一机一档获取用户的接口, 获取用户数据成功");
        return allUserList;
    }

    /**
     * 获取一机一档所有的用户
     * @param url 一机一档URL
     * @param paramsMap BODY的参数
     * @param platformId 对应平台的平台ID
     * @param returnCount 当第一次调用一机一档获取用户数据时，一机一档TOEKN过期，需要递归调用。该值表示如果出现<=0的情况则退出整个方法
     * @return 所有的网管用户List
     */
    private List<UserVO> getAllHYTUserList(String url, Map<String, String> paramsMap,
                                           String platformId, int returnCount) {
        List<UserVO> allUserList = new ArrayList<UserVO>();//对应平台的所有用户List
        JSONObject resultJSONObject = new JSONObject();
        JSONArray allUserJSONArray = new JSONArray();//用户列表
        LOGGER.info("开始调用会易通获取用户的接口, 接口的URL是: {}, BODY参数是: {}", url, JSONObject.toJSONString(paramsMap));
        try {
            //获取会易通用户的信息(第一页数据)
            resultJSONObject = RestTemplateUtil.postForObjectByForm(url, paramsMap, JSONObject.class);
        } catch (Exception e) {
            throw new BusinessException("无法连接会易通服务器", e);//Controller会有日志
        }

        /* 会易通返回为空表示失败 Start */
        if (resultJSONObject == null || resultJSONObject.size() == 0) {
            throw new BusinessException("会易通返回数据为空");
        }
        Integer ret = resultJSONObject.getInteger("code");//返回结果，为0表示成功，其余表示失败
        if (ret == null) {
            LOGGER.error("从会易通获取用户信息失败, 会易通返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("获取失败,会易通返回信息: " + resultJSONObject.getString("msg"));
        }
        if (ret.equals(9)) {//会易通的9表示token过期，需要重新登录获取token
            LOGGER.info("会易通的TOKEN失效, 递归调用, 调用的标识数为: {}", returnCount);
            this.pushUserService.delOtherPlatformToken(platformId);//先删除本地缓存中存在的token
            paramsMap.put("hytToken", this.pushUserService.getOtherPlatformToken(platformId));//重新获取token
            if (returnCount > 0) {
                //递归调用
                return this.getAllHYTUserList(url, paramsMap, platformId, --returnCount);
            } else {
                LOGGER.error("多次递归调用, 会易通依然是TOKEN失效, 请检查会易通系统的运行情况。");
                return null;
            }
        }
        if (!ret.equals(0)) {//返回失败。会易通不为0表示失败
            LOGGER.error("从会易通获取用户信息失败, 会易通返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("获取失败,会易通返回信息: " + resultJSONObject.getString("msg"));
        }
        if (resultJSONObject.getJSONObject("data") == null || resultJSONObject.getJSONObject("data").size() == 0) {
            LOGGER.error("从会易通获取用户信息的data为空, 会易通返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("会易通返回数据为空");
        }
        if(resultJSONObject.getJSONObject("data").getJSONObject("extra") == null ||
                resultJSONObject.getJSONObject("data").getJSONObject("extra").size() == 0) {
            LOGGER.error("从会易通获取用户信息的data.extra为空, 会易通返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("会易通返回数据为空");
        }
        if(resultJSONObject.getJSONObject("data").getJSONArray("itmes") == null ||
                resultJSONObject.getJSONObject("data").getJSONArray("itmes").size() == 0) {
            LOGGER.error("从会易通获取用户信息的data.itmes为空, 会易通返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("会易通返回数据为空");
        }
        /* 会易通返回为空表示失败 End */

        Integer totalPage = resultJSONObject.getJSONObject("data").getJSONObject("extra")
                .getInteger("totalPage");//总页数
        if (totalPage == null) {
            LOGGER.error("从会易通获取用户信息失败, 会易通返回的数据为: {}", resultJSONObject.toJSONString());
            throw new BusinessException("会易通返回数据为空");
        }

        //第一页数据
        allUserJSONArray.addAll(resultJSONObject.getJSONObject("data").getJSONArray("itmes"));

        /* 批量获取会易通用户信息 Start */
        int loopCount = 0;//程序中if (ret.equals(9))存在永久循环(从对方平台获取token立马就过期),该标志表示如果出现超过3次就跳出循环
        for (int i = 2; i <= totalPage; i++) {//从第二页开始循环调用, 获取会易通所有的用户信息
            try {
                paramsMap.put("pageNo", String.valueOf(i));//改变页码,多次调用
                resultJSONObject.clear();//清空之前的数据
                //获取一机一档用户的信息(第i页数据)
                resultJSONObject = RestTemplateUtil.postForObjectByForm(url, paramsMap, JSONObject.class);
            } catch (Exception e) {
                throw new BusinessException("无法连接会易通服务器", e);//Controller会有日志
            }
            if (resultJSONObject == null || resultJSONObject.size() == 0) {//调用其他页用户数据
                LOGGER.warn("第 {} 页会易通返回数据为空, 会易通的返回信息是: {}", i, resultJSONObject.toJSONString());
                continue;//调用下一页
            }

            ret = resultJSONObject.getInteger("code");//返回结果，为0表示成功，其余表示失败
            /* 会易通返回数据为空的处理: 继续调用下一页 Start */
            if (ret == null) {
                LOGGER.warn("获取会易通第 {} 页用户信息失败, 会易通的返回信息是: {}", i, resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (ret.equals(9)) {//会易通的9表示token过期，需要重新登录获取token
                LOGGER.info("会易通的TOKEN失效, 循环调用, 调用的标识数为: {}", loopCount);
                this.pushUserService.delOtherPlatformToken(platformId);//先删除本地缓存中存在的token
                paramsMap.put("hytToken", this.pushUserService.getOtherPlatformToken(platformId));//重新获取token
                i--;//token，需要重新登录，再次获取相同页的数据
                if ((++loopCount) > DEFAULT_RETURN_COUNT) {//出现3次：获取token后立马就过期的现象跳出循环
                    LOGGER.error("多次循环调用, 会易通依然是TOKEN失效, 请检查会易通系统的运行情况。");
                    break;
                }
                continue;
            }
            if (!ret.equals(0)) {//返回失败。会易通不为0表示失败
                LOGGER.error("从会易通获取用户信息失败, 会易通返回的数据为: {}", resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (resultJSONObject.getJSONObject("data") == null || resultJSONObject.getJSONObject("data").size() == 0) {
                LOGGER.warn("获取会易通第 {} 页用户信息的data数据为空, 会易通返回的信息是: {}", i,
                        resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            if (resultJSONObject.getJSONObject("data").getJSONArray("itmes") == null ||
                    resultJSONObject.getJSONObject("data").getJSONArray("itmes").size() == 0) {
                LOGGER.warn("获取会易通第 {} 页用户信息的data.items信息为空, 会易通返回的信息是: {}", i,
                        resultJSONObject.toJSONString());
                continue;//调用下一页
            }
            /* 会易通返回数据为空的处理: 继续调用下一页 End */
            JSONArray userJSONArray = resultJSONObject.getJSONObject("data").getJSONArray("itmes");//用户列表
            allUserJSONArray.addAll(userJSONArray);
        }
        /* 批量获取会易通用户信息 End */

        if (allUserJSONArray == null || allUserJSONArray.size() == 0) {
            throw new BusinessException("获取会易通用户列表为空");
        }

        /* 将会易通的用户信息转换成本地的用户User信息 Start */
        for (int i = 0; i < allUserJSONArray.size(); i++) {
            JSONObject userJSON = allUserJSONArray.getJSONObject(i);
            UserVO user = this.convertUserByJSONObject(userJSON, PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE);
            allUserList.add(user);
        }
        /* 将会易通的用户信息转换成本地的用户User信息 End */
        LOGGER.info("结束调用一机一档获取用户的接口, 获取用户数据成功");
        return allUserList;
    }

    /**
     * 将其他平台的JSONObject转换成用户信息
     * @param userJSONObject 其他平台的用户信息
     * @param platformType 平台类别。8表示网管；9表示会易通；10表示一机一档
     * @return 本平台的用户信息
     */
    private UserVO convertUserByJSONObject(JSONObject userJSONObject, String platformType) {
        UserVO resultUser = new UserVO();

        if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(platformType)) {
            //将网管平台的用户信息转换成本平台的用户信息
            resultUser.setLoginName(userJSONObject.getString("userid"));//用户登录名
            resultUser.setName(userJSONObject.getString("name"));//用户真实姓名
            resultUser.setLoginPwd(userJSONObject.getString("pwd"));//密码
            resultUser.setPhone(userJSONObject.getString("tel"));//用户手机号
            resultUser.setEmail(userJSONObject.getString("email"));//用户邮箱
            resultUser.setAreaId(userJSONObject.getString("regionid"));//所属区域ID
            resultUser.setAreaName(userJSONObject.getString("regionname"));//所属区域名称
            resultUser.setCreateTime(TimeUtil.dateToString(userJSONObject
                    .getDate("createtime"), "yyyy-MM-dd HH:mm:ss"));//创建时间
            resultUser.setCreateTime(TimeUtil.dateToString(userJSONObject
                    .getDate("updatetime"), "yyyy-MM-dd HH:mm:ss"));//最后一次更新时间
        } else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE.equals(platformType)) {
            //将会易通平台的用户信息转换成本平台的用户信息
            resultUser.setLoginName(userJSONObject.getString("phone"));//会易通用户手机号就是用户名称
            resultUser.setPhone(userJSONObject.getString("phone"));//手机号
            resultUser.setName(userJSONObject.getString("name"));//姓名
            resultUser.setLoginPwd(userJSONObject.getString("loginPwd"));//密码
            resultUser.setAreaId(StringUtil
                    .getCompleteString(userJSONObject.getString("areaId"), 12));//补全后的行政区域ID
            resultUser.setAreaName(userJSONObject.getString("areaName"));//行政区域名称
        } else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE.equals(platformType)) {
            //将一机一档平台的用户信息转换成本平台的用户信息
            resultUser.setPhone(userJSONObject.getString("account"));//手机号
            resultUser.setLoginName(userJSONObject.getString("account"));//一机一档用户手机号就是用户名称
            resultUser.setName(userJSONObject.getString("name"));//用户真实姓名
            resultUser.setLoginPwd(userJSONObject.getString("password"));//用户密码
            resultUser.setAreaId(StringUtil
                    .getCompleteString(userJSONObject
                            .getString("organ"), 12));//补全后的行政区域ID,organ表示一机一档最小级行政区域
            resultUser.setAreaName(userJSONObject.getString("organName"));//行政区域名称
            resultUser.setCreateTime(userJSONObject.getString("createtime"));//创建时间
            resultUser.setCreateTime(userJSONObject.getString("updatetime"));//最后一次更新时间
        }
        return resultUser;
    }

    /**
     * 将用户的List去除相同手机号的用户
     * @param allUserList 所有的用户List
     * @return 相同手机号的用户List，即allUserList删除的用户信息列表
     */
    private List<UserVO> removeSamePhoneUserList(List<UserVO> allUserList) {
        UserVO user;
        List<UserVO> samePhoneUserList = new ArrayList<UserVO>();//相同手机号的用户列表，即被删除的用户列表
        boolean isSamePhoneFlag;//是不是相同手机号的行为标志
        for (int i = 0; i < allUserList.size(); i++) {
            user = allUserList.get(i);
            isSamePhoneFlag = false;
            for (int j = allUserList.size() - 1; j > i; j--) {
                if (StringUtils.isNotBlank(user.getPhone()) && StringUtils.isNotBlank(allUserList.get(j).getPhone()) &&
                        user.getPhone().equals(allUserList.get(j).getPhone())) {
                    samePhoneUserList.add(allUserList.get(j));
                    allUserList.remove(j);
                    isSamePhoneFlag = true;
                }
            }
            if (isSamePhoneFlag) {
                samePhoneUserList.add(user);
            }
        }
        allUserList.removeAll(samePhoneUserList);
        return samePhoneUserList;
    }

    /**
     * 删除无效数据的用户：
     *  1、手机号、用户真实姓名、登录名、密码、所属区域ID、所属区域名称为空的用户
     *  2、其他平台用户与数据中心用户登录名一样，但是手机号不一样
     *  3、其他平台用户与数据中心用户登录名不一样，但是手机号一样(只有网管平台需要这样处理，因为网管平台的登录名和手机号不一样；而会易通、
     *      一机一档用户登录名和手机号是一样的)
     * @param otherAllUserList 所有用户的数据。去除手机号为空、用户名为空、用户名不同手机号相同(只有网管会这样处理)、
     *                         手机号不同用户名相同。即最后只剩下需要插入的元素了
     * @param otherDataList 手机号为空、用户名为空的数据
     * @param localDataList 其他平台和本平台用户不同的数据
     * @param updateUserList 需要更新的用户List
     * @param platformType 平台类别。8表示网管，9表示会易通，10表示一机一档。9和10只处理手机号不同用户名相同的情况
     * @param platformId 平台ID
     */
    private void removeInvalidUser(List<UserVO> otherAllUserList, List<SyncUserResultEntity> otherDataList,
                                                 List<SyncUserResultEntity> localDataList, List<UserVO> updateUserList,
                                                 String platformType, String platformId) {
        String currentTime = TimeUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
        Set<String> regionIdSet = new HashSet<>();//所属区域ID集合, 不能重复
        boolean isOut = false;//跳出外层循环
        for (int i = 0; i < otherAllUserList.size(); i++) {
            isOut = false;
            UserVO otherUser = otherAllUserList.get(i);//其他平台用户信息
            if (CommonConstrant.WHOLE_COUNTRY_REGION_ID.equals(otherUser.getAreaId())) {//全国数据
            	otherAllUserList.get(i).setAreaName("全国");//设置区域名称
            }
            /* 必填字段(用户名称、手机号、登录名、密码、行政区域ID、行政区域名称)为空的用户 Start */
            if (StringUtils.isBlank(otherUser.getPhone())) {//手机号为空
                otherAllUserList.remove(otherUser);
                otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                        otherUser.getPhone(), otherUser.getCreateTime(), REASON_USER_NULL_PHONE));
                i--;
                continue;
            }
            if (!StringUtil.isCorrectPhone(otherUser.getPhone())) {//手机号不匹配
                otherAllUserList.remove(otherUser);
                otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                        otherUser.getPhone(), otherUser.getCreateTime(), REASON_USER_ERROR_PHONE));
                i--;
                continue;
            }
            if (StringUtils.isBlank(otherUser.getLoginName())) {//登录名为空
                otherAllUserList.remove(otherUser);
                otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                        otherUser.getPhone(), otherUser.getCreateTime(), REASON_USER_NULL_LOGIN_NAME));
                i--;
                continue;
            }
            if (StringUtils.isBlank(otherUser.getAreaId())) {//行政区域ID为空
                otherAllUserList.remove(otherUser);
                otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                        otherUser.getPhone(), otherUser.getCreateTime(), REASON_USER_NULL_REGION_ID));
                i--;
                continue;
            }
            if (StringUtils.isBlank(otherUser.getAreaName())) {//行政区域名称为空
                otherAllUserList.remove(otherUser);
                otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                        otherUser.getPhone(), otherUser.getCreateTime(), REASON_USER_NULL_REGION_NAME));
                i--;
                continue;
            }
            if (StringUtils.isBlank(otherUser.getLoginPwd())) {//用户密码为空
                otherAllUserList.remove(otherUser);
                otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                        otherUser.getPhone(), otherUser.getCreateTime(), REASON_USER_NULL_LOGIN_PWD));
                i--;
                continue;
            }
            if (StringUtils.isBlank(otherUser.getName())) {//用户真实姓名为空
                otherAllUserList.remove(otherUser);
                otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                        otherUser.getPhone(), otherUser.getCreateTime(), REASON_USER_NULL_REAL_NAME));
                i--;
                continue;
            }
            /* 必填字段(用户名称、手机号、登录名、密码、行政区域ID、行政区域名称)为空的用户 End */

            /* 其他平台与本平台用户不同的数据手机号相同用户名不同或者手机号不同用户名相同) Start */
            //通过用户登录名或者电话号码查询用户信息
            List<UserVO> userList = this.userDao.selectUserByUniqueKeyWithOr(otherUser);
            if(userList != null && userList.size() > 0) {
                isOut = true;//说明存在这个用户
                for (UserVO localUser : userList) {
                    //其他平台用户与数据中心用户登录名一样，但是手机号不一样
                    if ((otherUser.getLoginName().equals(localUser.getLoginName())) &&
                            !(otherUser.getPhone().equals(localUser.getPhone()))) {
                        otherAllUserList.remove(otherUser);
                        localDataList.add(this.generateSyncUserResult(localUser, otherUser.getLoginName(),
                                otherUser.getPhone(), otherUser.getCreateTime(), REASON_SAME_LOGIN_NAME));
                        i--;
                        break;
                    }

                    //其他平台用户与数据中心用户登录名不一样，但是手机号一样
                    if (!(otherUser.getLoginName().equals(localUser.getLoginName())) &&
                            (otherUser.getPhone().equals(localUser.getPhone()))) {
                        if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(platformType)) {
                            //只有网管平台需要这样处理
                            otherAllUserList.remove(otherUser);
                            localDataList.add(this.generateSyncUserResult(localUser, otherUser.getLoginName(),
                                    otherUser.getPhone(), otherUser.getCreateTime(), REASON_SAME_PHONE));
                            i--;
                            break;
                        } else {//一机一档、会易通用户名和手机号是一样的，所以不进行处理
                            otherUser.setLoginName(localUser.getLoginName());//以本地用户名为准
                        }
                    }

                    /* 剩下的数据(数据中心数据库中存在)就是需要更新的数据 Start */
                    if (StringUtils.isBlank(otherUser.getAreaId())) {//对方平台没有传行政区域ID
                        otherUser.setAreaId(localUser.getAreaId());//使用本地行政区域ID
                    }
                    if (StringUtils.isBlank(otherUser.getAreaName())) {//对方平台没有传行政区域名称
                        otherUser.setAreaName(localUser.getAreaName());//使用本地行政区域名称
                    }
                    if (StringUtils.isBlank(otherUser.getLoginPwd())) {//对方平台没有传密码
                        otherUser.setLoginPwd(localUser.getLoginPwd());//使用本地密码
                    }
                    otherUser.setSource(platformId);
                    otherUser.setExt1(platformId);
                    otherUser.setUpdateTime(currentTime);
                    otherUser.setUuid(localUser.getUuid());
                    updateUserList.add(otherUser);//更新的用户
                    otherAllUserList.remove(otherUser);//从所有的用户中删除，所有的用户List就是需要添加的用户
                    i--;
                    /* 剩下的数据(数据中心数据库中存在)就是需要更新的数据 End */
                }
            }
            if (isOut) {//存在的用户,要么是失败,要么是更新,底下的逻辑是添加的用户信息,所以跳过本次循环,继续下一次循环
                continue;
            }
            /* 其他平台不存在的用户数据(即需要添加进数据库的数据)的数据补齐 Start */
            otherUser.setExt1(platformId);
            otherUser.setSource(platformId);
            if (StringUtils.isBlank(otherUser.getCreateTime())) {
                otherUser.setCreateTime(currentTime);
            }
            if (StringUtils.isBlank(otherUser.getUpdateTime())) {
                otherUser.setUpdateTime(currentTime);
            }
            otherUser.setPermission("0,0,0,0,0,0,0,0,0");//为了兼容会管同步用户功能
            otherUser.setUuid(StringUtil.get32UUID());//生成本地主键ID
            /* 其他平台不存在的用户数据(即需要添加进数据库的数据)的数据补齐 End */

            if (!CommonConstrant.WHOLE_COUNTRY_REGION_ID.equals(otherUser.getAreaId())) {//全国数据无需校验
                regionIdSet.add(otherUser.getAreaId());//校验行政区域是否是国标的Set
            }
            /* 其他平台与本平台用户不同的数据手机号相同用户名不同或者手机号不同用户名相同) End */
        }

        /* 计算用户的所属区域ID是否是国标的和所属区域名称问题(一机一档的所属区域名称可能不是国标) Start */
        String[] regionIdArr = new String[regionIdSet.size()];//无重复
        List<TRegionb> regionList = this.regionDao.selectRegionByIds(regionIdSet.toArray(regionIdArr));
        if (regionList == null || regionList.size() == 0) {//所有数据都不是国标的数据
            for (int i = 0; i < otherAllUserList.size(); i++) {
                UserVO otherUser = otherAllUserList.get(i);
                if (CommonConstrant.WHOLE_COUNTRY_REGION_ID.equals(otherUser.getAreaId())) {//全国数据无需校验
                	continue;
                }
                otherAllUserList.remove(otherUser);
                otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                        otherUser.getPhone(), otherUser.getCreateTime(),
                        REASON_REGION_NOT_NATIONAL_STANDARD + ": " + otherUser.getAreaId()));
                i--;
            }
        } else {
            boolean isNotRegionNationalStandard = true;//是否不是国标的所属区域的标志
            for (int i = 0; i < otherAllUserList.size(); i++) {
                UserVO otherUser = otherAllUserList.get(i);
                if (CommonConstrant.WHOLE_COUNTRY_REGION_ID.equals(otherUser.getAreaId())) {//全国数据无需校验
                	continue;
                }
                isNotRegionNationalStandard = true;//默认不是国标
                for (int j = 0; j < regionList.size(); j++) {
                    if (otherUser.getAreaId().equals(regionList.get(j).getId())) {//用户的所属区域是国标的
                        otherUser.setAreaName(regionList.get(j).getName());//设置区域名称
                        isNotRegionNationalStandard = false;//是国标
                        break;//跳出内层循环
                    }
                }
                if (isNotRegionNationalStandard) {//用户不是国标的区域
                    otherAllUserList.remove(otherUser);
                    otherDataList.add(this.generateSyncUserResult(null, otherUser.getLoginName(),
                            otherUser.getPhone(), otherUser.getCreateTime(),
                            REASON_REGION_NOT_NATIONAL_STANDARD + ": " + otherUser.getAreaId()));
                    i--;
                }
            }
        }
        /* 计算用户的所属区域ID是否是国标的和所属区域名称问题(一机一档的所属区域名称可能不是国标) End */
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
    private SyncUserResultEntity generateSyncUserResult(UserVO localUser, String otherLoginName,
                                                        String otherPhone, String otherCreateTime, String reason) {
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
}
