package com.visionvera.web.controller.rest;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.cms.ServerSyncVO;
import com.visionvera.constrant.LogType;
import com.visionvera.constrant.WsConstants;
import com.visionvera.service.DeviceService;
import com.visionvera.service.RegisterService;
import com.visionvera.service.SyncDataService;
import com.visionvera.service.SysConfigService;
import com.visionvera.util.LogWritter;
import com.visionvera.util.RestClient;


/**
 * 
 * @ClassName: SyncDataController
 * @Description: TODO 多级系统数据同步管理
 * @author xiechs
 * @date 2017年9月28日 上午11:28:27
 * 
 */
@RequestMapping("/rest/syncData")
@RestController
public class SyncDataController {

	@Resource
	private DeviceService deviceService;
	@Resource
	private SysConfigService sysConfigService;
	@Resource
	private RegisterService registerService;
	@Resource
	private SyncDataService syncDataService;
	
	//private static final //logger //logger = LogManager.get//logger(SyncDataController.class);

	/**
	 * 
	 * @Title: goList
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("showList")
	public String goList(ModelMap map) {
		return "/sysConfig/syncServerList";
	}

	/**
	 * 
	 * @Title: register
	 * @Description: webservice系统注册（使自己成为父系统的子系统）
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> register(@RequestBody(required = true)ServerSyncVO sv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (sv == null) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "参数错误");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getDstIp())
					|| StringUtils.isBlank(sv.getDstPort())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "父系统IP和端口不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getDstAccount())
					|| StringUtils.isBlank(sv.getDstPassword())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "父系统用户名和密码不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getDstAreaId())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "父系统行政区域不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getIp())
					|| StringUtils.isBlank(sv.getPort())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "本系统IP和端口不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getAccount())
					|| StringUtils.isBlank(sv.getPassword())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "本系统用户名和密码不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getAreaId())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "本系统行政区域不能为空");
				return resultMap;
			}
			//调用webservice注册
			String baseUrl = String.format(WsConstants.CMS_HOST_URL, sv.getDstIp(),sv.getDstPort());

			//获得token
			Map<String, Object> args = new HashMap<String, Object>();
			args.put(WsConstants.KEY_CMS_USER, sv.getDstAccount());//账号
			args.put(WsConstants.KEY_CMS_PWD, sv.getDstPassword());//密码
			String token = null;
			try{
				Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_CMS_LOGIN, null, args);
				if(data.get(WsConstants.KEY_TOKEN) == null){
					resultMap.put("result", false);
					resultMap.put("errmsg", data.get("errmsg"));
					return resultMap;
				}
				token = data.get(WsConstants.KEY_TOKEN).toString();
//				token = WsAgent.getToken(baseUrl + WsConstants.URL_LOGIN, args);
			}catch(Exception e){
				resultMap.put("result", false);
				resultMap.put("errmsg", "注册失败：获取token失败");
				return resultMap;
			}
			//将本系统信息注册到父系统
			args = new HashMap<String, Object>();
			args.put(WsConstants.KEY_CMS_IP, sv.getIp());//本系统IP
			args.put(WsConstants.KEY_CMS_PORT, sv.getPort());//本系统端口
			args.put(WsConstants.KEY_CMS_ACCOUNT, sv.getAccount());//本系登录账号
			args.put(WsConstants.KEY_CMS_PASSWORD, sv.getPassword());//本系统登录密码
			args.put(WsConstants.KEY_CMS_AREA_ID, sv.getAreaId());//本系统行政区域ID
			args.put(WsConstants.KEY_CMS_AREA_NAME, sv.getAreaName());//本系统行政区域名称
			Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_CMS_REGISTER, token, args);
			if(data != null && data.get("errmsg") != null){
				data.put("result", false);
				return data;
			}
			Map<String, Object> extra = (Map<String, Object>) data.get("extra");
			if(extra == null || extra.get("registerId") == null){
				resultMap.put("result", false);
				resultMap.put("errmsg", "注册失败：数据异常");
			}
			String registerId = extra.get("registerId").toString();
			//保存父系统的信息到本地数据库
			if (StringUtils.isNotBlank(sv.getDstAreaName())) {// 父系统区域名称
				sv.setDstAreaName(URLDecoder.decode(sv.getDstAreaName(), "utf-8"));
			}
			sv.setIp(sv.getDstIp());
			sv.setPort(sv.getDstPort());
			sv.setAccount(sv.getDstAccount());
			sv.setPassword(sv.getDstPassword());
			sv.setAreaId(sv.getDstAreaId());
			sv.setAreaName(sv.getDstAreaName());
			sv.setType(1);//本系统注册到其它系统，其它系统是本系统的父系统
			sv.setRegisterId(registerId);//保存本系统在父系统中的注册ID（解注册时使用）
			int ret = registerService.register(sv);
			if(ret > 0){
				resultMap.put("result", true);
				resultMap.put("errmsg", "注册成功");
			}
		} catch (Exception e) {
			//logger.error("分级系统注册失败", e);
			resultMap.put("result", false);
			resultMap.put("errmsg", "注册失败，内部异常");
		} 
		return resultMap;
	}

	/**
	 * 
	 * @Title: unregister
	 * @Description: webservice系统解注册（使自己与父系统解除父子关系）
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/unregister", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> unregister(@RequestBody(required = true)ServerSyncVO sv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (sv == null || StringUtils.isBlank(sv.getRegisterId())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "参数错误");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getDstIp())
					|| StringUtils.isBlank(sv.getDstPort())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "父系统IP和端口不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getDstAccount())
					|| StringUtils.isBlank(sv.getDstPassword())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "父系统用户名和密码不能为空");
				return resultMap;
			}
			//调用webservice解注册
			String baseUrl = String.format(WsConstants.CMS_HOST_URL, sv.getDstIp(),sv.getDstPort());

			//获得token
			Map<String, Object> args = new HashMap<String, Object>();
			args.put(WsConstants.KEY_CMS_USER, sv.getDstAccount());//账号
			args.put(WsConstants.KEY_CMS_PWD, sv.getDstPassword());//密码
			String token = null;
			try{
				Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_CMS_LOGIN, null, args);
				if(data.get(WsConstants.KEY_TOKEN) == null){
					resultMap.put("result", false);
					resultMap.put("errmsg", data.get("errmsg"));
					return resultMap;
				}
				token = data.get(WsConstants.KEY_TOKEN).toString();
//				token = WsAgent.getToken(baseUrl + WsConstants.URL_LOGIN, args);
			}catch(Exception e){
				resultMap.put("result", false);
				resultMap.put("errmsg", "解注册失败：获取token失败");
				return resultMap;
			}
			//将本系统信息与父系统解注册
			args = new HashMap<String, Object>();
			args.put(WsConstants.KEY_CMS_REGISTER_ID, sv.getRegisterId());//本系统在父系统中注册的uuid
			Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_CMS_UNREGISTER, token, args);
			if(data != null && "1".equals(data.get("errcode").toString())){
				data.put("result", false);
				return data;
			}
			//删除本系统数据库中的注册信息
			int ret = registerService.unregister(sv);
			if(ret > 0){
				resultMap.put("result", true);
				resultMap.put("errmsg", "解注册成功");
			}
		} catch (Exception e) {
			//logger.error("解注册失败", e);
			resultMap.put("result", false);
			resultMap.put("errmsg", "解注册失败，内部异常");
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.SYNC_SYS_UNREGISTER, session, "从" + sv.getDstIp() + "解注册", "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.SYNC_SYS_UNREGISTER, session, "从" + sv.getDstIp() + "解注册", "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: delRegister
	 * @Description: 删除系统信息
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/delRegister", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> delRegister(@RequestBody(required = true)ServerSyncVO sv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (sv == null || StringUtils.isBlank(sv.getUuid())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "参数错误");
				return resultMap;
			}
			//删除本系统数据库中的注册信息
			int ret = registerService.unregister(sv);
			if(ret > 0){
				resultMap.put("result", true);
				resultMap.put("errmsg", "删除成功");
			}
		} catch (Exception e) {
			//logger.error("删除失败", e);
			resultMap.put("result", false);
			resultMap.put("errmsg", "删除失败，内部异常");
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: getSyncServers
	 * @Description: 多级系统-获取系统列表（子系统、父系统）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/getSyncServers", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> getSyncServers(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestBody ServerSyncVO sv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			if (StringUtils.isNotBlank(sv.getAreaName())) {// 区域
				sv.setAreaName(URLDecoder.decode(sv.getAreaName(), "utf-8"));
			}
			List<ServerSyncVO> list = registerService.getSyncServers(sv, paramsMap);
			int total = registerService.getSyncServersCount(paramsMap);
			total = total % pageSize == 0 ? total / pageSize
					: (total / pageSize) + 1;
			resultMap.put("pageNum", pageNum);
			resultMap.put("pageTotal", total);
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "系统内部异常");
			//logger.error("获取多级系统列表数据失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: synchroDevices
	 * @Description: 从子系统同步设备、用户、会议等数据
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/syncData", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> syncData(@RequestBody ServerSyncVO sv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (StringUtils.isBlank(sv.getDstIp())
					|| StringUtils.isBlank(sv.getDstPort())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "父系统IP和端口不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getDstAccount())
					|| StringUtils.isBlank(sv.getDstPassword())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "父系统用户名和密码不能为空");
				return resultMap;
			}
			resultMap = syncDataService.syncData(sv);
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "同步失败，系统内部异常");
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: editRegister
	 * @Description: 多级系统-编辑系统信息
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/editRegister", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> editRegister(@RequestBody ServerSyncVO sv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (StringUtils.isBlank(sv.getIp())
					|| StringUtils.isBlank(sv.getPort())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "系统IP和端口不能为空");
				return resultMap;
			}
			if (StringUtils.isBlank(sv.getAccount())) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "系统用户名不能为空");
				return resultMap;
			}
			registerService.editRegister(sv);
			resultMap.put("errmsg", "编辑成功");
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "编辑失败，系统内部异常");
		}
		return resultMap;
	}
}
