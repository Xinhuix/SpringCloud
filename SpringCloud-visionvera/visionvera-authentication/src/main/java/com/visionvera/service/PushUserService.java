package com.visionvera.service;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;

/**
 * 向其他平台推送用户的业务Interface
 */
public interface PushUserService {
    /***
     * 向其他平台推送添加用户的操作
     * @param user 用户信息
     * @param token 本地访问标识
     */
    void pushUserForAdd(UserVO user, String token);

    /**
     * 向其他平台推送修改用户的操作
     * @param user 用户信息
     * @param token 本地访问标识
     */
    void pushUserForEdit(UserVO user, String token);

    /**
     * 向其他平台推送删除用户的操作
     * @param user 用户信息
     * @param token 本地访问标识
     */
    void pushUserForDel(UserVO user, String token);

    /**
     * 获取其他平台的登录token
     * @param otherPlatformId 其他平台的平台ID
     * @param token 本平台的TOKEN。调用网管登录接口使用，调用其他平台暂可以传空
     * @return 对应平台的token
     */
    String getOtherPlatformToken(String otherPlatformId, String token);

    /**
     * 获取其他平台的登录token
     * @param otherPlatformId 其他平台的平台ID
     * @return 对应平台的token
     */
    String getOtherPlatformToken(String otherPlatformId);

    /**
     * 删除其他平台的登录token
     * @param otherPlatformId 其他平台的平台ID
     * @return true表示删除成功
     */
    boolean delOtherPlatformToken(String otherPlatformId);

    /**
     * 向其他平台推送本平台的token信息
     * @param serverConfigId 平台的配置主键ID
     * @param token 本平台Token
     * @return
     */
    ReturnData pushToken(String serverConfigId, String token);
}
