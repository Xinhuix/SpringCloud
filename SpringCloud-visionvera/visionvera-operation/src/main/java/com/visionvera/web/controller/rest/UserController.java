package com.visionvera.web.controller.rest;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.common.api.operation.OperationUserAPI;
import com.visionvera.constrant.LogType;
import com.visionvera.service.UserService;
import com.visionvera.util.LogWritter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @ClassName: UserController
 * @Description: TODO(这里用一句话描述这个类的作用)
 */
@RestController
public class UserController extends BaseReturn implements OperationUserAPI {

	@Resource
	private UserService userService;

	private static final Logger logger = LogManager.getLogger(UserController.class);


	
	/**
	 * 
	 * @Title: deleteUser
	 * @Description: 删除用户
	 * @param @param userid
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("deleteUser.do")
	@ResponseBody
	public Map<String, Object> deleteUser(String uuid, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			String[] uuids = uuid.split(",");
			paramsMap.put("uuids", uuids);
			//session.removeAttribute("ticket");
			resultMap = userService.deleteUser(paramsMap);// 删除用户
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "删除用户失败");
			logger.error("删除用户失败，msg：", e);
		}
		/*if ((Boolean) resultMap.get("result")) {
			LogWritter.writeLog(LogType.DEL_USER, session, "删除用户：" + uuid, "",
					LogType.OPERATE_OK);
		} else {
			LogWritter.writeLog(LogType.DEL_USER, session, "删除用户：" + uuid, "",
					LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: getUserInfo
	 * @Description: 获取用户
	 * @param loginName
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@Override
	public UserVO getHgUserInfo(@PathVariable("loginName") String loginName, String token) {
		UserVO user =null;
		try {
			return userService.getNameByLoginName(loginName);
		} catch (Exception e) {
		}
		return user;
	}

	/**
	 * 
	 * @Title: addUserGroupDev
	 * @Description: 添加用户设备组关联关系
	 * @return int
	 * @throws
	 */
	@Override
	public int addUserGroupDev(@RequestBody UserVO user, String token) {
		try {
			return userService.addUserGroupDev(user);
		} catch (Exception e) {
			logger.error("UserController---添加用户组失败",e);
		}
		return 0;
	}

	/**
	 * 
	 * @Title: updateUserGroupDev
	 * @Description: 修改用户设备组关联关系
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@Override
	public int updateUserGroupDev(@RequestBody UserVO user, String token) {
		try {
			return userService.updateUserGroupDev(user);
		} catch (Exception e) {
			logger.error("UserController---修改用户组失败",e);
		}
		return 0;
	}
	
	/**
	 * 设置视联汇用户最大允许参会终端数
	 */
	public int updateMaxDevNumByLoginName(@RequestBody UserVO user, String token) {
		try {
			return userService.updateMaxDevNumByLoginName(user);
		} catch (Exception e) {
			logger.error("UserController---修改用户组失败",e);
		}
		return 0;
	}
	
	/**
	 * 
	 * @Title: getUserMaxDevNum
	 * @Description: 获取用户
	 * @param loginName
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value="/{loginName}/getUserMaxDevNum",method=RequestMethod.GET)
	public ReturnData getUserMaxDevNum(@PathVariable("loginName") String loginName) {
		try {
			UserVO user = userService.getNameByLoginName(loginName);
			if(user==null){
			   return super.returnError("用户不存在");
			}
			return	super.returnResult(0,"获取成功",null,null,user);
		} catch (Exception e) {
			 return super.returnError("系统异常");
		}
	}
	
	
	/** <pre>getSes(获取session过期时间)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年8月16日 下午3:43:54    
	 * 修改人：周逸芳        
	 * 修改时间：2017年8月16日 下午3:43:54    
	 * 修改备注： 
	 * @param sessionTime
	 * @return</pre>    
	 */
	@RequestMapping("getSes")
	public Map<String, Object> getSes(){
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			String sessionTime = userService.selSession();
			resultMap.put("sessionTime", sessionTime);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("修改session失效时间失败, msg: ", e);
		}
		return resultMap;
	}
	

}
