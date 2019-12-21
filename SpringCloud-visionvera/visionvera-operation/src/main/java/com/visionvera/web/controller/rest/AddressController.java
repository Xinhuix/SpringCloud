package com.visionvera.web.controller.rest;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.LogType;
import com.visionvera.service.DeviceService;
import com.visionvera.service.SysConfigService;
import com.visionvera.service.UserService;
import com.visionvera.util.LogWritter;

/**
 * 
 * @ClassName: SyncDataController
 * @Description: TODO 多级系统数据同步管理
 * @author xiechs
 * @date 2017年9月28日 上午11:28:27
 * 
 */
@Controller
@RequestMapping("/rest/address")
public class AddressController {

	@Resource
	private DeviceService deviceService;
	@Resource
	private SysConfigService sysConfigService;
	
	@Resource
	private UserService userService;
	
	private static final Logger logger = LogManager.getLogger(AddressController.class);

	/**
	 * 
	 * @Title: resourceManagement
	 * @Description: TODO(资源管理跳转页面)
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("resourceManagement")
	public String resourceManagement(ModelMap map) {
		return "/addressList/resourceManagement";
	}
	/**
	 * 
	 * @Title: groupManagement
	 * @Description: TODO(群组管理跳转页面)
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("groupManagement")
	public String groupManagement(ModelMap map) {
		return "/addressList/groupManagement";
	}
	/**
	 * 
	 * @Title: userManagement
	 * @Description: TODO(用户管理跳转页面)
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("userManagement")
	public String userManagement(ModelMap map) {
		return "/addressList/userManagement";
	}
	/**
	 * 
	 * @Title: userCustomized
	 * @Description: TODO(用户定制跳转页面)
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("userCustomized")
	public String userCustomized(ModelMap map) {
		return "/addressList/userCustomized";
	}
	
	/** <pre>getDeviceListBooks(通讯录资源管理获取设备列表)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月17日 下午3:15:51    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月17日 下午3:15:51    
	 * 修改备注： 
	 * @param pageNum
	 * @param pageSize
	 * @param id
	 * @param name
	 * @return</pre>    
	 */
	@RequestMapping("getDeviceListBooks")
	@ResponseBody
	public Map<String, Object> getDeviceListBooks(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String key,String name,String id) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("id", id);
			if (StringUtils.isNotBlank(key)) {
				paramsMap.put("key", URLDecoder.decode(key, "utf-8"));
			}
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			List<DeviceVO> list = deviceService.getDeviceListBooks(paramsMap);
			if(pageSize != -1){
				int total = deviceService.getDeviceListBooksCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
			resultMap.put("msg", "获取数据成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取通讯录用户信息失败，msg：", e);
		}
		return resultMap;
	}
	
	
	/** <pre>getDeviceGroupBooks(用户管理页面初始群授权获取)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月17日 下午4:59:22    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月17日 下午4:59:22    
	 * 修改备注： 
	 * @param pageNum
	 * @param pageSize
	 * @param name
	 * @return</pre>    
	 */
	@RequestMapping("getDeviceGroupBooks")
	@ResponseBody
	public Map<String, Object> getDeviceGroupBooks(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String name) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			List<DeviceGroupVO> list = deviceService.getDeviceGroupBooks(paramsMap);
			if(pageSize != -1){
				int total = deviceService.getDeviceGroupBooksCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取通讯录用户信息失败，msg：", e);
			e.printStackTrace();
		}
		return resultMap;
	}
	
	
	/** <pre>addUserBooks(通讯录添加用户)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月17日 下午6:28:50    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月17日 下午6:28:50    
	 * 修改备注： 
	 * @param user
	 * @return</pre>    
	 */
	@RequestMapping("addUserBooks")
	@ResponseBody
	public Map<String, Object> addUserBooks(UserVO user, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (null == user) {
				resultMap.put("result", true);
				resultMap.put("msg", "参数为空");
				return resultMap;
			}
			if (StringUtils.isBlank(user.getDeviceIds())) {//所有设备
				resultMap.put("result", true);
				resultMap.put("msg", "联系人不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(user.getLoginName())) {
				resultMap.put("result", true);
				resultMap.put("msg", "用户姓名不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(user.getDevGroupIds())) {//所有群
				resultMap.put("result", true);
				resultMap.put("msg", "群不能为空");
				return resultMap;
			}
			if (StringUtils.isNotBlank(user.getName())) {
				user.setName(URLDecoder.decode(user.getName(), "utf-8"));
			}
			if (StringUtils.isNotBlank(user.getLoginName())) {
				user.setLoginName(URLDecoder.decode(user.getLoginName(),
						"utf-8"));
			}
			if (StringUtils.isNotBlank(user.getNickName())) {
				user.setNickName(URLDecoder.decode(user.getNickName(),
						"utf-8"));
			}
			if (StringUtils.isNotBlank(user.getAddress())) {
				user.setAddress(URLDecoder.decode(user.getAddress(),
						"utf-8"));
			}
			//设置App端添加用户标志位 1会控助手，2其他
//			user.setType(1);
			user.setIsvalid(1);//通讯录注册用户默认生效
//			user.setWebLogin(1);//通讯录注册用户允许登录会管
			user.setRole("40288a56565993dc01565993f9680002");//通讯录注册用户默认角色为预约操作员
			int result = userService.addUserBooks(user);
			if (result<1) {
				resultMap.put("result", false);
			}
			resultMap.put("result", true);
			resultMap.put("msg", "操作成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取通讯录用户信息失败，msg：", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.ADD_USER, session, "新增通讯录用户："+user.getLoginName(), "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.ADD_USER, session, "新增通讯录用户："+user.getLoginName(), "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}
	
	
	
	/** <pre>getUserInfo(用户管理获取用户信息)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月18日 下午2:55:44    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月18日 下午2:55:44    
	 * 修改备注： 
	 * @param pageNum
	 * @param pageSize
	 * @param loginName
	 * @param name
	 * @param devId
	 * @return</pre>    
	 */
	@RequestMapping("getUserInfo")
	@ResponseBody
	public Map<String, Object> getUserInfo(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String loginName,String name, String devId) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			if (StringUtils.isNotBlank(loginName)) {
				paramsMap.put("loginName", (URLDecoder.decode(loginName, "utf-8")));
			}
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", (URLDecoder .decode(name, "utf-8")));
			}
			if (StringUtils.isNotBlank(devId)) {
				paramsMap.put("devId", (URLDecoder.decode(devId, "utf-8")));
			}
			List<UserVO> list = userService.getUserInfo(paramsMap);
			if(pageSize != -1){
				int total = userService.getUserInfoCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
			resultMap.put("msg", "操作成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取通讯录用户信息失败，msg：", e);
		}
		return resultMap;
	}
	
	

	/** <pre>updateUserBooks(用户管理更新用户)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月18日 下午2:57:01    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月18日 下午2:57:01    
	 * 修改备注： 
	 * @param user
	 * @return</pre>    
	 */
	@RequestMapping("updateUserBooks")
	@ResponseBody
	public Map<String, Object> updateUserBooks(@ModelAttribute UserVO user,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (null == user) {
				resultMap.put("result", true);
				resultMap.put("msg", "参数为空");
				return resultMap;
			}
			if (StringUtils.isBlank(user.getDeviceIds())) {//所有设备
				resultMap.put("result", true);
				resultMap.put("msg", "联系人不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(user.getLoginName())) {
				resultMap.put("result", true);
				resultMap.put("msg", "用户账号不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(user.getDevGroupIds())) {//所有群
				resultMap.put("result", true);
				resultMap.put("msg", "群不能为空");
				return resultMap;
			}
//			user.setType(1);
			paramsMap.put("uuid", user.getUuid());
//			paramsMap.put("type", user.getType());
			paramsMap.put("maxDevNum", user.getMaxDevNum());
			paramsMap.put("webLogin", user.getWebLogin());
			paramsMap.put("allowHkzs", user.getAllowHkzs());
			if (StringUtils.isNotBlank(user.getNickName())) {
				paramsMap.put("nickName",(URLDecoder.decode(user.getNickName(), "utf-8")));
			}
			if (StringUtils.isNotBlank(user.getDevId())) {
				paramsMap.put("devId",(URLDecoder.decode(user.getDevId(), "utf-8")));
			}
			if (StringUtils.isNotBlank(user.getAddress())) {
				paramsMap.put("address",(URLDecoder.decode(user.getAddress(), "utf-8")));
			}
			if (StringUtils.isNotBlank(user.getLoginPwd())) {
				paramsMap.put("loginPwd",(URLDecoder.decode(user.getLoginPwd(), "utf-8")));
			}
			if (StringUtils.isNotBlank(user.getLoginName())) {
				paramsMap.put("loginName",(URLDecoder.decode(user.getLoginName(), "utf-8")));
			}
			if(StringUtils.isNotBlank(user.getDeviceIds()))
			{
				String[] devIds = user.getDeviceIds().split(",");
				if (devIds.length > 0) {
					paramsMap.put("devIds", devIds);
				}
			}
			if(StringUtils.isNotBlank(user.getDevGroupIds()))
			{
				String[] devGroupIds = user.getDevGroupIds().split(",");
				if (devGroupIds.length > 0) {
					paramsMap.put("devGroupIds", devGroupIds);
				}
			}
			int result = userService.updateUserBooks(paramsMap);
			if (result > 0) {
				resultMap.put("result", true);
				resultMap.put("msg", "操作成功");
			} else {
				resultMap.put("result", false);
				resultMap.put("msg", "更新失败");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("更新用户失败，msg：", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.EDIT_USER, session, "修改通讯录用户："+user.getLoginName(), "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.EDIT_USER, session, "修改通讯录用户："+user.getLoginName(), "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}
	
	
	/** <pre>getDevgroup(群组管理获取群信息)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月18日 下午5:49:34    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月18日 下午5:49:34    
	 * 修改备注： 
	 * @param pageNum
	 * @param pageSize
	 * @param groupName
	 * @return</pre>    
	 */
	@RequestMapping("getDevgroup")
	@ResponseBody
	public Map<String, Object> getDevgroup(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String groupName) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			if (StringUtils.isNotBlank(groupName)) {
				paramsMap.put("name", (URLDecoder.decode(groupName, "utf-8")));
			}
			List<UserVO> list = deviceService.getDevgroup(paramsMap);
			if(pageSize != -1){
				int total = deviceService.getDevgroupCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
			resultMap.put("msg", "操作成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取通讯录用户信息失败，msg：", e);
		}
		return resultMap;
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
	@RequestMapping("customizedUserList")
	@ResponseBody
	public Map<String, Object> getCustomizedUserList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@ModelAttribute UserVO user,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (null != user) {
				paramsMap.put("pageNum", pageSize * (pageNum - 1));
				paramsMap.put("pageSize", pageSize);
				paramsMap.put("uuid", user.getUuid());
				if (StringUtils.isNotBlank(user.getLoginName())) {//登录名
					paramsMap.put("loginName",(URLDecoder.decode(user.getLoginName(), "utf-8")));
				}
				if (StringUtils.isNotBlank(user.getName())) {//姓名
					paramsMap.put("name",(URLDecoder.decode(user.getName(), "utf-8")));
				}
				List<UserVO> list = userService.getCustomizedUserList(paramsMap);
				if(pageSize != -1){
					int total = userService.getCustomizedUserCount(paramsMap);
					total = total % pageSize == 0 ? total / pageSize
							: (total / pageSize) + 1;
					resultMap.put("pageNum", pageNum);
					resultMap.put("pageTotal", total);
				}
				resultMap.put("list", list);
				resultMap.put("result", true);
				resultMap.put("msg", "获取数据成功");
			} else {
				resultMap.put("result", false);
				resultMap.put("msg", "参数为空");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取定制用户列表失败，msg：", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 获取用户定制私有联系人列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	@RequestMapping("customizedDevList")
	@ResponseBody
	public Map<String, Object> getCustomizedDevList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@ModelAttribute UserVO user,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (null != user) {
				paramsMap.put("pageNum", pageSize * (pageNum - 1));
				paramsMap.put("pageSize", pageSize);
				paramsMap.put("userId", user.getUuid());//用户ID
				List<DeviceVO> list = userService.getCustomizedDevList(paramsMap);
				if(pageSize != -1){
					int total = userService.getCustomizedDevCount(paramsMap);
					total = total % pageSize == 0 ? total / pageSize
							: (total / pageSize) + 1;
					resultMap.put("pageNum", pageNum);
					resultMap.put("pageTotal", total);
				}
				resultMap.put("list", list);
				resultMap.put("result", true);
				resultMap.put("msg", "获取数据成功");
			} else {
				resultMap.put("result", false);
				resultMap.put("msg", "参数为空");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取用户定制私有联系人失败，msg：", e);
		}
		return resultMap;
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
	@RequestMapping("customizedDevGroupList")
	@ResponseBody
	public Map<String, Object> getCustomizedDevGroupList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@ModelAttribute UserVO user,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (null != user) {
				paramsMap.put("pageNum", pageSize * (pageNum - 1));
				paramsMap.put("pageSize", pageSize);
				paramsMap.put("userId", user.getUuid());//用户ID
				List<DeviceGroupVO> list = userService.getCustomizedDevGroupList(paramsMap);
				if(pageSize != -1){
					int total = userService.getCustomizedDevGroupCount(paramsMap);
					total = total % pageSize == 0 ? total / pageSize
							: (total / pageSize) + 1;
					resultMap.put("pageNum", pageNum);
					resultMap.put("pageTotal", total);
				}
				resultMap.put("list", list);
				resultMap.put("result", true);
				resultMap.put("msg", "获取数据成功");
			} else {
				resultMap.put("result", false);
				resultMap.put("msg", "参数为空");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取用户定制私有群失败，msg：", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 获取用户定制私有群成员列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param user
	 * @param session
	 * @return
	 */
	@RequestMapping("customizedGroupMembers")
	@ResponseBody
	public Map<String, Object> getCustomizedGroupMembers(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@ModelAttribute DeviceGroupVO dgv,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (null != dgv) {
				paramsMap.put("pageNum", pageSize * (pageNum - 1));
				paramsMap.put("pageSize", pageSize);
				paramsMap.put("groupId", dgv.getUuid());//群ID
				List<DeviceVO> list = userService.getCustomizedGroupMembers(paramsMap);
				if(pageSize != -1){
					int total = userService.getCustomizedGroupMembersCount(paramsMap);
					total = total % pageSize == 0 ? total / pageSize
							: (total / pageSize) + 1;
					resultMap.put("pageNum", pageNum);
					resultMap.put("pageTotal", total);
				}
				resultMap.put("list", list);
				resultMap.put("result", true);
				resultMap.put("msg", "获取数据成功");
			} else {
				resultMap.put("result", false);
				resultMap.put("msg", "参数为空");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取用户定制私有群失败，msg：", e);
		}
		return resultMap;
	}
	
	
	/** <pre>getSelectedGroup(获取已选群组)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月18日 下午8:02:51    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月18日 下午8:02:51    
	 * 修改备注： 
	 * @param devGroup
	 * @param session
	 * @return</pre>    
	 */
	@RequestMapping("getSelectedGroup")
	@ResponseBody
	public Map<String, Object> getSelectedGroup(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String loginName,String name,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			UserVO user =userService.getNameByLoginName(loginName);
			if(user==null||StringUtils.isEmpty(user.getUuid())){
				resultMap.put("result", false);
				resultMap.put("msg", "会管上不存在该用户！");
				return resultMap;
			}
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("dataType", 1);
			paramsMap.put("userId", user.getUuid());
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			List<DeviceGroupVO> list = deviceService.getUserDevGroups(paramsMap);
			if(pageSize != -1){
				int total = deviceService.getUserDevGroupsCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
			resultMap.put("msg", "获取数据成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取用户定制私有群失败，msg：", e);
			e.printStackTrace();
		}
		return resultMap;
	}
	
	
	
	/** <pre>getSelectedDev(获取用户已选设备列表)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年11月3日 上午9:36:35    
	 * 修改人：周逸芳        
	 * 修改时间：2017年11月3日 上午9:36:35    
	 * 修改备注： 
	 * @param pageNum
	 * @param pageSize
	 * @param userId
	 * @param key
	 * @param session
	 * @return</pre>    
	 */
	@RequestMapping("getSelectedDev")
	@ResponseBody
	public Map<String, Object> getSelectedDev(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String loginName,String id,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			UserVO user =userService.getNameByLoginName(loginName);
			if(user==null||StringUtils.isEmpty(user.getUuid())){
				resultMap.put("result", false);
				resultMap.put("msg", "用户不存在！");
				return resultMap;
			}
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("userId", user.getUuid());
			if (StringUtils.isNotBlank(id)) {
				paramsMap.put("id", URLDecoder.decode(id, "utf-8"));
			}
			List<DeviceVO> list = deviceService.getUserDev(paramsMap);
			if(pageSize != -1){
				int total = deviceService.getUserDevCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
			resultMap.put("msg", "获取数据成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("获取用户定制私有群失败，msg：", e);
			e.printStackTrace();
		}
		return resultMap;
	}
	/** <pre>addDevGroupBooks(群组管理新增群组)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月18日 下午8:02:51    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月18日 下午8:02:51    
	 * 修改备注： 
	 * @param devGroup
	 * @param session
	 * @return</pre>    
	 */
	@RequestMapping("addDevGroupBooks")
	@ResponseBody
	public Map<String, Object> addDevGroupBooks(@ModelAttribute DeviceGroupVO devGroup,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (null == devGroup) {
				resultMap.put("result", false);
				resultMap.put("msg", "参数不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(devGroup.getMasterNo())) {//主席号码
				resultMap.put("result", false);
				resultMap.put("msg", "主席不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(devGroup.getDevices())) {//所有设备
				resultMap.put("result", false);
				resultMap.put("msg", "群成员不能为空");
				return resultMap;
			}
			if (StringUtils.isNotBlank(devGroup.getName())) {//群名称
				paramsMap.put("name",(URLDecoder.decode(devGroup.getName(), "utf-8")));
			}
			if(deviceService.checkRepeatGroup(paramsMap) > 0){
				resultMap.put("result", false);
				resultMap.put("msg", "群名称已存在");
				return resultMap;
			}
			paramsMap.put("devices",(URLDecoder.decode(devGroup.getDevices(), "utf-8")));
			paramsMap.put("masterNo", devGroup.getMasterNo());//主席
			if (StringUtils.isNotBlank(devGroup.getSpeakerOne())) {
				paramsMap.put("speakerOne",(URLDecoder.decode( devGroup.getSpeakerOne(), "utf-8")));
			}
			if (StringUtils.isNotBlank(devGroup.getSpeakerTwo())) {
				paramsMap.put("speakerTwo",(URLDecoder.decode(devGroup.getSpeakerTwo(), "utf-8")));
			}
			paramsMap.put("speakerOne", devGroup.getSpeakerOne());//发言人1
				paramsMap.put("speakerTwo", devGroup.getSpeakerTwo());//发言人2
			paramsMap.put("type", 1);//数据属性1默认2私有
			int result = deviceService.addDevGroupBooks(paramsMap);
			if (result > 0) {
				resultMap.put("result", true);
				resultMap.put("msg", "新增群组成功");
			} else {
				resultMap.put("result", false);
				resultMap.put("msg", "新增群组失败");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("新增群组失败，msg：", e);
			e.printStackTrace();
		}
		return resultMap;
	}
	
	
	
	/** <pre>deleteDeviceGroup(删除群组操作)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月19日 上午11:05:29    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月19日 上午11:05:29    
	 * 修改备注： 
	 * @param devGroupId
	 * @param session
	 * @return</pre>    
	 */
	@RequestMapping("deleteDevGroup")
	@ResponseBody
	public Map<String, Object> deleteDeviceGroup( String devGroupId, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if(StringUtils.isBlank(devGroupId)){
				resultMap.put("result", false);
				resultMap.put("msg", "参数为空");
				return resultMap;
			}
			String[] uuids = devGroupId.split(",");
			paramsMap.put("uuids", uuids);
			int rlt= deviceService.deleteDevGroup(paramsMap);
			if (rlt > 0) {
				resultMap.put("result", true);
				resultMap.put("msg", "删除群组成功");
			}else{
				resultMap.put("result", false);
				resultMap.put("msg", "删除群组失败");
			}
		} catch (Exception e) {
			resultMap.put("msg", "系统内部异常");
			resultMap.put("result", false);
			logger.error("删除群组失败，msg：", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.DEL_DEV_GROUP, session, "删除群组："+devGroupId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.DEL_DEV_GROUP, session, "删除群组："+devGroupId, "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}
	
	
	/** <pre>updateDevGroupBooks(群组管理修改接口)   
	 * 创建人：周逸芳       
	 * 创建时间：2017年10月19日 上午11:19:50    
	 * 修改人：周逸芳        
	 * 修改时间：2017年10月19日 上午11:19:50    
	 * 修改备注： 
	 * @param devGroup
	 * @param session
	 * @return</pre>    
	 */
	@RequestMapping("updateDevGroupBooks")
	@ResponseBody
	public Map<String, Object> updateDevGroupBooks(@ModelAttribute DeviceGroupVO devGroup,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (null == devGroup) {
				resultMap.put("result", true);
				resultMap.put("msg", "参数为空");
				return resultMap;
			}
			if (StringUtils.isBlank(devGroup.getDevices())) {//所有设备
				resultMap.put("result", true);
				resultMap.put("msg", "群成员不能为空");
				return resultMap;
			}
			if (StringUtils.isNotBlank(devGroup.getName())) {//群名称
				devGroup.setName(URLDecoder.decode(devGroup.getName(), "utf-8"));
			}
			HashMap<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.put("groupId", devGroup.getUuid());
			paramsMap.put("name", devGroup.getName());
			if(deviceService.checkRepeatGroup(paramsMap) > 0){
				resultMap.put("result", false);
				resultMap.put("msg", "群名称已存在");
				return resultMap;
			}
		    int result = deviceService.updateDevGroupBooks(devGroup);
			if (result > 0) {
				resultMap.put("result", true);
				resultMap.put("msg", "修改群组信息成功");
			} else {
				resultMap.put("result", false);
				resultMap.put("msg", "修改群组信息失败");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "系统内部异常");
			logger.error("修改群组信息失败，msg：", e);
			e.printStackTrace();
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: devicesInGroup
	 * @Description: 获取群内已添加设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("devicesInGroup")
	@ResponseBody
	public Map<String, Object> devicesInGroup(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String groupId, String devId, String devName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			if(StringUtils.isBlank(groupId)){//群ID
				resultMap.put("result", false);
				resultMap.put("msg", "群ID不能为空");
				return resultMap;
			}
			String[] uuids = groupId.split(",");
			if (uuids.length > 0) {
				paramsMap.put("uuids", uuids);
			}
			paramsMap.put("devId", devId);//设备ID
			paramsMap.put("devName", devName);//设备名称
			List<DeviceVO> userList = deviceService.getDevsInGroup(paramsMap);
			if(pageSize != -1){
				int total = deviceService.getDevsInGroupCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
				resultMap.put("pageNum", pageNum);
			}
			resultMap.put("list", userList);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "获取群内联系人失败，系统内部异常");
			logger.error("获取会场内已添加设备失败", e);
		}
		return resultMap;
	}
}
