package com.visionvera.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.ServerConfig;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.constrant.PlatformTypeConstrant;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.enums.PushUserTypeEnum;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.PushUserService;
import com.visionvera.service.ServerConfigService;
import com.visionvera.service.UserService;
import com.visionvera.util.ReturnDataUtil;
import com.visionvera.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 向其他平台推送用户信息的业务
 */
@Service
public class PushUserServiceImpl implements PushUserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushUserServiceImpl.class);

    @Autowired
    private JRedisDao jRedisDao;

    @Autowired
    private ServerConfigService serverConfigService;

    @Autowired
    private UserService userService;

    /** 默认的退出次数: 3 */
    private final int DEFAULT_RETURN_COUNT = 3;

    /***
     * 向其他平台推送添加用户的操作
     * @param user 用户信息
     * @param token 访问标识
     * @return
     */
    @Async
    @Override
    public void pushUserForAdd(UserVO user, String token) {
        String[] platformTypeArr = new String[]{PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE,
                PlatformTypeConstrant.VSDC_PLATFORM_TYPE,
                PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE};
        List<ServerConfig> netConfigList = new ArrayList<>();//网管配置信息
        List<ServerConfig> vsdcConfigList = new ArrayList<>();//一机一档配置信息
        List<ServerConfig> hytConfigList = new ArrayList<>();//会易通配置信息
        try {
            /* 查询配置列表，并将配置列表计算分类 Start */
            List<ServerConfig> serverConfigList = serverConfigService
                    .getServerConfigByPlatformType(platformTypeArr);
            if (serverConfigList != null && serverConfigList.size() > 0) {
                for (ServerConfig serverConfig : serverConfigList) {
                    if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//网管
                        netConfigList.add(serverConfig);
                    } else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//一机一档
                        vsdcConfigList.add(serverConfig);
                    } else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//会易通
                        hytConfigList.add(serverConfig);
                    }
                }
                serverConfigList = null;//置空，节约内存空间
            }
            /* 查询配置列表，并将配置列表计算分类 End */
        } catch (Exception e) {
            LOGGER.error("向其他平台推送添加用户失败", e);
        }

        //向所有网管推送添加用户信息, 异步
        pushUserForNetManager(user, PushUserTypeEnum.ADD, token, netConfigList);
        //向所有一机一档推送修改用户信息, 异步
        pushUserForVSDC(user, PushUserTypeEnum.ADD, vsdcConfigList);
        //向所有会易通推送修改用户信息, 异步
        pushUserForHYT(user, PushUserTypeEnum.ADD, hytConfigList);

    }

    /**
     * 向其他平台推送修改用户的操作
     * @param user 用户信息
     * @param token 本地访问标识
     * @return
     */
    @Async
    @Override
    public void pushUserForEdit(UserVO user, String token) {
        String[] platformTypeArr = new String[]{PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE,
                PlatformTypeConstrant.VSDC_PLATFORM_TYPE,
                PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE};
        List<ServerConfig> netConfigList = new ArrayList<>();//网管配置信息
        List<ServerConfig> vsdcConfigList = new ArrayList<>();//一机一档配置信息
        List<ServerConfig> hytConfigList = new ArrayList<>();//会易通配置信息
        Map<String, Object> paramsMap = new HashMap<>(2);//本次确定只使用1个长度,所以申请2个Entry数组，避免扩容
        try {
            /* 查询配置列表，并将配置列表计算分类 Start */
            List<ServerConfig> serverConfigList = serverConfigService
                    .getServerConfigByPlatformType(platformTypeArr);
            if (serverConfigList != null && serverConfigList.size() > 0) {
                for (ServerConfig serverConfig : serverConfigList) {
                    if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//网管
                        netConfigList.add(serverConfig);
                    } else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//一机一档
                        vsdcConfigList.add(serverConfig);
                    } else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//会易通
                        hytConfigList.add(serverConfig);
                    }
                }
                serverConfigList = null;//置空，节约内存空间
            }
            /* 查询配置列表，并将配置列表计算分类 End */

            /* 数据补齐 Start */

            //查询数据库里面的用户信息
            paramsMap.put("phone", user.getPhone());
            UserVO dataUser = userService.selectUserByUniqueKey(paramsMap);
            if (dataUser != null) {
                if (StringUtils.isBlank(user.getCreateTime())) {
                    user.setCreateTime(dataUser.getCreateTime());
                }
                if (StringUtils.isBlank(user.getLoginPwd())) {
                    user.setLoginPwd(dataUser.getLoginPwd());
                }
            } else {
                LOGGER.error("本地数据库没有该用户的数据, 用户数据为: {}", JSONObject.toJSONString(user));
            }
            /* 数据补齐 End */

        } catch (Exception e) {
            LOGGER.error("向其他平台推送修改用户失败", e);
        }

        //向所有网管推送修改用户信息, 异步
        pushUserForNetManager(user, PushUserTypeEnum.EDIT, token, netConfigList);
        //向所有一机一档推送修改用户信息, 异步
        pushUserForVSDC(user, PushUserTypeEnum.EDIT, vsdcConfigList);
        //向所有会易通推送修改用户信息, 异步
        pushUserForHYT(user, PushUserTypeEnum.EDIT, hytConfigList);
    }

    /**
     * 向其他平台推送删除用户的操作
     * @param user 用户信息
     * @param token 本地访问标识
     * @return
     */
    @Async
    @Override
    public void pushUserForDel(UserVO user, String token) {
        String[] platformTypeArr = new String[]{PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE,
                PlatformTypeConstrant.VSDC_PLATFORM_TYPE,
                PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE};
        List<ServerConfig> netConfigList = new ArrayList<>();//网管配置信息
        List<ServerConfig> vsdcConfigList = new ArrayList<>();//一机一档配置信息
        List<ServerConfig> hytConfigList = new ArrayList<>();//会易通配置信息
        Map<String, Object> paramsMap = new HashMap<>(2);//本次确定只使用1个长度,所以申请2个Entry数组，避免扩容
        try {
            /* 查询配置列表，并将配置列表计算分类 Start */
            List<ServerConfig> serverConfigList = serverConfigService
                    .getServerConfigByPlatformType(platformTypeArr);
            if (serverConfigList != null && serverConfigList.size() > 0) {
                for (ServerConfig serverConfig : serverConfigList) {
                    if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//网管
                        netConfigList.add(serverConfig);
                    } else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//一机一档
                        vsdcConfigList.add(serverConfig);
                    } else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE
                            .equals(serverConfig.getPlatformType())) {//会易通
                        hytConfigList.add(serverConfig);
                    }
                }
                serverConfigList = null;//置空，节约内存空间
            }
            /* 查询配置列表，并将配置列表计算分类 End */
        } catch (Exception e) {
            LOGGER.error("向其他平台推送修改用户失败", e);
        }
        //向所有网管推送删除用户信息, 异步
        pushUserForNetManager(user, PushUserTypeEnum.DEL, token, netConfigList);
        //向所有一机一档推送删除用户信息, 异步
        pushUserForVSDC(user, PushUserTypeEnum.DEL, vsdcConfigList);
        //向所有会易通推送修改用户信息, 异步
        pushUserForHYT(user, PushUserTypeEnum.DEL, hytConfigList);
    }

    /**
     * 给所有网管推送用户信息：添加、修改、删除
     * @param user 用户信息
     * @param typeEnum 操作类型
     * @param token 本平台的token，调用网管登录接口使用
     * @param serverConfigList 服务配置列表
     */
    @Async
    public void pushUserForNetManager(UserVO user, PushUserTypeEnum typeEnum, String token,
                                      List<ServerConfig> serverConfigList) {
        JSONObject paramsJSONObject = new JSONObject();//请求参数
        JSONObject resultJSONObject = new JSONObject();//返回参数
        String url = "";//请求的URL
        String otherToken = "";//其他平台的token
        Map<String, String> uriVariables = new HashMap<>();//请求的URL参数
        try {
            if (serverConfigList != null && serverConfigList.size() > 0) {
                if (PushUserTypeEnum.ADD.equals(typeEnum)) {//添加用户
                    //将用户基本信息转换成网管接口的参数
                    paramsJSONObject = getOtherPlatformParams(user, PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE);
                    for (ServerConfig config : serverConfigList) {
                        uriVariables.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId(), token);//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取网管的token为空, 具体请查看日志信息. 获取的平台是: {}", JSONObject.toJSONString(config));
                            continue;
                        }

                        paramsJSONObject.put("createid", config.getLoginName());//网管的用户的创建者
                        paramsJSONObject.put("updateid", config.getLoginName());//网管用户的修改者
                        url = config.getUrl() + WsConstants.NET_MANAGER_USER_ADD_URL;//网管添加用户的接口URL
                        uriVariables.put("pathparam", config.getLoginName());//当前登录网管的用户ID
                        uriVariables.put("token", otherToken);//当前登录网管的token
                        pushUserForNetManagerRecursion(url, config.getOtherPlatformId(), paramsJSONObject,
                                resultJSONObject, uriVariables, token, PushUserTypeEnum.ADD, DEFAULT_RETURN_COUNT);
                    }
                } else if (PushUserTypeEnum.EDIT.equals(typeEnum)) {//修改用户
                    //将用户基本信息转换成网管接口的参数
                    paramsJSONObject = getOtherPlatformParams(user, PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE);
                    for (ServerConfig config : serverConfigList) {
                        uriVariables.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId(), token);//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取网管的token为空, 具体请查看日志信息. 获取的平台是: {}", JSONObject.toJSONString(config));
                            continue;
                        }
                        paramsJSONObject.put("createid", config.getLoginName());//网管的用户的创建者
                        paramsJSONObject.put("updateid", config.getLoginName());//网管用户的修改者
                        url = config.getUrl() + WsConstants.NET_MANAGER_USER_EDIT_URL;//网管修改用户的接口URL
                        uriVariables.put("pathparam", config.getLoginName());//当前登录网管的用户ID
                        uriVariables.put("token", otherToken);//当前登录网管的token
                        pushUserForNetManagerRecursion(url, config.getOtherPlatformId(), paramsJSONObject,
                                resultJSONObject, uriVariables, token, PushUserTypeEnum.EDIT, DEFAULT_RETURN_COUNT);
                    }
                } else if (PushUserTypeEnum.DEL.equals(typeEnum)) {//删除用户
                    //将用户基本信息转换成网管接口的参数
                    for (ServerConfig config : serverConfigList) {
                        uriVariables.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId(), token);//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取网管的token为空, 具体请查看日志信息. 获取的平台是: {}", JSONObject.toJSONString(config));
                            continue;
                        }
                        url = config.getUrl() + WsConstants.NET_MANAGER_USER_DEL_URL;//网管删除用户的接口URL
                        uriVariables.put("pathparam", config.getLoginName());//当前登录网管的用户ID
                        uriVariables.put("token", otherToken);//当前登录网管的token
                        uriVariables.put("userId", user.getLoginName());
                        uriVariables.put("phone", user.getPhone());//手机号
                        pushUserForNetManagerRecursion(url, config.getOtherPlatformId(), paramsJSONObject,
                                resultJSONObject, uriVariables, token, PushUserTypeEnum.DEL, DEFAULT_RETURN_COUNT);
                    }
                } else {
                    LOGGER.warn("操作类型出现异常");
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.error("向网管平台推送用户失败", e);
        }
    }

    /**
     * 给所有一机一档推送用户信息：添加、修改、删除
     * @param user 用户信息
     * @param typeEnum 操作类型
     * @param serverConfigList 服务配置列表
     */
    @Async
    public void pushUserForVSDC(UserVO user, PushUserTypeEnum typeEnum, List<ServerConfig> serverConfigList) {
        JSONObject paramsJSONObject = new JSONObject();//请求参数
        JSONObject resultJSONObject = new JSONObject();//返回参数
        String url = "";//请求的URL
        String otherToken = "";//其他平台的token
        try {
            if (serverConfigList != null && serverConfigList.size() > 0) {
                if (PushUserTypeEnum.ADD.equals(typeEnum)) {//添加用户
                    //将用户基本信息转换成一机一档接口的参数
                    paramsJSONObject = getOtherPlatformParams(user, PlatformTypeConstrant.VSDC_PLATFORM_TYPE);
                    for (ServerConfig config : serverConfigList) {
                        resultJSONObject.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId());//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取一机一档的token为空, 具体请查看日志信息. 获取的平台是: {}", JSONObject.toJSONString(config));
                            continue;
                        }
                        paramsJSONObject.put("accesstoken", otherToken);//一机一档访问TOEKN
                        url = config.getUrl() + WsConstants.VSDC_USER_ADD_URL;//一机一档添加用户的接口URL
                        pushUserForVSDCRecursion(url, config.getOtherPlatformId(), paramsJSONObject, resultJSONObject,
                                DEFAULT_RETURN_COUNT);
                    }
                } else if (PushUserTypeEnum.EDIT.equals(typeEnum)) {//修改用户
                    //将用户基本信息转换成一机一档接口的参数
                    paramsJSONObject = getOtherPlatformParams(user, PlatformTypeConstrant.VSDC_PLATFORM_TYPE);
                    for (ServerConfig config : serverConfigList) {
                        resultJSONObject.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId());//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取一机一档的token为空, 具体请查看日志信息. 获取的平台是: {}", JSONObject.toJSONString(config));
                            continue;
                        }
                        paramsJSONObject.put("accesstoken", otherToken);//一机一档访问TOEKN
                        url = config.getUrl() + WsConstants.VSDC_USER_EDIT_URL;//一机一档修改用户的接口URL
                        pushUserForVSDCRecursion(url, config.getOtherPlatformId(), paramsJSONObject, resultJSONObject,
                                DEFAULT_RETURN_COUNT);
                    }
                } else if (PushUserTypeEnum.DEL.equals(typeEnum)) {//删除用户
                    paramsJSONObject.put("uuid", user.getUuid());
                    paramsJSONObject.put("phone", user.getPhone());
                    for (ServerConfig config : serverConfigList) {
                        resultJSONObject.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId());//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取一机一档的token为空, 具体请查看日志信息. 获取的平台是: {}", JSONObject.toJSONString(config));
                            continue;
                        }
                        paramsJSONObject.put("accesstoken", otherToken);//一机一档访问TOEKN
                        url = config.getUrl() + WsConstants.VSDC_USER_DEL_URL;//一机一档删除用户的接口URL
                        pushUserForVSDCRecursion(url, config.getOtherPlatformId(), paramsJSONObject, resultJSONObject,
                                DEFAULT_RETURN_COUNT);
                    }
                } else {
                    LOGGER.warn("操作类型出现异常");
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.error("向一机一档平台推送用户失败", e);
        }
    }

    /**
     * 给所有会易通推送用户信息：添加、修改、删除
     * @param user 用户信息
     * @param typeEnum 操作类型
     * @param serverConfigList 服务配置列表
     */
    @Async
    public void pushUserForHYT(UserVO user, PushUserTypeEnum typeEnum, List<ServerConfig> serverConfigList) {
        JSONObject paramsJSONObject = new JSONObject();//请求参数
        JSONObject resultJSONObject = new JSONObject();//返回参数
        String url = "";//请求的URL
        String otherToken = "";//其他平台的token
        try {
            if (serverConfigList != null && serverConfigList.size() > 0) {
                if (PushUserTypeEnum.ADD.equals(typeEnum)) {//添加用户
                    //将用户基本信息转换成会易通接口的参数
                    paramsJSONObject = getOtherPlatformParams(user, PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE);
                    for (ServerConfig config : serverConfigList) {
                        resultJSONObject.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId());//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取会易通的token为空, 具体请查看日志信息. 获取的平台是: {}",
                                    JSONObject.toJSONString(config));
                            continue;
                        }
                        paramsJSONObject.put("hytToken", otherToken);//会易通访问TOEKN
                        url = config.getUrl() + WsConstants.HUIYITONG_USER_ADD_URL;//会易通添加用户的接口URL
                        pushUserForHYTRecursion(url, config.getOtherPlatformId(), paramsJSONObject, resultJSONObject,
                                DEFAULT_RETURN_COUNT);
                    }
                } else if (PushUserTypeEnum.EDIT.equals(typeEnum)) {//修改用户
                    //将用户基本信息转换成会易通接口的参数
                    paramsJSONObject = getOtherPlatformParams(user, PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE);
                    for (ServerConfig config : serverConfigList) {
                        resultJSONObject.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId());//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取会易通的token为空, 具体请查看日志信息. 获取的平台是: {}",
                                    JSONObject.toJSONString(config));
                            continue;
                        }
                        paramsJSONObject.put("hytToken", otherToken);//会易通访问TOEKN
                        url = config.getUrl() + WsConstants.HUIYITONG_USER_EDIT_URL;//会易通修改用户的接口URL
                        pushUserForHYTRecursion(url, config.getOtherPlatformId(), paramsJSONObject, resultJSONObject,
                                DEFAULT_RETURN_COUNT);
                    }
                } else if (PushUserTypeEnum.DEL.equals(typeEnum)) {//删除用户
                    paramsJSONObject.put("phone", user.getPhone());
                    for (ServerConfig config : serverConfigList) {
                        resultJSONObject.clear();
                        otherToken = getOtherPlatformToken(config.getOtherPlatformId());//获取token
                        if (StringUtils.isBlank(otherToken)) {
                            LOGGER.warn("获取会易通的token为空, 具体请查看日志信息. 获取的平台是: {}",
                                    JSONObject.toJSONString(config));
                            continue;
                        }
                        paramsJSONObject.put("hytToken", otherToken);//会易通访问TOEKN
                        url = config.getUrl() + WsConstants.HUIYITONG_USER_DEL_URL;//会易通删除用户的接口URL
                        pushUserForHYTRecursion(url, config.getOtherPlatformId(), paramsJSONObject, resultJSONObject,
                                DEFAULT_RETURN_COUNT);
                    }
                } else {
                    LOGGER.warn("操作类型出现异常");
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.error("向会易通平台推送用户失败", e);
        }
    }

    /**
     * 给网管推送用户信息：添加、修改、删除
     * @param url 请求的URL
     * @param platformId 平台ID
     * @param paramsJSONObject 参数
     * @param resultJSONObject 返回结果
     * @param uriVariables URL参数的Map
     * @param localToken 本地用户的Token。调用网管获取token接口需要使用
     * @param type 操作类型。ADD表示添加；EDIT表示修改，DEL表示删除，删除的调用方式为DELETE，所以需要区分一下
     * @param returnCount 当调用网管用户接口时，网管TOEKN过期，需要递归调用。该值表示如果出现<=0的情况则退出整个方法
     */
    private int pushUserForNetManagerRecursion(String url, String platformId,
                                               JSONObject paramsJSONObject, JSONObject resultJSONObject,
                                               Map<String, String> uriVariables, String localToken,
                                               PushUserTypeEnum type,int returnCount) {
        LOGGER.info("开始调用网管用户接口, 调用的网管地址为: {}, BODY参数为: {}, URI参数为: {}", url, paramsJSONObject,
                JSONObject.toJSONString(uriVariables));
        try {
            if (PushUserTypeEnum.DEL.equals(type)) {//删除的为delete方式调用
                resultJSONObject = RestTemplateUtil.deleteForObject(url, JSONObject.class, uriVariables);
            } else {//添加、修改为post方式调用
                resultJSONObject = RestTemplateUtil.postForObject(url, paramsJSONObject,
                        JSONObject.class, uriVariables);//调用网管用户接口
            }
        } catch (Exception e) {
            LOGGER.error("调用网管用户接口失败: 无法连接网管服务器", e);
            return returnCount;//调用失败, 直接退出
        }
        LOGGER.info("结束调用网管用户接口, 网管返回的结果是: {}", resultJSONObject.toJSONString());
        if (resultJSONObject == null || resultJSONObject.size() == 0) {
            LOGGER.error("向网管推送用户信息失败, 网管返回的数据为空");
            return returnCount;//调用失败, 直接退出
        }
        if (resultJSONObject.getInteger("ret") == null) {
            LOGGER.error("向网管推送用户信息失败, 网管返回的ret数据为空, 返回的数据为: {}", resultJSONObject);
            return returnCount;//调用失败, 直接退出
        }
        //依据网管开发人员信息:1:表示运维工作站传递的token为空。2:表示运维工作站传递的token和网管的token不一样。若存在其他情况则不兼容
        if (resultJSONObject.getInteger("ret").equals(1) || resultJSONObject.getInteger("ret").equals(2)) {
            LOGGER.warn("网管TOKEN过期或token验证失败, 进行递归调用, 递归调用数为: {}", returnCount);
            if (returnCount == 1) {
                return 1;
            } else {//递归调用
                //删除本地缓存的过期(相对于网管来说过期)token
                delOtherPlatformToken(platformId);
                uriVariables.put("token", getOtherPlatformToken(platformId, localToken));//重新设置token
                return pushUserForNetManagerRecursion(url, platformId, paramsJSONObject, resultJSONObject,
                        uriVariables, localToken, type, --returnCount);//递归调用
            }
        }
        return returnCount;
    }

    /**
     * 给一机一档推送用户信息：添加、修改、删除
     * @param url 请求的URL
     * @param platformId 平台ID
     * @param paramsJSONObject 参数
     * @param resultJSONObject 返回结果
     * @param returnCount 当调用一机一档用户接口时，一机一档TOEKN过期，需要递归调用。该值表示如果出现<=0的情况则退出整个方法
     */
    private int pushUserForVSDCRecursion(String url, String platformId, JSONObject paramsJSONObject,
                                          JSONObject resultJSONObject, int returnCount) {
        LOGGER.info("开始调用一机一档用户接口, 调用的一机一档地址为: {}, BODY参数为: {}", url, paramsJSONObject);
        try {
            //添加和修改为POST方式调用
            resultJSONObject = RestTemplateUtil.postForObjectByForm(url,
                    getParamsMap(paramsJSONObject), JSONObject.class);//调用一机一档添加用户接口
        } catch (Exception e) {
            LOGGER.error("添加一机一档用户失败: 无法连接一机一档服务器", e);
            return returnCount;//调用失败, 直接退出
        }
        LOGGER.info("结束调用一机一档用户接口, 一机一档返回的结果是: {}", resultJSONObject.toJSONString());
        if (resultJSONObject == null || resultJSONObject.size() == 0) {
            LOGGER.error("向一机一档推送用户信息失败, 一机一档返回的数据为空");
            return returnCount;//调用失败, 直接退出
        }
        if (resultJSONObject.getInteger("result") == null) {
            LOGGER.error("向一机一档推送用户信息失败, 一机一档返回的result数据为空, 返回的数据为: {}", resultJSONObject);
            return returnCount;//调用失败, 直接退出
        }
        if (resultJSONObject.getInteger("result").equals(4)) {//TOKEN过期，需要递归调用
            LOGGER.warn("一机一档TOKEN过期, 进行递归调用, 递归调用数为: {}", returnCount);
            if (returnCount == 1) {
                return 1;
            } else {//递归调用
                //删除本地缓存的过期(相对于一机一档来说过期)token
                delOtherPlatformToken(platformId);
                paramsJSONObject.put("accesstoken", getOtherPlatformToken(platformId));//重新设置token
                return pushUserForVSDCRecursion(url, platformId, paramsJSONObject,
                        resultJSONObject, --returnCount);//递归调用
            }
        }
        return returnCount;
    }

    /**
     * 给会易通推送用户信息：添加、修改、删除
     * @param url 请求的URL
     * @param platformId 平台ID
     * @param paramsJSONObject 参数
     * @param resultJSONObject 返回结果
     * @param returnCount 当调用会易通用户数据时，会易通TOEKN过期，需要递归调用。该值表示如果出现<=0的情况则退出整个方法
     */
    private int pushUserForHYTRecursion(String url, String platformId, JSONObject paramsJSONObject,
                                         JSONObject resultJSONObject, int returnCount) {
        LOGGER.info("开始调用会易通用户接口, 调用的会易通地址为: {}, BODY参数为: {}", url, paramsJSONObject);
        try {
            resultJSONObject = RestTemplateUtil.postForObjectByForm(url, getParamsMap(paramsJSONObject),
                    JSONObject.class);//调用会易通添加用户接口
        } catch (Exception e) {
            LOGGER.error("添加会易通用户失败: 无法连接会易通服务器", e);
            return returnCount;//调用失败, 直接退出
        }
        LOGGER.info("结束调用会易通用户接口, 会易通返回的结果是: {}", resultJSONObject.toJSONString());
        if (resultJSONObject == null || resultJSONObject.size() == 0) {
            LOGGER.error("向会易通推送用户信息失败, 会易通返回的数据为空");
            return returnCount;//调用失败, 直接退出
        }
        if (resultJSONObject.getInteger("code") == null) {
            LOGGER.error("向会易通推送用户信息失败, 会易通返回的code数据为空, 返回的数据为: {}", resultJSONObject);
            return returnCount;//调用失败, 直接退出
        }
        if (resultJSONObject.getInteger("code").equals(9)) {//TOKEN过期，需要递归调用
            LOGGER.warn("会易通TOKEN过期, 进行递归调用, 递归调用数为: {}", returnCount);
            if (returnCount == 1) {
                return 1;
            } else {//递归调用
                //删除本地缓存的过期(相对于一机一档来说过期)token
                delOtherPlatformToken(platformId);
                paramsJSONObject.put("hytToken", getOtherPlatformToken(platformId));//重新设置token
                return pushUserForHYTRecursion(url, platformId, paramsJSONObject,
                        resultJSONObject, --returnCount);//递归调用
            }
        }
        return returnCount;
    }

    /**
     * 将User信息转换成对应平台的接口参数
     * @param user 用户信息
     * @param platformType 平台类别。8表示网管，9表示会易通，10表示一机一档
     * @return
     */
    private JSONObject getOtherPlatformParams(UserVO user, String platformType) {
        JSONObject paramsJSONObject = new JSONObject();//请求参数
        if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(platformType)) {//网管
            paramsJSONObject.put("userid", user.getLoginName());//网管用户名
            paramsJSONObject.put("pwd", user.getLoginPwd());//网管密码
            paramsJSONObject.put("name", user.getName());//网管昵称
            paramsJSONObject.put("deptid", -1);//网管部门ID，依据给定文档传-1
            paramsJSONObject.put("roleid", -1);//网管角色ID，依据给定文档传-1
            paramsJSONObject.put("tel", user.getPhone());//网管手机号
        } else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE.equals(platformType)) {//会易通
            paramsJSONObject.put("name", user.getName());//会易通用户真实姓名
            paramsJSONObject.put("phone", user.getPhone());//会易通用户手机号
            paramsJSONObject.put("loginPwd", user.getLoginPwd());//会易通用户密码
            paramsJSONObject.put("areaId", user.getAreaId());//会易通用户行政区域ID
            paramsJSONObject.put("role", 1);//会易通用户角色。依据给定文档传1
        } else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE.equals(platformType)) {//一机一档
            paramsJSONObject.put("uuid", user.getUuid());//一机一档主键ID
            paramsJSONObject.put("phone", user.getPhone());//一机一档手机号
            paramsJSONObject.put("password", user.getLoginPwd());//一机一档用户密码
            paramsJSONObject.put("areaId", user.getAreaId());//一机一档所属区域ID，最小级
            paramsJSONObject.put("name", user.getName());//一机一档用户名称

        }
        return paramsJSONObject;
    }

    /**
     * 获取其他平台的登录token
     * 1 先从Redis缓存里面查找是否有该token信息
     *  1.1 如果有直接返回，如果没有则登录对应的平台获取token再存储token
     * 2 如果Redis出现异常或者Redis中没有该缓存信息，则从对应平台登录获取token
     * @param otherPlatformId 其他平台的平台ID
     * @param token 本平台token。调用网管登录接口使用，调用其他平台暂可以传空
     * @return 对应平台的token
     */
    @Override
    public String getOtherPlatformToken(String otherPlatformId, String token) {
        String otherPlatformToken = "";

        try {
            otherPlatformToken = jRedisDao.get(StringUtil.
                    getRedisKey(CommonConstrant.PUSH_USER_PREFIX_OTHER_PLATFORM, otherPlatformId));
        } catch (Exception e) {//服务异常，去对应平台获取token
            LOGGER.error("Redis服务出现异常，请配置好Redis服务");
        }
        if (StringUtils.isNotBlank(otherPlatformToken)) {//如果缓存中存在该平台的Token
            return otherPlatformToken;
        }

        /* 从对应平台获取token Start */
        String url = "";//调用对应平台的URL
        final JSONObject paramJSONObject = new JSONObject();//调用接口传递的参数
        JSONObject resultJSONObject = new JSONObject();//接口返回的数据
        //查询服务配置信息
        final ServerConfig serverConfig = serverConfigService.getServerConfigByOtherPlatformId(otherPlatformId);
        if (serverConfig != null) {
            url = serverConfig.getUrl();//基础URL：http://ip:port
            if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(serverConfig.getPlatformType())) {//网管平台
                url = url + WsConstants.NET_MANAGER_LOGIN_URL;//网管登录地址
                //调用对应平台的登录接口, 获取token
                paramJSONObject.put("userid", serverConfig.getLoginName());
                paramJSONObject.put("pwd", serverConfig.getLoginPwd());
                paramJSONObject.put("token", token);
                LOGGER.info("调用网管获取TOKEN接口的URL是: {}, BODY参数是: {}", url, paramJSONObject);
                try {
                    resultJSONObject = RestTemplateUtil.postForObject(url, paramJSONObject,
                            JSONObject.class);//调用网管登录接口
                } catch (Exception e) {
                    LOGGER.error("调用网管登录接口失败: 无法连接网管服务器", e);
                    throw new BusinessException("获取失败: 无法连接网管服务器");
                }
                if (resultJSONObject == null || resultJSONObject.size() == 0) {
                    LOGGER.error("获取Token信息失败: 网管没有任何返回数据");
                    throw new BusinessException("获取失败: 网管无数据返回");
                }
                if (resultJSONObject.getJSONObject("data") == null ||
                        resultJSONObject.getJSONObject("data").size() == 0) {
                    LOGGER.error("获取Token信息失败: 解析网管返回数据失败, 网管返回数据.data为空, 网管返回的数据为: {}",
                            resultJSONObject.toJSONString());
                    throw new BusinessException("获取失败: 网管无数据返回");
                }
                if (resultJSONObject.getJSONObject("data").getJSONObject("extra") == null ||
                        resultJSONObject.getJSONObject("data").getJSONObject("extra").size() == 0) {
                    LOGGER.error("获取Token信息失败: 解析网管返回数据失败, 网管返回数据.data.extra为空, 网管返回的数据为: {}",
                            resultJSONObject.toJSONString());
                    throw new BusinessException("获取失败: 网管无数据返回");
                }
                if (StringUtils.isBlank(resultJSONObject.getJSONObject("data").
                        getJSONObject("extra").getString("access_token"))) {
                    LOGGER.error("获取Token信息失败: 解析网管返回数据失败, 网管返回数据.data.extra.access_token为空, " +
                                    "网管返回的数据为: {}",
                            resultJSONObject.toJSONString());
                    throw new BusinessException("获取失败: " + resultJSONObject.getString("msg"));
                }
                otherPlatformToken = resultJSONObject.getJSONObject("data").
                        getJSONObject("extra").getString("access_token");//网管token
            } else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE.equals(serverConfig.getPlatformType())) {//一机一档平台
                url = url + WsConstants.VSDC_LOGIN_URL;//一机一档登录地址
                //调用对应平台的登录接口, 获取token
                paramJSONObject.put("user", serverConfig.getLoginName());//一机一档用户名
                paramJSONObject.put("secretkey", serverConfig.getLoginPwd());//一机一档密码
                LOGGER.info("调用一机一档获取TOKEN接口的URL是: {}, BODY参数是: {}", url, paramJSONObject);
                try {
                    //调用一机一档登录接口
                    resultJSONObject = RestTemplateUtil.postForObjectByForm(url, getParamsMap(paramJSONObject), JSONObject.class);
                } catch (Exception e) {
                    LOGGER.error("调用一机一档登录接口失败: 无法连接一机一档服务器", e);
                    throw new BusinessException("获取失败: 无法连接一机一档服务器");
                }
                if (resultJSONObject == null || resultJSONObject.size() == 0) {
                    LOGGER.error("获取Token信息失败: 一机一档没有任何返回数据");
                    throw new BusinessException("获取失败: 一机一档无数据返回");
                }
                if (StringUtils.isBlank(resultJSONObject.getString("token"))) {
                    LOGGER.error("获取Token信息失败: 一机一档没有任何返回token数据, 一机一档返回的数据为: {}",
                            resultJSONObject.toJSONString());
                    throw new BusinessException("获取失败: " + resultJSONObject.getString("msg"));
                }
                otherPlatformToken = resultJSONObject.getString("token");//一机一档token
            } else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE.equals(serverConfig.getPlatformType())) {//会易通平台
                url = url + WsConstants.HUIYITONG_LOGIN_URL;//会易通URL

                //调用对应平台的登录接口, 获取token
                paramJSONObject.put("loginName", serverConfig.getLoginName());//会易通用户名
                paramJSONObject.put("loginPwd", serverConfig.getLoginPwd());//会易通密码
                LOGGER.info("调用会易通获取TOKEN接口的URL是: {}, BODY参数是: {}", url, paramJSONObject);
                try {
                    //调用会易通登录接口
                    resultJSONObject = RestTemplateUtil.postForObjectByForm(url, getParamsMap(paramJSONObject), JSONObject.class);
                } catch (Exception e) {
                    LOGGER.error("调用会易通登录接口失败: 无法连接会易通服务器", e);
                    throw new BusinessException("获取失败: 无法连接会易通服务器");
                }
                if (resultJSONObject == null || resultJSONObject.size() == 0) {
                    LOGGER.error("获取Token信息失败: 会易通没有任何返回数据");
                    throw new BusinessException("获取失败: 会易通无数据返回");
                }
                if (StringUtils.isBlank(resultJSONObject.getString("hytToken"))) {
                    LOGGER.error("获取Token信息失败: 会易通没有任何返回hytToken数据, 会易通返回的数据为: {}",
                            resultJSONObject.toJSONString());
                    throw new BusinessException("获取失败: " + resultJSONObject.getString("msg"));
                }
                otherPlatformToken = resultJSONObject.getString("hytToken");//一机一档token
            } else {//平台异常
                LOGGER.warn("平台信息异常, 没有要获取的平台, 平台信息是: {}", JSONObject.toJSONString(serverConfig));
                throw new BusinessException("平台信息异常");
            }
            //设置缓存
            try {
                jRedisDao.set(StringUtil.getRedisKey(CommonConstrant.PUSH_USER_PREFIX_OTHER_PLATFORM,
                        otherPlatformId), otherPlatformToken);//永不失效
            } catch (Exception e) {
                LOGGER.error("Redis服务出现异常，请配置好Redis服务", e);
            }
        }
        /* 从对应平台获取token End */
        return otherPlatformToken;
    }

    /**
     * 获取其他平台的登录token
     * 1 先从Redis缓存里面查找是否有该token信息
     *  1.1 如果有直接返回，如果没有则登录对应的平台获取token再存储token
     * 2 如果Redis出现异常或者Redis中没有该缓存信息，则从对应平台登录获取token
     * @param otherPlatformId 其他平台的平台ID
     * @return 对应平台的token
     */
    @Override
    public String getOtherPlatformToken(String otherPlatformId) {
        return getOtherPlatformToken(otherPlatformId, null);
    }


    /**
     * 删除其他平台的登录token
     * @param otherPlatformId 其他平台的平台ID
     * @return true表示删除成功
     */
    @Override
    public boolean delOtherPlatformToken(String otherPlatformId) {
        boolean isSuccess = true;
        //删除缓存
        try {
            jRedisDao.del(StringUtil.getRedisKey(CommonConstrant.PUSH_USER_PREFIX_OTHER_PLATFORM, otherPlatformId));
        } catch (Exception e) {
            LOGGER.error("Redis服务出现异常，请配置好Redis服务", e);
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 向其他平台推送本平台的token信息
     *  1 查询该平台的配置信息
     * @param serverConfigId 平台的配置主键ID
     * @param token 本平台Token
     * @return
     */
    @Override
    public ReturnData pushToken(String serverConfigId, String token) {
        BaseReturn dataReturn = new BaseReturn();

        /* 查询服务配置信息 Start */
        final ServerConfig serverConfig = serverConfigService.getServerConfigById(serverConfigId);
        if (serverConfig == null) {
            return dataReturn.returnError("没有找到该服务");
        }
        /* 查询服务配置信息 End */

        /* 通过token查询用户信息 Start */
        ReturnData otherServiceData = new ReturnData();//调用其他Service返回的信息
        //去缓存中查询用户信息,但是缓存中不存在用户的密码,调用网管下发token的接口需要用户的密码,所以需要去数据库查询用户的密码
        otherServiceData = userService.getUserByToken(token);
        if (otherServiceData.getErrcode() != WsConstants.OK) {
            LOGGER.warn("查询用户信息失败, 失败原因: {}, 查询的token是: {}", otherServiceData.getErrmsg(), token);
            return dataReturn.returnError(otherServiceData.getErrmsg());
        }
        UserVO user = new UserVO();
        user = ReturnDataUtil.getExtraJsonObject(otherServiceData, UserVO.class);//缓存中的用户信息
        if (user == null) {
            LOGGER.warn("用户信息的缓存过期, 查询的token是: {}", token);
            return dataReturn.returnError("Token过期");
        }
        //查询用户密码
        final String pwd = userService.getUserPwdByUserId(user.getUuid());
        user.setLoginPwd(pwd);
        /* 通过token查询用户信息 End */

        /* 下发用户的Token信息给对应的平台 Start */
        Map<String, String> paramsMap = new HashMap();//传递接口的参数Map
        JSONObject resultJSONObject = new JSONObject();//接口返回的JSON
        String url = serverConfig.getUrl();//调用的URL,http://ip:port
        if (PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(serverConfig.getPlatformType())) {//网管平台
            paramsMap.clear();
            paramsMap.put("userid", user.getLoginName());//网管用户ID
            paramsMap.put("pwd", user.getLoginPwd());//网管用户密码
            paramsMap.put("token", token);//需要给网管下发的token
            url = url + WsConstants.NET_MANAGER_LOGIN_URL;//网管下发TOKEN接口
            LOGGER.info("开始调用网管下发TOKEN接口, 接口的URL是: {}, BODY参数是: {}", url, JSONObject.toJSONString(paramsMap));
            try {
                resultJSONObject = RestTemplateUtil.postForObject(url, paramsMap, JSONObject.class);
            } catch (Exception e) {
                LOGGER.error("调用网管接口失败, 请联系网管WEB管理人员排查错误", e);
                return dataReturn.returnError("无法连接网管服务器");
            }
            if (resultJSONObject == null || resultJSONObject.size() == 0 ||
                    resultJSONObject.getInteger("ret") == null) {
                LOGGER.warn("调用网管下发TOKEN接口失败, 网管返回结果为空, 网管的返回结果为: {}", resultJSONObject);
                return dataReturn.returnError("下发TOKEN失败");
            }
            if (!resultJSONObject.getInteger("ret").equals(0)) {//网管返回不为0表示失败
                LOGGER.warn("调用网管下发TOKEN接口失败, 网管的返回结果ret不为0, 网管返回结果为: {}", resultJSONObject);
                return dataReturn.returnError("下发TOKEN失败");
            }
            LOGGER.info("结束调用网管下发TOKEN接口, 网管返回结果是: {}", resultJSONObject);
        } else if (PlatformTypeConstrant.VSDC_PLATFORM_TYPE.equals(serverConfig.getPlatformType())) {//一机一档平台
            paramsMap.clear();
            paramsMap.put("uuid", user.getUuid());//一机一档用户ID
            paramsMap.put("phone", user.getPhone());//一机一档用户手机号
            paramsMap.put("access_token", token);//需要给一机一档下发的TOKEN
            url = url + WsConstants.VSDC_PUSH_TOKEN_URL;//一机一档下发TOKEN接口
            LOGGER.info("开始调用一机一档下发TOKEN接口, 接口的URL是: {}, BODY参数是: {}", url,
                    JSONObject.toJSONString(paramsMap));
            try {
                resultJSONObject = RestTemplateUtil.postForObjectByForm(url, paramsMap, JSONObject.class);
            } catch (Exception e) {
                LOGGER.error("调用一机一档接口失败, 请联系一机一档WEB管理人员排查错误", e);
                return dataReturn.returnError("无法连接一机一档服务器");
            }
            if (resultJSONObject == null || resultJSONObject.size() == 0 ||
                    resultJSONObject.getInteger("result") == null) {
                LOGGER.warn("调用一机一档下发TOKEN接口失败, 一机一档返回结果为空, 一机一档的返回结果为: {}", resultJSONObject);
                return dataReturn.returnError("下发TOKEN失败");
            }
            if (!resultJSONObject.getInteger("result").equals(0)) {//一机一档返回不为0表示失败
                LOGGER.warn("调用一机一档下发TOKEN接口失败, 一机一档的返回结果result不为0, 一机一档返回结果为: {}", resultJSONObject);
                return dataReturn.returnError("下发TOKEN失败");
            }
            LOGGER.info("结束调用一机一档下发TOKEN接口, 一机一档返回结果是: {}", resultJSONObject);
        } else if (PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE.equals(serverConfig.getPlatformType())) {//会易通平台
            paramsMap.clear();
            paramsMap.put("hytToken", getOtherPlatformToken(serverConfig.getOtherPlatformId()));//本地调用会易通的token
            paramsMap.put("phone", user.getPhone());//会易通用户手机号
            paramsMap.put("appToken", token);//需要给会易通下发的TOKEN
            url = url + WsConstants.HUIYITONG_PUSH_TOKEN_URL;//会易通下发TOKEN接口
            LOGGER.info("开始调用会易通下发TOKEN接口, 接口的URL是: {}, BODY参数是: {}", url, JSONObject.toJSONString(paramsMap));
            try {
                resultJSONObject = RestTemplateUtil.postForObjectByForm(url, paramsMap, JSONObject.class);
            } catch (Exception e) {
                LOGGER.error("调用会易通接口失败, 请联系会易通管理人员排查错误", e);
                return dataReturn.returnError("无法连接会易通服务器");
            }
            if (resultJSONObject == null || resultJSONObject.size() == 0 ||
                    resultJSONObject.getInteger("code") == null) {
                LOGGER.warn("调用会易通下发TOKEN接口失败, 会易通返回结果为空, 会易通的返回结果为: {}", resultJSONObject);
                return dataReturn.returnError("下发TOKEN失败");
            }
            if (!resultJSONObject.getInteger("code").equals(0)) {//会易通返回不为0表示失败
                LOGGER.warn("调用会易通下发TOKEN接口失败, 会易通的返回结果code不为0, 会易通返回结果为: {}", resultJSONObject);
                return dataReturn.returnError("下发TOKEN失败");
            }
            LOGGER.info("结束调用会易通下发TOKEN接口, 会易通返回结果是: {}", resultJSONObject);
        } else {
            LOGGER.error("该平台无法下发TOKEN, 平台信息是: {}", JSONObject.toJSONString(serverConfig));
            return dataReturn.returnError("平台错误");
        }
        /* 下发用户的Token信息给对应的平台 End */
        return dataReturn.returnResult(WsConstants.OK, "下发TOKEN成功");
    }

    /**
     * 将JSONObject转换成Map<String, String>
     * @param paramsJSONObject 参数JSONObject
     * @return Map<String, String>
     */
    private Map<String, String> getParamsMap(JSONObject paramsJSONObject) {
        Map<String, String> resultMap = new HashMap<>();
        Set<String> keySet = null;
        if (paramsJSONObject != null && paramsJSONObject.size() > 0) {
            keySet = paramsJSONObject.keySet();
        }
        if (keySet != null && keySet.size() > 0) {
            for (String key : keySet) {
                resultMap.put(key, paramsJSONObject.getString(key));
            }
        }
        return resultMap;
    }
}
