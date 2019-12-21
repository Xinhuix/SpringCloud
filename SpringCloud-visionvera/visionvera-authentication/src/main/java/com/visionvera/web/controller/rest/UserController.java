package com.visionvera.web.controller.rest;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.common.api.authentication.UserAPI;
import com.visionvera.constrant.LogTypeConstrant;
import com.visionvera.exception.BusinessException;
import com.visionvera.mq.provider.RabbitProviderAsync;
import com.visionvera.service.UserService;
import com.visionvera.util.Sm3Utils;
import com.visionvera.util.StringUtil;

@RestController
public class UserController extends BaseReturn implements UserAPI {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RabbitProviderAsync rabbitProvider;
	
	
	/**
	 * 添加用户信息
	 * @param pfType 平台类型
	 * @param user 用户信息。source不为空表示从其他平台过来的添加(流媒体)
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public ReturnData userAdd(@RequestBody UserVO user, @RequestParam("access_token") String token) {
		try {
			/** 数据校验 Start */
			ReturnData dataReturn = this.checkUserInfoNew(user);//校验用户的必填信息
			if (!dataReturn.getErrcode().equals(0)) {//用户必填信息校验失败
				return dataReturn;
			}
			/** 数据校验 End */
			
			if (StringUtil.isNotNull(user.getSource())) {//流媒体添加用户
				return this.userService.addUserForLmt(user, token);
			}
			
			//添加用户.运维工作站添加用户
			return this.userService.addUser(user, token);
		} catch (BusinessException e) {
			this.LOGGER.error(e.getMessage(), e);
			this.rabbitProvider.sendLogMessage(LogTypeConstrant.ADD_USER, "添加用户失败", false, e.getMessage(), token);
			return super.returnError("添加用户失败");
		} catch (Exception e) {
			this.LOGGER.error("添加用户失败,=====> ", e);
			this.rabbitProvider.sendLogMessage(LogTypeConstrant.ADD_USER, "添加用户失败", false, "系统错误", token);
			return super.returnError("添加用户失败");
		}
	}
	
	/**
	 * 修改用户信息
	 * @param pfType 平台类型
	 * @param user 用户信息
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData userEdit(@RequestBody UserVO user, @RequestParam("access_token") String token) {
		try {
			return this.userService.updateUser(user, token);
		} catch (Exception e) {
			this.LOGGER.error("更新用户失败 ===== UserController ===== userEdit =====> ", e);
			this.rabbitProvider.sendLogMessage(LogTypeConstrant.EDIT_USER, "修改用户失败", false, "系统错误", token);
			return super.returnError("更新失败");
		}
	}
	
	/**
	 * 删除用户信息
	 * @param user 主键UUID，多个主键UUID之间使用英文逗号","隔开
	 * @return
	 */
	@RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData userDel(@RequestBody UserVO user, @RequestParam("access_token") String token) {
		try {
			if (StringUtil.isNull(user.getUuid())) {
				return super.returnError("用户ID不能为空");
			}
			
			return this.userService.delUserBatch(user.getUuid(), token, null);
		} catch (Exception e) {
			this.LOGGER.error("删除用户失败 ===== UserController ===== userDel =====> ", e);
			//发送操作日志消息: 异步
			this.rabbitProvider.sendLogMessage(LogTypeConstrant.DEL_USER, "删除用户失败", false, "系统错误", token);
			return super.returnError("删除失败");
		}
	}
	/**
	 * 通过用户名删除用户信息
	 * @param user 主键UUID，多个主键UUID之间使用英文逗号","隔开
	 * @return
	 */
	@RequestMapping(value = "/delByLoginName", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData delByLoginName(@RequestBody UserVO user, @RequestParam("access_token") String token) {
		try {
			if (StringUtil.isNull(user.getLoginName())) {
				return super.returnError("用户名不能为空");
			}
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("loginName", user.getLoginName());
			UserVO entity = userService.selectUserByUniqueKey(params);
	        if(entity==null){
	        	return super.returnError("删除失败,用户不存在！");
	        }
	        user.setUuid(entity.getUuid());
			return this.userService.delUserBatch(user.getUuid(), token, user.getSource());
		} catch (Exception e) {
			this.LOGGER.error("删除用户失败 ===== UserController ===== delByLoginName =====> ", e);
			//发送操作日志消息: 异步
			this.rabbitProvider.sendLogMessage(LogTypeConstrant.DEL_USERBYLOGINNAME, "删除用户失败", false, "系统错误", token);
			return super.returnError("删除失败");
		}
	}
	
	/**
	 * 获取用户列表
	 * @param user 用户条件
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return
	 */
	@RequestMapping(value = "/getUserList", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData getUserList(@RequestBody(required = false) UserVO user, 
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize) {
		Map<String, Object> extraMap = new HashMap<String, Object>();
		try {
			PageInfo<UserVO> userInfo = this.userService.getUserList(true, pageNum, pageSize, user);
			extraMap.put("totalPage", userInfo.getPages());
			extraMap.put("pageNum", pageNum);
			extraMap.put("pageSize", pageSize);
			
			return super.returnResult(0, "查询成功", null, userInfo.getList(), extraMap);
		} catch (Exception e) {
			this.LOGGER.error("获取用户列表失败 ===== UserController ===== getUserList =====> ", e);
			return super.returnError("获取用户列表失败");
		}
	}
	
	/**
	 * 获取用户列表信息，用于子平台同步用户信息
	 * @param user 查询条件
	 * @param pageNum 页码.默认从第一页开始查询
	 * @param pageSize 页大小.默认页大小为15条
	 * @param isPage 是否分页.默认分页
	 * @return
	 */
	@RequestMapping(value = "/getUserListForSync", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData getUserListForSync(@RequestBody(required = false) UserVO user, 
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestParam(value = "isPage", required = false, defaultValue = "true") Boolean isPage) {
		Map<String, Object> extraMap = new HashMap<String, Object>();
		try {
			PageInfo<UserVO> userInfo = this.userService.getUserListForSync(isPage, pageNum, pageSize, user);
			
			if (isPage) {//分页才会封装分页信息
				extraMap.put("totalPage", userInfo.getPages());
				extraMap.put("pageNum", pageNum);
				extraMap.put("pageSize", pageSize);
			}
			
			return super.returnResult(0, "获取用户信息成功", null, userInfo.getList(), extraMap);
			
		} catch (Exception e) {
			this.LOGGER.error("同步用户信息失败 ===== UserController ===== getUserListForSync =====> ", e);
			return super.returnError("同步用户列表失败");
		}
	}
	
	/**
	 * 通过Token获取用户信息，提供其他微服务获取用户信息的接口
	 */
	@Override
	public ReturnData getUser(@PathVariable("token") String token) {
		try {
			return this.userService.getUserByToken(token);
		} catch (Exception e) {
			this.LOGGER.error("获取用户信息失败 ===== UserController ===== getUser =====>", e);
			return super.returnError("获取用户信息失败");
		}
	}
	
	/**
	 * 获取手机号验证码
	 * @param phone 手机号
	 * @param type 验证码类型：1 通过手机号验证码； 2 用户名获取验证码
	 * @return
	 */
	@RequestMapping(value = "/{type}/sendVirificationCode", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData sendVirificationCode(@RequestBody UserVO user, @PathVariable("type") Integer type) {
		try {
			/** 数据校验 Start */
			if (type.equals(1)) {
				if (StringUtil.isNull(user.getPhone())) {
					return super.returnError("手机号不能为空");
				}
			} else if (type.equals(2)) {
				if (StringUtil.isNull(user.getLoginName())) {
					return super.returnError("用户名不能为空");
				}
			}
			/** 数据校验 End */



			return this.userService.sendVirificationCode(user, type);
		} catch (Exception e) {
			this.LOGGER.error("发送验证码失败 =====>", e);
			return super.returnError("发送失败");
		}
	}
	
	/**
	 * 通过UUID获取用户信息，携带对应的权限信息
	 * @param uuid 用户UUID
	 * @param token
	 * @return
	 */
	@Override
	public ReturnData getUserInfo(@PathVariable("uuid") String uuid, @RequestParam(name = "access_token") String token) {
		try {
			return this.userService.getUserInfoById(uuid, token);
		} catch (Exception e) {
			this.LOGGER.error("通过UUID获取用户信息失败 ===== UserController ===== getUserInfo =====>", e);
			return super.returnError("获取用户信息失败");
		}
	}
	
	/**
	 * 通过Token获取平台ID和登录名
	 * @param token 访问令牌
	 * @return
	 */
	public ReturnData getPlatformIdAndLoginNameByToken(@PathVariable("token") String token) {
		try {
			return this.userService.getPlatformIdAndLoginName(token);
		} catch (Exception e) {
			this.LOGGER.error("获取用户信息失败 ===== UserController ===== getUser =====>", e);
			return super.returnError("获取平台ID和登录名失败");
		}
	}
	
	/**
	 * 同步会管用户信息
	 * @param token
	 * @param otherPlatformId 平台ID
	 * @param platformType 平台类别
	 * @return
	 */
	@RequestMapping(value = "/platformId/{otherPlatformId}/platformType/{platformType}/syncHuiguanUser", method = RequestMethod.GET)
	public ReturnData syncHuiguanUser(@RequestParam("access_token") String token, 
			@PathVariable(name = "otherPlatformId") String otherPlatformId, 
			@PathVariable(name = "platformType") String platformType) {
		try {
			return this.userService.syncHuiguanUser(token, otherPlatformId, platformType);
		} catch (BusinessException e) {
			this.LOGGER.error("同步会管用户信息失败 ===== UserController ===== syncHuiguanUser =====>", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("同步会管用户信息失败 ===== UserController ===== syncHuiguanUser =====>", e);
			return super.returnError("同步会管用户信息失败");
		}
	}
	
	/**
	 * 下载同步失败的用户Excel文件
	 * @param exportSystemType 导出哪个平台的系统类型。1：表示与会管平台同步；2：表示与流媒体平台同步
	 * @return
	 */
	@RequestMapping(value = "/{exportSystemType}/downloadSyncUserFailExcel", method = RequestMethod.GET)
	public ReturnData downloadSyncUserFailExcel(@PathVariable("exportSystemType") String exportSystemType, HttpServletResponse response) {
		HSSFWorkbook workBook = null;
		OutputStream outputStream = null;
		String fileName = "同步失败用户.xls";
		
		try {
			if (!exportSystemType.equals("1") && !exportSystemType.equals("2")) {
				return super.returnError("系统类型错误");
			}
			
			workBook = this.userService.getSyncUserFailWorkbook(exportSystemType);//获取工作簿对象
			
			//获取输入流并进行清空处理
			outputStream = response.getOutputStream();
			response.reset();
			// 设定输出文件头
            response.setHeader("Content-disposition", "attachment; filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859-1"));
            // 定义输出类型
            response.setContentType("application/msexcel;charset=utf-8");
            
            workBook.write(outputStream);//写出
			return super.returnResult(0, "下载成功");
		} catch (BusinessException e) {
			this.LOGGER.error("下载同步失败的用户Excel失败 ===== UserController ===== downloadSyncUserFailExcel =====>", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			this.LOGGER.error("下载同步失败的用户Excel失败 ===== UserController ===== downloadSyncUserFailExcel =====>", e);
			return super.returnError("下载失败");
		} finally {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(workBook);
		}
	}
	
	/**
	 * 校验用户的必填字段：登录名、密码、手机号
	 * @param user
	 * @return
	 */
	private ReturnData checkUserInfoNew(UserVO user) {
		if (null == user) {
			return super.returnError("用户信息不能为空");
		}
		
		if (StringUtil.isNull(user.getLoginName())) {
			return super.returnError("用户登录名不能为空");
		}
		
		if (StringUtil.isNull(user.getLoginPwd())) {
			return super.returnError("用户密码不能为空");
		}
		
		if (StringUtil.isNull(user.getPhone())) {
			return super.returnError("手机号不能为空");
		}
		
		return super.returnResult(0, "校验成功");
	}
	
	
	/**
	 * 流媒体用户同步
	 * @return
	 */
	@RequestMapping(value = "/syncUserListForLmt/{otherPlatformId}", method = RequestMethod.GET)
	public ReturnData syncUserListForLmt(@PathVariable(name = "otherPlatformId") String otherPlatformId, 
			@RequestParam(name = "access_token") String token) {
		try {
			return this.userService.syncUserForLmt(otherPlatformId, token);
		} catch (BusinessException e) {
			this.LOGGER.error("同步流媒体用户信息失败 ===== UserController ===== syncUserListForLmt =====>", e);
			return super.returnError(e.getMessage());
	    } catch (Exception e) {
	    	this.LOGGER.error("同步流媒体用户信息失败 ===== UserController ===== syncUserListForLmt =====>", e);
	    	return super.returnError("同步失败");
	    }
	}
	
	/**
	 * 修改用户密码
	 * @param user 用户信息
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "/updateUserPwd", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData updateUserPwd(@RequestBody UserVO user) {
		try {
			if (StringUtil.isNull(user.getLoginName())) {
				return super.returnError("用户名不能为空");
			}
			if (StringUtil.isNull(user.getLoginPwd())) {
				return super.returnError("旧密码不能为空");
			}
			if (StringUtil.isNull(user.getNewPwd())) {
				return super.returnError("sm3新密码不能为空");
			}
			if (StringUtil.isNull(user.getMd5Pwd())) {
				return super.returnError("md5新密码不能为空");
			}
			return this.userService.updateUserPwd(user);
		} catch (Exception e) {
			this.LOGGER.error("修改用户密码失败 ===== UserController ===== updateUserPwd =====> ", e);
			return super.returnError("修改用户密码失败");
		}
	}
	
	
	/**
	 *  Description:更新旧数据密码加密方式为sm3
	 *  @author  ==zyf==
	 *  @date 2019年11月18日 下午2:19:36  
	 *  @return
	 */
	@RequestMapping(value = "/updateHistoryUser")
	public ReturnData updateHistoryUser() {
		try {
			return this.userService.updateHistoryUser();
		} catch (BusinessException e) {
			this.LOGGER.error(e.getMessage(), e);
			return super.returnError("更新用户密码加密方式失败");
		}
	}
	
}
