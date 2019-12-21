package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.cms.UserVO;

public interface SyncLmtUserService {

	/**
	 * 用户注册
	 * @param user 
	 * @return
	 */
	public Map<String,Object> lmtUserAdd(UserVO user);
	/**
	 * 修改用户
	 * @param user 
	 * @return
	 */
	public Map<String,Object> lmtUpdateUser(UserVO user);
	/**
	 * 删除用户
	 * @param user 
	 * @return
	 */
	public Map<String,Object> lmtDeleteUser(List<UserVO> list);
	/**
	 * 用户登录
	 * @param user 
	 * @return
	 */
	public Map<String,Object> lmtLoginUser(UserVO user);
}
