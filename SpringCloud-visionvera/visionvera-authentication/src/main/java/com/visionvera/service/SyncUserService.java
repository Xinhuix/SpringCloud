package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;

/**
 * 同步P-Server、网管、会易通、一机一档用户业务
 */
public interface SyncUserService {
    /**
     * P-Server添加用户
     * @param user 用户信息
     * @return
     */
    ReturnData addUserForPServer(UserVO user);

    /**
     * 通过多个手机号获取这些用户的基本信息
     * @param user {"phone":"13112341234,13412341234"} 多个手机号使用英文逗号","隔开
     * @param pageNum 页码
     * @param pageSize 页大小，为-1表示不分页
     * @return
     */
    PageInfo<UserVO> getUserForPServer(UserVO user, Integer pageNum, Integer pageSize);

    /**
     * P-Server根据用户手机号修改用户信息
     * @param user {"phone":"","loginPwd":"","areaId":"","areaName":""}
     * @return
     */
    ReturnData editUserForPServer(UserVO user);

    /**
     * 添加用户：提供给网管使用
     * 由于业务逻辑，网管删除的用户并不会真正的删除，所以如果存在该用户(通过用户名和手机号联合查询表示该用户存在)则更新
     * @param user 用户信息
     * @return
     */
    ReturnData addUserForNetManager(UserVO user);

    /**
     * 修改用户：提供给网管使用
     * @param user 用户信息
     * @return
     */
    ReturnData editUserForNetManager(UserVO user);

    /**
     * 删除用户：提供给网管使用
     * @param user 用户信息
     * @return
     */
    ReturnData delUserForNetManager(UserVO user);

    /**
     * 添加用户：提供给会易通使用
     * 由于业务逻辑，会易通删除的用户并不会真正的删除，所以如果存在该用户(通过用户名和手机号联合查询表示该用户存在)则更新
     * @param user 用户信息
     * @return
     */
    ReturnData addUserForHYT(UserVO user);

    /**
     * 修改用户：提供给会易通使用
     * @param user 用户信息
     * @return
     */
    ReturnData editUserForHYT(UserVO user);

    /**
     * 删除用户：提供给会易通使用
     * @param user 用户信息
     * @return
     */
    ReturnData delUserForHYT(UserVO user);

    /**
     * 添加用户：提供给一机一档使用
     * 由于业务逻辑，一机一档删除的用户并不会真正的删除，所以如果存在该用户(通过用户名和手机号联合查询表示该用户存在)则更新
     * @param user 用户信息
     * @return
     */
    ReturnData addUserForVSDC(UserVO user);

    /**
     * 修改用户：提供给一机一档使用
     * @param user 用户信息
     * @return
     */
    ReturnData editUserForVSDC(UserVO user);

    /**
     * 删除用户：提供给一机一档使用
     * @param user 用户信息
     * @return
     */
    ReturnData delUserForVSDC(UserVO user);

    /**
     * 同步网管用户信息
     * @param token 访问令牌
     * @param otherPlatformId 平台ID
     * @param platformType 平台类别
     * @return
     */
    ReturnData syncNetManagerUser(String token, String otherPlatformId, String platformType);

    /**
     * 同步一机一档用户信息
     * @param token 访问令牌
     * @param otherPlatformId 平台ID
     * @param platformType 平台类别
     * @return
     */
    ReturnData syncVSDCUser(String token, String otherPlatformId, String platformType);

    /**
     * 同步会易通用户信息
     * @param token 访问令牌
     * @param otherPlatformId 平台ID
     * @param platformType 平台类别
     * @return
     */
    ReturnData syncHYTUser(String token, String otherPlatformId, String platformType);
}
