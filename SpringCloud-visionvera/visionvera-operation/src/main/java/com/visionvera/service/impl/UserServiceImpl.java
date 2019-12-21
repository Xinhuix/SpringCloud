package com.visionvera.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.operation.UserDao;
import com.visionvera.service.UserService;

/**
 * 
 * @ClassName: UserServiceImpl
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author zhaolei
 * @date 2016年8月12日 下午3:22:54
 * 
 */
@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

	
	@Autowired
	private UserDao userDao;
	
	private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
	
	
	

	/**
	 * 
	 * @Title: addUserBooks 
	 * @Description: 通讯录新增用户接口
	 * @param @param user
	 * @param @return  参数说明 
	 * @return void    返回类型 
	 * @throws
	 */
	public int addUserBooks(UserVO user) {
		int rlt = 1;
		if (user == null)
			return 0;
		try {
			rlt = userDao.addUserBooks(user);
			if (rlt != 0) {
				//将新增的用户信息插入OA通讯录中
				if(userDao.getOAUser(user).size() < 1){//不存在则插入OA通讯录
					rlt = userDao.addOAUser(user);
				}
				Map<String, Object> paramsMap = new HashMap<String, Object>();
				//新增用户角色关联
				if(StringUtils.isNotBlank(user.getRole()))
				{
					paramsMap.put("userid", user.getUuid());
					paramsMap.put("roleid", user.getRole());
					rlt = userDao.addUserRole(paramsMap);
				}
				UserGroupVO userGroupVO = new UserGroupVO();
				userGroupVO.setName(user.getLoginName());
				userDao.addUserGroup(userGroupVO);
				//新增用户组和用户的关联
				user.setGroupId(userGroupVO.getUuid());
				if(StringUtils.isNotBlank(user.getGroupId()))
				{
					String[] uuids = user.getUuid().split(",");
					if (uuids.length > 0) {
						paramsMap.put("groupuuid", user.getGroupId());
						paramsMap.put("useruuids", uuids);
						rlt = userDao.addUser2Group(paramsMap);
					}
				}
				//用户设备关联
				if(StringUtils.isNotBlank(user.getDeviceIds()))
				{
					String[] devIds = user.getDeviceIds().split(",");
					if (devIds.length > 0) {
						paramsMap.put("uuid", user.getUuid());
						paramsMap.put("devIds", devIds);
						paramsMap.put("type", user.getType());
						rlt = userDao.addUserDevice(paramsMap);
					}
				}
				//用户设备组关联
				if(StringUtils.isNotBlank(user.getDevGroupIds()))
				{
					String[] devGroupIds = user.getDevGroupIds().split(",");
					if (devGroupIds.length > 0) {
						paramsMap.put("uuisd", user.getUuid());
						paramsMap.put("devGroupIds", devGroupIds);
						rlt = userDao.addUserDeviceGroup(paramsMap);
					}
				}
			}
		}catch (Exception e) {
			logger.error("新增用户失败", e);
			rlt = 0;
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
	return rlt;	
	}

	/**
	 * 
	 * @Title: getUserInfo 
	 * @Description: 通讯录获取用户信息接口
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return List<UserVO>    返回类型 
	 * @throws
	 */
	public List<UserVO> getUserInfo(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return userDao.getUserInfo(map, new RowBounds());
		}
		return userDao.getUserInfo(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: getUserInfoCount 
	 * @Description: 通讯录获取用户信息总条数
	 * @param @param paramsMap
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int getUserInfoCount(Map<String, Object> paramsMap) {
		return userDao.getUserInfoCount(paramsMap);
	}

	public int updateUserBooks(Map<String, Object> paramsMap) {
		int rlt = 0;
		if(null == paramsMap)
			return 0;
		try {
			if(paramsMap.get("allowHkzs") != null && "0".equals(paramsMap.get("allowHkzs").toString())){
				paramsMap.put("userId", paramsMap.get("uuid"));
				paramsMap.put("clientType", 4);//4代表会控助手
				userDao.delAccessToken(paramsMap);//禁止登录会控助手
			}
			rlt = userDao.updateUserBooks(paramsMap);
			//更新用户和设备关联
			if(StringUtils.isNotBlank(paramsMap.get("devIds").toString()))
			{
				rlt = userDao.deleteUserDevice(paramsMap);
				rlt = userDao.addUserDevice(paramsMap);
			}
			if(StringUtils.isNotBlank(paramsMap.get("devGroupIds").toString()))
			{
				rlt = userDao.deleteUserDeviceGroup(paramsMap);
				rlt = userDao.addUserDeviceGroup(paramsMap);
			}
			
		} catch (Exception e) {
			logger.error("更新失败", e);
			rlt = 0;
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return rlt;
	}
	
	/**
	 * 
	 * TODO 获取用户定制列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	public List<UserVO> getCustomizedUserList(Map<String, Object> map) {
		
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return userDao.getCustomizedUserList(map, new RowBounds());
		}
		return userDao.getCustomizedUserList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * TODO 获取用户定制列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	public int getCustomizedUserCount(Map<String, Object> paramsMap) {
		return userDao.getCustomizedUserCount(paramsMap);
	}
	
	/**
	 * 
	 * TODO 获取用户定制私有联系人列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceVO> getCustomizedDevList(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return userDao.getCustomizedDevList(map, new RowBounds());
		}
		return userDao.getCustomizedDevList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	
	/**
	 * 
	 * TODO 获取用户定制私有联系人列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public int getCustomizedDevCount(Map<String, Object> paramsMap) {
		return userDao.getCustomizedDevCount(paramsMap);
	}

	/**
	 * 
	 * TODO 获取用户定制私有群列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	public List<DeviceGroupVO> getCustomizedDevGroupList(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return userDao.getCustomizedDevGroupList(map, new RowBounds());
		}
		return userDao.getCustomizedDevGroupList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * TODO 获取用户定制私有群列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	public int getCustomizedDevGroupCount(Map<String, Object> paramsMap) {
		return userDao.getCustomizedDevGroupCount(paramsMap);
	}

	/**
	 * 
	 * TODO 获取用户定制私有群成员列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceVO> getCustomizedGroupMembers(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return userDao.getCustomizedGroupMembers(map, new RowBounds());
		}
		return userDao.getCustomizedGroupMembers(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	
	/**
	 * 
	 * TODO 获取用户定制私有群成员列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public int getCustomizedGroupMembersCount(Map<String, Object> paramsMap) {
		return userDao.getCustomizedGroupMembersCount(paramsMap);
	}
	
	/** <pre>getNameByLoginName(根据用户登录名获取真实名称)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年12月12日 下午7:16:52    
	 * 修改人：周逸芳        
	 * 修改时间：2017年12月12日 下午7:16:52    
	 * 修改备注： 
	 * @return</pre>    
	 */
	public UserVO getNameByLoginName(String loginName) {
		return userDao.getNameByLoginName(loginName);
	}

		/**
		 * 
		 * Title: deleteUser Description: 删除用户
		 * 
		 * @param map
		 * @return
		 * @see com.visionvera.cms.service.UserService#deleteUser(java.util.Map)
		 */
		public Map<String, Object> deleteUser(Map<String, Object> paramsMap) {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("result", true);
			resultMap.put("msg", "删除用户成功");
			try{
				paramsMap.put("adminName", GlobalConstants.ADMIN_LOGIN_NAME);//超级管理员的登录名（admin)
				if(userDao.isAdmin(paramsMap) > 0){//判断是否包含admin用户
					resultMap.put("result", false);
					resultMap.put("msg", "admin不允许删除");
					return resultMap;
				}
				
				List<UserVO> users = userDao.isUserOnline(paramsMap);//判断当前用户是否在线
				if(users.size() > 0){
					resultMap.put("result", false);
					resultMap.put("msg", users.get(0).getLoginName() + "用户在线，不允许删除");
					return resultMap;
				}
				userDao.deleteUser(paramsMap);
			}catch (Exception e) {
				resultMap.put("result", false);
				resultMap.put("msg", "删除用户失败");
				logger.error("删除用户失败", e);
				throw new RuntimeException("运行时出错！");//为了使事务回滚
			}
			return resultMap;
		}

		@Override
		public int addUserGroupDev(UserVO user) {
			int rlt = 0;
			int udCount=0;
			int udgCount=0;
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			//用户设备关联
			if(StringUtils.isNotBlank(user.getDeviceIds()))
			{
				String[] devIds = user.getDeviceIds().split(",");
				if (devIds.length > 0) {
					paramsMap.put("uuid", user.getUuid());
					paramsMap.put("devIds", devIds);
					paramsMap.put("type", user.getType());
					udCount = userDao.addUserDevice(paramsMap);
				}
			}
			//用户设备组关联
			if(StringUtils.isNotBlank(user.getDevGroupIds()))
			{
				String[] devGroupIds = user.getDevGroupIds().split(",");
				if (devGroupIds.length > 0) {
					paramsMap.put("uuid", user.getUuid());
					paramsMap.put("devGroupIds", devGroupIds);
					udgCount = userDao.addUserDeviceGroup(paramsMap);
				}
			}
//			//更新用户最大参会数
//			userDao.updateMaxDevNumByLoginName(user);
			if(udCount!=0&&udgCount!=0){
				rlt =1;
			}
			return rlt;
		}

		@Override
		public int updateUserGroupDev(UserVO user) {
			int rlt = 0;
			int udCount=0;
			int udgCount=0;
			int maxCount =0;
			Map<String, Object> paramsMap = new HashMap<String, Object>();
//			//更新用户最大参会数
//			maxCount = userDao.updateMaxDevNumByLoginName(user);
			
			//先删除用户和设备关联
			paramsMap.put("uuid", user.getUuid());
			userDao.deleteUserDevice(paramsMap);
			//用户设备关联
			if(StringUtils.isNotBlank(user.getDeviceIds()))
			{
				String[] devIds = user.getDeviceIds().split(",");
				if (devIds.length > 0) {
					paramsMap.put("devIds", devIds);
					paramsMap.put("type", user.getType());
					udCount = userDao.addUserDevice(paramsMap);
				}
			}
			
			//先删除用户和群组关联
			paramsMap.put("uuid", user.getUuid());
			userDao.deleteUserDeviceGroup(paramsMap);
			//用户设备组关联
			if(StringUtils.isNotBlank(user.getDevGroupIds()))
			{
				String[] devGroupIds = user.getDevGroupIds().split(",");
				if (devGroupIds.length > 0) {
					paramsMap.clear();
					paramsMap.put("uuid", user.getUuid());
					paramsMap.put("devGroupIds", devGroupIds);
					udgCount = userDao.addUserDeviceGroup(paramsMap);
				}
			}
			
			if(udCount!=0&&udgCount!=0&&maxCount!=0){
				rlt =1;
			}
			return rlt;
		}
		
		/**
		 * 设置视联汇用户最大允许参会终端数
		 */
		@Override
		public int updateMaxDevNumByLoginName(UserVO user) {
			//更新用户最大参会数
			return userDao.updateMaxDevNumByLoginName(user);
		}
	
		public String selSession() {
			return userDao.selSession();
		}
}
