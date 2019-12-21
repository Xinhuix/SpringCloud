package com.visionvera.service;

import com.visionvera.bean.base.ReturnData;

/**
 * @author Administrator
 * @date 2018年11月23日 16:51
 */
public interface ControlService {
    ReturnData controlPhone(String platformId, String uniqId, String msg, String loginName, String loginPwd);

    /**
     * 业务控制接口
     * @param platformId 业务平台id
     * @param uniqId 业务唯一id
     * @param msg 内容
     * @return
     */
//    ReturnData control(String platformId, String uniqId, String msg, String loginName, String loginPwd);
    /**
     * 流媒体停会接口
     * @param platformId 业务平台id
     * @param uniqId 业务唯一id
     * @param msg 内容
     * @return
     */
    ReturnData stopMeeting(String platformId, String uuid, String loginName, String password, String msg);
}
