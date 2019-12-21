package com.visionvera.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.Function;
import com.visionvera.bean.cms.PhoneVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.RoleVO;
import com.visionvera.bean.cms.UserAccessTokenVO;
import com.visionvera.bean.cms.UserApproveVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.cms.WorkAreaVO;
import com.visionvera.bean.cms.WorkDepartVO;
import com.visionvera.bean.cms.WorkUnitVO;


public interface UserService {
	

	/**
	 * 
	 * @author 通讯录用户管理添加用户
	 * @param user
	 * @return
	 */
	int addUserBooks(UserVO user);
	/**
	 * 
	 * @author 用户管理获取用户信息
	 * @param paramsMap
	 * @return
	 */
	List<UserVO> getUserInfo(Map<String, Object> paramsMap);
	/**
	 * 
	 * @author 用户管理获取用户信息总条数
	 * @param paramsMap
	 * @return
	 */
	int getUserInfoCount(Map<String, Object> paramsMap);
	/**
	 * 
	 * @author 用户更新
	 * @param paramsMap
	 * @return
	 */
	int updateUserBooks(Map<String, Object> paramsMap);

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
	List<UserVO> getCustomizedUserList(Map<String, Object> paramsMap);

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
	int getCustomizedUserCount(Map<String, Object> paramsMap);


	/**
	 * 
	 * TODO 获取用户定制私有联系人列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getCustomizedDevList(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * TODO 获取用户定制私有联系人列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int getCustomizedDevCount(Map<String, Object> paramsMap);

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
	List<DeviceGroupVO> getCustomizedDevGroupList(Map<String, Object> paramsMap);

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
	int getCustomizedDevGroupCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取用户定制私有群成员列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getCustomizedGroupMembers(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * TODO 获取用户定制私有群成员列表总数
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int getCustomizedGroupMembersCount(Map<String, Object> paramsMap);
	
	/** <pre>getNameByLoginName(根据用户登录名获取真实名称)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年12月12日 下午7:16:52    
	 * 修改人：周逸芳        
	 * 修改时间：2017年12月12日 下午7:16:52    
	 * 修改备注： 
	 * @return</pre>    
	 */
	UserVO getNameByLoginName(String loginName);
	
	/**
	 * 
	 * @Title: deleteUser 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	Map<String, Object> deleteUser(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: addUserGroupDev 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param user
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addUserGroupDev(UserVO user);
	/**
	 * 
	 * @Title: updateUserGroupDev 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param user
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateUserGroupDev(UserVO user);
	
	/**
	 * 
	 * @Title: updateMaxDevNumByLoginName 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param user
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateMaxDevNumByLoginName(UserVO user);
	
	String selSession();

}
