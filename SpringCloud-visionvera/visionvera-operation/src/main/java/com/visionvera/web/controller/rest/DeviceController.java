package com.visionvera.web.controller.rest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceTypeVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.LogType;
import com.visionvera.feign.UserService;
import com.visionvera.service.DeviceService;
import com.visionvera.service.SysConfigService;
import com.visionvera.util.ExcelUtil;
import com.visionvera.util.LogWritter;
import com.visionvera.util.RegionUtil;
import com.visionvera.util.SocketClient;
import com.visionvera.util.VSMeet;



/**
 * 
 * @ClassName: DeviceController
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xiechs
 * @date 2016年9月13日 下午5:45:27
 * 
 */
@RestController
@RequestMapping("/rest/device")
public class DeviceController {

	@Resource
	private DeviceService deviceService;
	@Resource
	private SysConfigService sysConfigService;
	@Resource
	private UserService userService;
	
	private static final Logger logger = LogManager.getLogger(DeviceController.class);

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
		return "/device/deviceList";
	}

	@RequestMapping("deviceList")
	
	public Map<String, Object> getDeviceList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String id, String name, String mac, String ip, String type, String description, String groupId, String groupName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("id", id);
			paramsMap.put("mac", mac);
			paramsMap.put("ip", ip);
			paramsMap.put("type", type);
			paramsMap.put("groupId", groupId);
			if (StringUtils.isNotBlank(groupName)) {
				paramsMap.put("groupName", URLDecoder.decode(groupName, "utf-8"));
			}
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			if (StringUtils.isNotBlank(description)) {
				paramsMap.put("description",
						URLDecoder.decode(description, "utf-8"));
			}
			List<DeviceVO> deviceList = deviceService.getDeviceList(paramsMap);
			int total = deviceService.getDeviceListCount(paramsMap);
			total = total % pageSize == 0 ? total / pageSize
					: (total / pageSize) + 1;
			resultMap.put("pageNum", pageNum);
			resultMap.put("pageTotal", total);
			resultMap.put("list", deviceList);
			resultMap.put("result", true);

		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取设备列表失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: synchroDevices
	 * @Description: 从网管同步设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("synchro")
	
	public Map<String, Object> synchroDevices(String scheduleId, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			resultMap = deviceService.synchroDevices(paramsMap);
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "同步失败，系统内部异常");
			logger.error("从网管服务器同步设备失败", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.SYN_DEVICE, session, "从网管同步设备", "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.SYN_DEVICE, session, "从网管同步设备", "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: synchroDevice
	 * @Description: 从网管同步设备（定时任务用）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public void synchroDevice() {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			deviceService.synchroDevices(paramsMap);
		} catch (Exception e) {
			logger.error("定时从网管服务器同步设备失败", e);
		}
	}
	
	/**
	 * 
	 * @Title: getRegions
	 * @Description: 获取行政区域信息
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("regions")
	
	public Map<String, Object> getRegions(Integer id, String name) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("id", id);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			List<RegionVO> list = deviceService.getRegions(paramsMap);
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取行政区域信息失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getDevices
	 * @Description: 获取行设备信息（根据行政节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("devices")
	
	public Map<String, Object> getDevices(Integer id, String name, String regionId,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("id", id);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			paramsMap.put("regionId", regionId);
			List<DeviceVO> list = deviceService.getDevices(paramsMap);
			resultMap.put("list", list);
			if(pageSize != -1){
				int total = deviceService.getDevicesCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
			}
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取行政区域下的设备失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getDevices
	 * @Description: 获取行设备信息（根据行政节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("devicesOfRegion")
	
	public Map<String, Object> getDevicesOfRegion(String regionId,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("regionId", RegionUtil.getBase(regionId));
			List<DeviceVO> list = deviceService.getDevicesOfRegion(paramsMap);
			resultMap.put("list", list);
			if(pageSize != -1){
				int total = deviceService.getDevicesOfRegionCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
			}
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取行政区域下的所有设备失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getRoot
	 * @Description: 获取行政区域信息的第一级节点（只返回有服务器的节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("root")
	
	public Map<String, Object> getRoot(String id, String name) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("id", id);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			List<RegionVO> list = deviceService.getRoot(paramsMap);
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取行政区域信息根节点（省份）失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: getChild
	 * @Description: 获取行政区域信息的子节点
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("child")
	
	public Map<String, Object> getChild(String pId, String regionName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pId", pId);
			paramsMap.put("regionName", regionName);
			List<RegionVO> list = deviceService.getChild(paramsMap);
			if(list == null || list.size() < 1){
				paramsMap.remove("regionName");
				list = deviceService.getChild(paramsMap);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取行政区域信息子节点失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: auth
	 * @Description: 给用户组授权设备（增减合一）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("auth")
	
	public Map<String, Object> auth(String groupId, String regionIds, String devIds) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("errmsg", "授权失败");
		resultMap.put("result", false);
		if(StringUtils.isBlank(groupId)){
			logger.error("授权设备失败: 用户组ID为空");
			return resultMap;
		}
		/*if(StringUtils.isBlank(regionIds) && StringUtils.isBlank(devIds)){
			logger.error("授权设备失败: 请勾选设备或行政区域节点");
			return resultMap;
		}*/
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(regionIds)){
			regionIds = "";
		}
		if(StringUtils.isBlank(devIds)){
			devIds = "";
		}
		try {
			//授权设备
			paramsMap.put("groupId", groupId);//用户分组ID
			paramsMap.put("devIds", devIds);//设备ID字符串
			paramsMap.put("regionIds", regionIds);//设备ID字符串
			resultMap = deviceService.auth(paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("授权设备失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: addAuth
	 * @Description: 给用户组授权设备（增加）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("addAuth")
	
	public Map<String, Object> addAuth(String groupId, String regionIds, String devIds
			,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("errmsg", "授权失败");
		resultMap.put("result", false);
		if(StringUtils.isBlank(groupId)){
			logger.error("授权设备失败: 用户组ID为空");
			return resultMap;
		}
		/*if(StringUtils.isBlank(regionIds) && StringUtils.isBlank(devIds)){
			logger.error("授权设备失败: 请勾选设备或行政区域节点");
			return resultMap;
		}*/
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(regionIds)){
			regionIds = "";
		}
		if(StringUtils.isBlank(devIds)){
			devIds = "";
		}
		try {
			//授权设备
			paramsMap.put("groupId", groupId);//用户分组ID
			paramsMap.put("devIds", devIds);//设备ID字符串
			paramsMap.put("regionIds", regionIds);//设备ID字符串
			resultMap = deviceService.addAuth(paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("授权设备失败", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.ADD_AUTH_DEVICE, session, "增加授权设备，用户组："+groupId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.ADD_AUTH_DEVICE, session, "增加授权设备，用户组："+groupId, "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: delAuth
	 * @Description: 给用户组授权设备（减少）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("delAuth")
	
	public Map<String, Object> delAuth(String groupId, String regionIds, String devIds,
			HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("errmsg", "授权失败");
		resultMap.put("result", false);
		if(StringUtils.isBlank(groupId)){
			logger.error("授权设备失败: 用户组ID为空");
			return resultMap;
		}
		/*if(StringUtils.isBlank(regionIds) && StringUtils.isBlank(devIds)){
			logger.error("授权设备失败: 请勾选设备或行政区域节点");
			return resultMap;
		}*/
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(regionIds)){
			regionIds = "";
		}
		if(StringUtils.isBlank(devIds)){
			devIds = "";
		}
		try {
			//授权设备
			paramsMap.put("groupId", groupId);//用户分组ID
			paramsMap.put("devIds", devIds);//设备ID字符串
			paramsMap.put("regionIds", regionIds);//设备ID字符串
			resultMap = deviceService.delAuth(paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("授权设备失败", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.DEL_AUTH_DEVICE, session, "减少授权设备，用户组："+groupId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.DEL_AUTH_DEVICE, session, "减少授权设备，用户组："+groupId, "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getAuthed
	 * @Description: 获取已经授权给用户组的设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("authed")
	
	public Map<String, Object> getAuthed(String groupId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("groupId", groupId);
			List<DeviceVO> list = deviceService.getAuthed(paramsMap);
			resultMap.put("list", list);
			resultMap.put("result", true);
			resultMap.put("errmsg", "获取已授权设备成功");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取已授权设备失败");
			logger.error("获取已授权设备失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getAuthedOfRegion
	 * @Description: 获取某一节点下户组内已授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("authedOfRegion")
	
	public Map<String, Object> getAuthedOfRegion(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String groupId, String regionId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("groupId", groupId);
			paramsMap.put("regionId", regionId);
			List<DeviceVO> list = deviceService.getAuthedOfRegion(paramsMap);
			resultMap.put("list", list);
			if(pageSize != -1){
				int total = deviceService.getAuthedOfRegionCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
			}
			resultMap.put("result", true);
			resultMap.put("errmsg", "获取已授权设备成功");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取已授权设备失败");
			logger.error("获取已授权设备失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getAuthedOfRegions
	 * @Description: 获取某一节点下户组内已授权的所有设备 （含子节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("authedOfRegions")
	
	public Map<String, Object> getAuthedOfRegions(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String groupId, String regionId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("groupId", groupId);
			paramsMap.put("regionId", RegionUtil.getBase(regionId));
			List<DeviceVO> list = deviceService.getAuthedOfRegions(paramsMap);
			resultMap.put("list", list);
			if(pageSize != -1){
				int total = deviceService.getAuthedOfRegionsCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
			}
			resultMap.put("result", true);
			resultMap.put("errmsg", "获取所有已授权设备成功");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取所有已授权设备失败");
			logger.error("获取所有已授权设备失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getNotAuthedOfRegion
	 * @Description: 获取某一节点下户组内未授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("notAuthedOfRegion")
	
	public Map<String, Object> getNotAuthedOfRegion(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String groupId, String regionId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("groupId", groupId);
			paramsMap.put("regionId", regionId);
			List<DeviceVO> list = deviceService.getNotAuthedOfRegion(paramsMap);
			resultMap.put("list", list);
			if(pageSize != -1){
				int total = deviceService.getNotAuthedOfRegionCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
			}
			resultMap.put("result", true);
			resultMap.put("errmsg", "获取未授权设备成功");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取未授权设备失败");
			logger.error("获取未授权设备失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getNotAuthedOfRegions
	 * @Description: 获取某一节点下户组内未授权的所有设备 （含子节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("notAuthedOfRegions")
	
	public Map<String, Object> getNotAuthedOfRegions(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String groupId, String regionId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("groupId", groupId);
			paramsMap.put("regionId", RegionUtil.getBase(regionId));
			List<DeviceVO> list = deviceService.getNotAuthedOfRegions(paramsMap);
			resultMap.put("list", list);
			if(pageSize != -1){
				int total = deviceService.getNotAuthedOfRegionsCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
			}
			resultMap.put("result", true);
			resultMap.put("errmsg", "获取所有未授权设备成功");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取所有未授权设备失败");
			logger.error("获取所有授权设备失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: delAuthed（该接口暂时不对外开放）
	 * @Description: 删除用户组内已授权的设备（devIds为空时，删除组内所有已授权设备）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("delAuthed")
	
	public Map<String, Object> delAuthed(String groupId, String devIds) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("groupId", groupId);//用户分组ID
			String[] deviceuuids = devIds.split(",");
			paramsMap.put("devIds", deviceuuids);//设备ID数组
			//从用户组中移除授权的设备
			resultMap = deviceService.delAuthed(paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "删除用户组内已授权设备失败");
			logger.error("删除用户组内已授权设备失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: userGroupList
	 * @Description: 获取用户组列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("userGroupList")
	
	public Map<String, Object> getUserGroupList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestParam(value = "access_token") String token, String id, String name, String devId, String devName, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		UserVO userData = new UserVO();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("id", id);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("groupName", URLDecoder.decode(name, "utf-8"));
			}
			paramsMap.put("devId", devId);
			if (StringUtils.isNotBlank(devName)) {
				paramsMap.put("devName", URLDecoder.decode(devName, "utf-8"));
			}
			List<UserGroupVO> list = deviceService.getUserGroupList(paramsMap);
			ReturnData data = userService.getUser(token);
			if (!data.getErrcode().equals(0)) {
				resultMap.put("result", false);
				resultMap.put("errmsg", data.getErrmsg());
				return resultMap;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>)data.getData();
			userData = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
			String loginUserId = userData.getUuid();
			for(UserGroupVO group : list){
				if(loginUserId.equals(group.getIsLock())){
					group.setIsLock("1");//被当前登录的用户锁定
				}else{
					group.setIsLock("0");//未被锁定
				}
			}
			if(pageSize != -1){
				int total = deviceService.getUserGroupCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取用户分组列表失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: groupSearch
	 * @Description: 模糊查询设备、会场、用户组（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("groupSearch")
	
	public Map<String, Object> groupSearch(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestParam(value = "access_token") String token,String groupName, String devId, String devName, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		UserVO userData = new UserVO();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			if (StringUtils.isNotBlank(groupName)) {//用户组名
				paramsMap.put("groupName", URLDecoder.decode(groupName, "utf-8"));
			}
			paramsMap.put("devId", devId);//设备ID
			if (StringUtils.isNotBlank(devName)) {//设备名称
				paramsMap.put("devName", URLDecoder.decode(devName, "utf-8"));
			}
			List<UserGroupVO> userGroupList = deviceService.getUserGroupList(paramsMap);
			List<DeviceGroupVO> deviceGroupList = deviceService.getGroupList(paramsMap);
			List<DeviceVO> deviceList = deviceService.devicesInGroup(paramsMap);
			ReturnData data = userService.getUser(token);
			if (!data.getErrcode().equals(0)) {
				resultMap.put("result", false);
				resultMap.put("errmsg", data.getErrmsg());
				return resultMap;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>)data.getData();
			userData = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
			String loginUserId = userData.getUuid();
			for(UserGroupVO group : userGroupList){
				if(loginUserId.equals(group.getIsLock())){
					group.setIsLock("1");//被当前登录的用户锁定
				}else{
					group.setIsLock("0");//未被锁定
				}
			}
			if(pageSize != -1){
				int total = deviceService.getUserGroupCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("userGroupList", userGroupList);
			resultMap.put("deviceGroupList", deviceGroupList);
			resultMap.put("deviceList", deviceList);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取用户分组列表失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: groupList
	 * @Description: 获取用户组下的会场列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("groupList")
	
	public Map<String, Object> getGroupList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String groupId, String id, String name, String devId, String devName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("groupId", groupId);//用户组ID
			paramsMap.put("id", id);//会场ID
			if (StringUtils.isNotBlank(name)) {//会场名称
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			if (StringUtils.isNotBlank(devName)) {
				paramsMap.put("devName", URLDecoder.decode(devName, "utf-8"));
			}
			paramsMap.put("devId", devId);//设备ID
			List<DeviceGroupVO> list = deviceService.getGroupList(paramsMap);
			if(pageSize != -1){
				int total = deviceService.getGroupCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageNum", pageNum);
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取用户分组下的会场列表失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: rmFromGroup
	 * @Description: 移除会场内的设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("rmFromGroup")
	
	public Map<String, Object> rmFromGroup(String groupIds, String devIds) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			String[] devgroupuuids = groupIds.split(",");
			paramsMap.put("uuids", devgroupuuids);//会场ID数组
			String[] deviceuuids = devIds.split(",");
			paramsMap.put("devIds", deviceuuids);//设备ID数组
			resultMap = deviceService.rmFromGroup(paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "移动设备失败");
			logger.error("移动会场内设备失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: mvDev2Group
	 * @Description: 移动（多个）会场内的设备到其它（一个）会场
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("mvDev2Group")
	
	public Map<String, Object> mvDev2Group(String groupIds, String devIds,
			String destGroupId, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			String[] devgroupuuids = groupIds.split(",");
			paramsMap.put("uuids", devgroupuuids);//会场ID数组（可多个）
			String[] deviceuuids = devIds.split(",");
			paramsMap.put("devIds", deviceuuids);//设备ID数组（可多个）
			paramsMap.put("groupId", destGroupId);//目标会场会场ID（一个）
			resultMap = deviceService.mvDev2Group(paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "移动设备失败");
			logger.error("移动会场内设备失败", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.MV_DEVICE, session, "移动设备到其它会场", "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.MV_DEVICE, session, "移动设备到其它会场", "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: generateGroups
	 * @Description: 快速生成会场列表（一个设备一个会场）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("generateGroups")
	
	public Map<String, Object> generateGroups(String groupId, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("groupId", groupId);//用户组ID
			resultMap = deviceService.generateGroups(paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "自动生成会场失败");
			logger.error("快速生成会场失败", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.GEN_DEV_GROUPS, session, "快速生成会场，用户组："+groupId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.GEN_DEV_GROUPS, session, "快速生成会场，用户组："+groupId, "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: add2Group
	 * @Description: 往会场添加设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("add2Group")
	
	public Map<String, Object> add2Group(String groupId, String devIds) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("groupId", groupId);//会场ID
			String[] deviceuuids = devIds.split(",");
			paramsMap.put("devIds", deviceuuids);//设备ID数组
			int ret = deviceService.add2Group(paramsMap);
			if(ret > 0){
				resultMap.put("result", true);
			}else{
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("往会场添加设备失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: searchDev
	 * @Description: 模糊查询设备（包含行政结构）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("searchDev")
	
	public Map<String, Object> searchDev(String id, String name) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("id", id);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			List<DeviceVO> deviceList = deviceService.getDevices(paramsMap);
			List<String> regionIds = new ArrayList<String>();
			for(DeviceVO device : deviceList){
				regionIds.add(device.getRegionId());
			}
			List<RegionVO> regionList = new ArrayList<RegionVO>();
			if(regionIds.size() > 0){
				paramsMap.put("regionIds", regionIds);
				regionList = deviceService.searchRegion(paramsMap);
			}
			resultMap.put("deviceList", deviceList);
			resultMap.put("regionList", regionList);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("查询设备失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: searchDevice
	 * @Description: 模糊查询设备（通过设备名称、设备号码、用户组ID、行政结构ID、授权模式、显示模式查询）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("searchDevice")
	
	public Map<String, Object> searchDevice(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String devId, String devName, Integer authMode, Integer showMode, 
			String groupId, String regionId, String regionName, Integer clickType) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("devId", devId);//设备号码
			if (StringUtils.isNotBlank(devName)) {//设备名称
				paramsMap.put("devName", URLDecoder.decode(devName, "utf-8"));
			}
			if (StringUtils.isNotBlank(regionName)) {//区域名称
				paramsMap.put("regionName", URLDecoder.decode(regionName, "utf-8"));
			}
			paramsMap.put("groupId", groupId);//用户组ID
			//查询设备列表
			if(authMode == null && showMode == null){
				paramsMap.put("regionId", regionId);//区域ID
				if(regionId != null && regionId.toString().length() < 12){// 行政区域ID长度为12位，<12说明是成员单位的pif
					if(!paramsMap.get("regionId").equals("-1")){
						paramsMap.remove("regionId");//区域ID
					}
					paramsMap.put("unitId", regionId);//成员单位ID
					paramsMap.put("unitName", URLDecoder.decode(regionName, "utf-8"));//成员单位ID
				}
				List<DeviceVO> list = deviceService.getAuthedOfRegion(paramsMap);
				resultMap.put("deviceList", list);
				if(pageSize != -1){
					int total = deviceService.getAuthedOfRegionCount(paramsMap);
					total = total % pageSize == 0 ? total / pageSize
							: (total / pageSize) + 1;
					resultMap.put("pageTotal", total);
				}
				resultMap.put("result", true);
			}else if(authMode == null || authMode == 0){//已授权（或所有）
				if(authMode != null && showMode == 0){//仅本节点
					paramsMap.put("regionId", regionId);//区域ID
					List<DeviceVO> list = deviceService.getAuthedOfRegion(paramsMap);
					resultMap.put("deviceList", list);
					if(pageSize != -1){
						int total = deviceService.getAuthedOfRegionCount(paramsMap);
						total = total % pageSize == 0 ? total / pageSize
								: (total / pageSize) + 1;
						resultMap.put("pageTotal", total);
					}
					resultMap.put("result", true);
				}else{//本节点及子节点
					if(StringUtils.isNotBlank(regionId)){
						paramsMap.put("regionId", RegionUtil.getBase(regionId));
					}
					List<DeviceVO> list = deviceService.getAuthedOfRegions(paramsMap);
					resultMap.put("deviceList", list);
					if(pageSize != -1){
						int total = deviceService.getAuthedOfRegionsCount(paramsMap);
						total = total % pageSize == 0 ? total / pageSize
								: (total / pageSize) + 1;
						resultMap.put("pageTotal", total);
					}
				}
			}else{//未授权
				if(showMode != null && showMode == 0){//仅本节点
					paramsMap.put("regionId", regionId);
					if(regionId != null && regionId.toString().length() < 12){// 行政区域ID长度为12位，<12说明是成员单位的pif
						if(!paramsMap.get("regionId").equals("-1")){
							paramsMap.remove("regionId");//区域ID
						}
						paramsMap.put("unitId", regionId);//成员单位ID
						paramsMap.put("unitName", URLDecoder.decode(regionName, "utf-8"));//成员单位ID
					}
					List<DeviceVO> list = deviceService.getNotAuthedOfRegion(paramsMap);
					resultMap.put("deviceList", list);
					if(pageSize != -1){
						int total = deviceService.getNotAuthedOfRegionCount(paramsMap);
						total = total % pageSize == 0 ? total / pageSize
								: (total / pageSize) + 1;
						resultMap.put("pageTotal", total);
					}
				}else{//本节点及子节点
					if(StringUtils.isNotBlank(regionId)){
						paramsMap.put("regionId", RegionUtil.getBase(regionId));
						if(regionId != null && regionId.toString().length() < 12){// 行政区域ID长度为12位，<12说明是成员单位的pif
							if(!paramsMap.get("regionId").equals("-1")){
								paramsMap.remove("regionId");//区域ID
							}
							paramsMap.put("unitId", regionId);//成员单位ID
							paramsMap.put("unitName", URLDecoder.decode(regionName, "utf-8"));//成员单位ID
						}
					}
					List<DeviceVO> list = deviceService.getNotAuthedOfRegions(paramsMap);
					resultMap.put("deviceList", list);
					if(pageSize != -1){
						int total = deviceService.getNotAuthedOfRegionsCount(paramsMap);
						total = total % pageSize == 0 ? total / pageSize
								: (total / pageSize) + 1;
						resultMap.put("pageTotal", total);
					}
				}
			}
			//查询行政区域列表（只有点击左侧节点且查询条件为空的时候才不查询）
			if ((clickType == null || clickType != 1) && (StringUtils.isNotBlank(regionName) 
					|| StringUtils.isNotBlank(devId) || StringUtils.isNotBlank(devName))) {
				// 查询行政区域目录
				List<RegionVO> regionListAll = new ArrayList<RegionVO>();
				List<RegionVO> regionList = new ArrayList<RegionVO>();
				paramsMap.put("name", URLDecoder.decode(regionName, "utf-8"));
				paramsMap.put("id", regionId);//行政区域ID
				List<RegionVO> list = deviceService.getRegions(paramsMap);
				List<String> regionIds = new ArrayList<String>();
				for(RegionVO region : list){
					regionIds.add(region.getId());
				}
				if(regionIds.size() > 0){
					paramsMap.put("regionIds", regionIds);
					regionList = deviceService.searchRegion(paramsMap);
				}
				resultMap.put("regionList", regionList);
				List<RegionVO> list2 = new ArrayList<RegionVO>();
				for (int i = 0; i < regionList.size(); i++) {
					if (regionList.get(i).getPid().equals("000000000000")) {
						list2.add(regionList.get(i));
					}
				}
				resultMap.put("regionList2", list2);
				
				// 查询成员单位目录
				List<RegionVO> unitList = new ArrayList<RegionVO>();
				paramsMap.put("regionName", URLDecoder.decode(regionName, "utf-8"));
				paramsMap.put("regionid", regionId);//行政区域ID
				List<RegionVO> units = deviceService.getUnits(paramsMap);
				List<String> unitIds = new ArrayList<String>();//用于查询成员单位父节点
				List<String> existedIds = new ArrayList<String>();//用于记录已存在的成员单位信息
				Iterator<RegionVO> it = units.iterator();
				while(it.hasNext()){
					RegionVO region = it.next();
					if(!existedIds.contains(region.getId())){
						existedIds.add(region.getId());
					}
					if(region.getPid().equals("-1")){//如果已经是根节点，则不需要继续查询其父节点
						region.setPid(region.getRegionId());
//						it.remove();//把父节点删除，因为后面的查询会再次查询到父节点。防止重复数据
						continue;
					}
					if(!unitIds.contains(region.getPid())){
						unitIds.add(region.getPid());
					}
				}
				if(unitIds.size() > 0){
					paramsMap.put("unitIds", unitIds);
					unitList = deviceService.searchUnit(paramsMap);
				}
				// 数据去重及合并
				for(RegionVO region : unitList){
					if(!existedIds.contains(region.getId())){
						units.add(region);
					}
				}
				regionListAll.addAll(units);
				regionListAll.addAll(regionList);
				resultMap.put("regionList", regionListAll);
			}
			
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("查询设备失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: searchRegion
	 * @Description: 模糊查询行政结构
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("searchRegion")
	
	public Map<String, Object> searchRegion(String id, String name) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("id", id);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			} else {
				resultMap.put("result", false);
				resultMap.put("errmsg", "请输入行政结构名称");
				return resultMap;
			}
			List<RegionVO> list = deviceService.getRegions(paramsMap);
			List<String> regionIds = new ArrayList<String>();
			for(RegionVO region : list){
				regionIds.add(region.getId());
			}
			List<RegionVO> regionList = new ArrayList<RegionVO>();
			if(regionIds.size() > 0){
				paramsMap.put("regionIds", regionIds);
				regionList = deviceService.searchRegion(paramsMap);
			}
			resultMap.put("list", regionList);
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("查询行政结构失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getLock
	 * @Description: 获取用户组锁定状态
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("getLock")
	
	public Map<String, Object> getLock(@RequestParam(value = "access_token") String token,String groupId, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		UserVO userData = new UserVO();
		try {
			paramsMap.put("groupId", groupId);
			List<UserVO> list = deviceService.getLock(paramsMap);
			ReturnData data = userService.getUser(token);
			if (!data.getErrcode().equals(0)) {
				resultMap.put("result", false);
				resultMap.put("errmsg", data.getErrmsg());
				return resultMap;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>)data.getData();
			userData = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
			if(list.size() == 1){//一个用户组同一时间只能被一个人锁定
				if(list.get(0).getUuid().equals(userData.getUuid())){//被自己解锁
					resultMap.put("status", 1);//被自己锁定
				}else{
					resultMap.put("status", 2);//被别人锁定
				}
			}else{
				resultMap.put("status", 0);//无人锁定
			}
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("查询用户组锁定状态失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: lock
	 * @Description: 锁定用户组
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("lock")
	
	public Map<String, Object> lock(@RequestParam(value = "access_token") String token,String groupId, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		UserVO userData = new UserVO();
		resultMap.put("status", 0);//锁定失败
		resultMap.put("errmsg", "锁定失败");
		try {
			paramsMap.put("groupId", groupId);
			ReturnData data = userService.getUser(token);
			if (!data.getErrcode().equals(0)) {
				resultMap.put("result", false);
				resultMap.put("errmsg", data.getErrmsg());
				return resultMap;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>)data.getData();
			userData = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
			paramsMap.put("userId", userData.getUuid());
			List<UserVO> list = deviceService.getLock(paramsMap);
			if(list.size() == 1){//一个用户组同一时间只能被一个人锁定
				if(list.get(0).getUuid().equals(session.getAttribute("userId"))){//被自己锁定
					resultMap.put("status", 1);//被自己锁定（锁定成功）
					resultMap.put("errmsg", "锁定成功。编辑完成后请解锁，否则其他人将无法编辑");
				}else{
					resultMap.put("status", 2);//被别人锁定（锁定失败）
					resultMap.put("errmsg", "锁定失败，已被" + list.get(0).getLoginName() + "锁定！");
				}
			}else{
				int ret = deviceService.lock(paramsMap);
				if(ret > 0){
					resultMap.put("status", 1);//被自己锁定（锁定成功）
					resultMap.put("errmsg", "锁定成功。编辑完成后请解锁，否则其他人将无法编辑");
				}
			}
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("锁定用户组失败", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.LOCK_USER_GROUP, session, "锁定用户组："+groupId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.LOCK_USER_GROUP, session, "锁定用户组："+groupId, "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: unlock
	 * @Description: 解锁用户组
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("unlock")
	
	public Map<String, Object> unlock(String groupId, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		resultMap.put("status", 0);//解锁失败
		resultMap.put("errmsg", "解锁失败");
		try {
			paramsMap.put("groupId", groupId);
			int ret = deviceService.unlock(paramsMap);
			if(ret > 0){
				resultMap.put("status", 1);//解锁成功
				resultMap.put("errmsg", "解锁成功");
			}
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("解锁用户组失败", e);
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.UNLOCK_USER_GROUP, session, "解锁用户组："+groupId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.UNLOCK_USER_GROUP, session, "解锁用户组："+groupId, "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: avaliableDevices
	 * @Description: 获取所有未分组设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("avaliableDevices")
	
	public Map<String, Object> avaliableDevices(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String name, String groupId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("name", name);
			paramsMap.put("groupId", groupId);
			List<DeviceVO> userList = deviceService.avaliableDevices(paramsMap);
			resultMap.put("list", userList);
			if(pageSize == -1){//不分页
				resultMap.put("pageTotal", 1);
			} else {
				int total = deviceService.avaliableDevicesCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
			}
			resultMap.put("result", true);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取所有未分组设备失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: devicesInGroup
	 * @Description: 获取会场内已添加设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("devicesInGroup")
	
	public Map<String, Object> devicesInGroup(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String groupId, String devId, String devName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("devGroupId", groupId);//会场ID
			paramsMap.put("devId", devId);//设备ID
			paramsMap.put("devName", devName);//设备名称
			List<DeviceVO> userList = deviceService.devicesInGroup(paramsMap);
			if(pageSize != -1){
				int total = deviceService.getDeviceListCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize
						: (total / pageSize) + 1;
				resultMap.put("pageTotal", total);
			}
			resultMap.put("list", userList);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取群内联系人失败，系统内部异常");
			logger.error("获取会场内已添加设备失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: addDevice
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("addDevice")
	
	public Map<String, Object> addDevice(@ModelAttribute DeviceVO device, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (null != device) {
				if (StringUtils.isNotBlank(device.getName())) {
					device.setName(URLDecoder.decode(device.getName(), "utf-8"));
				}

				if (StringUtils.isNotBlank(device.getDescription())) {
					device.setDescription(URLDecoder.decode(
							device.getDescription(), "utf-8"));
				}
				int result = deviceService.addDevice(device);
				if (result > 0) {
					resultMap.put("result", true);
				} else {
					resultMap.put("result", false);
				}
			} else {
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("添加设备失败，msg：", e);
			e.printStackTrace();
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.ADD_DEVICE, session, "新增设备："+device.getId(), "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.ADD_DEVICE, session, "新增设备："+device.getId(), "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: deleteDevice
	 * @Description: 删除设备
	 * @param @param deviceid
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("deleteDevice")
	
	public Map<String, Object> deleteDevice(String uuid, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (StringUtils.isNotBlank(uuid)) {
				String[] uuids = uuid.split(",");
				if (uuids.length > 0) {
					paramsMap.put("uuids", uuids);
					int result = deviceService.deleteDevice(paramsMap);
					if (result > 0) {
						resultMap.put("result", true);
					} else {
						resultMap.put("result", false);
					}
				} else {
					resultMap.put("result", false);
				}
			} else {
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("删除设备失败，msg：", e);
			e.printStackTrace();
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.DEL_DEVICE, session, "删除设备："+uuid, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.DEL_DEVICE, session, "删除设备："+uuid, "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: updateDevice
	 * @Description: 更新设备
	 * @param @param device
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("updateDevice")
	
	public Map<String, Object> updateDevice(@ModelAttribute DeviceVO device, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (null != device) {
				if (StringUtils.isNotBlank(device.getName())) {
					device.setName(URLDecoder.decode(device.getName(), "utf-8"));
				}
				if (StringUtils.isNotBlank(device.getAlias())) {
					device.setAlias(URLDecoder.decode(device.getAlias(), "utf-8"));
				}
				if (StringUtils.isNotBlank(device.getDescription())) {
					device.setDescription(URLDecoder.decode(
							device.getDescription(), "utf-8"));
				}
				int result = deviceService.updateDevice(device);
				if (result > 0) {
					resultMap.put("result", true);
					resultMap.put("errmsg", "修改成功");
				} else {
					resultMap.put("result", false);
					resultMap.put("errmsg", "修改失败");
				}
			} else {
				resultMap.put("result", false);
				resultMap.put("errmsg", "参数为空");
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "系统内部异常");
			logger.error("更新设备失败，msg：", e);
			e.printStackTrace();
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.EDIT_DEVICE, session, "修改设备："+device.getId(), "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.EDIT_DEVICE, session, "修改设备："+device.getId(), "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: checkRepeatName
	 * @Description: 验证设备名称重复
	 * @param @param uuid
	 * @param @param name
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("checkRepeatName")
	
	public Map<String, Object> checkRepeatName(String name, String id) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("id", id);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
				int result = deviceService.checkRepeatName(paramsMap);
				if (result > 0) {
					resultMap.put("result", true);
				} else {
					resultMap.put("result", false);
				}
			} else {
				
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("设备名重复", e);
			e.printStackTrace();
		}
		return resultMap;
	}
	/**
	 * 
	 * @Title: checkRepeatId
	 * @Description: 验证设备号码重复
	 * @param @param uuid
	 * @param @param name
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("checkRepeatId")
	
	public Map<String, Object> checkRepeatId(String id) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (StringUtils.isNotBlank(id)) {
				paramsMap.put("id", id);
				int result = deviceService.checkRepeatId(paramsMap);
				if (result > 0) {
					resultMap.put("result", true);
				} else {
					resultMap.put("result", false);
				}
			} else {
				
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("设备组名重复", e);
			e.printStackTrace();
		}
		return resultMap;
	}
	/**
	 * 
	 * @Title: checkGroupName
	 * @Description: 验证设会场名称重复
	 * @param @param uuid
	 * @param @param name
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("checkRepeatGroup")
	
	public Map<String, Object> checkRepeatGroup(String name, String groupId, String userGroupId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("groupId", groupId);
			paramsMap.put("userGroupId", userGroupId);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			int result = deviceService.checkRepeatGroup(paramsMap);
			if (result > 0) {
				resultMap.put("result", true);
			} else {
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("会场名重复", e);
			e.printStackTrace();
		}
		return resultMap;
	}

	@RequestMapping("testFile")
	public String testFile(ModelMap map) {
		return "testFile";
	}
	/**
	 * 
	 * @Title: upLoadExcel
	 * @Description: 解析上传的excel文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("uploadExcel")
	
	public Map<String, Object> upLoadExcel(HttpServletRequest request, HttpSession session){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		InputStream in = null;
		try {
	        //创建一个通用的多部分解析器    
			CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());    
			//判断 request 是否有文件上传,即多部分请求    
			if(multipartResolver.isMultipart(request)){    
			    //转换成多部分request      
			    MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;  
			    // 取得request中的所有文件名  
			    Iterator<String> iter = multiRequest.getFileNames();  
			    MultipartFile file = null;
			    String myFileName = null;
			    while (iter.hasNext()) {  
			        // 取得上传文件  
			        file = multiRequest.getFile(iter.next());  
			        // 数据封装操作 MultipartFile reqeust  
			        // 取得当前上传文件的文 件名称  
			        myFileName = file.getOriginalFilename(); //这里需要对文件进行处理 
			        in = file.getInputStream();
			        resultMap.put("result", deviceService.upLoadExcel(in, myFileName));
			    }  
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("导入excel文件失败", e);
		} finally {
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					logger.error("关闭输入流失败", e);
				}
			}
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.ADD_DEVICES, session, "批量导入设备", "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.ADD_DEVICES, session, "批量导入设备", "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: downLoadExcel
	 * @Description: 下载excel文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("downLoadExcel")
	
	public void downLoadExcel(HttpServletRequest request, HttpServletResponse response){
		String fileName="设备列表";
        //填充projects数据
        List<Map<String,Object>> list= deviceService.createExcelRecord();
        String columnNames[]={"号码","名称","MAC","IP","备注","类型"};//列名
        String keys[]   =    {"id","name","mac","ip","description","type"};//map中的key
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            ExcelUtil.createWorkBook(list,keys,columnNames).write(os);
	        byte[] content = os.toByteArray();
	        InputStream is = new ByteArrayInputStream(content);
	        // 设置response参数，可以打开下载页面
	        response.reset();
	        response.setContentType("application/vnd.ms-excel;charset=utf-8");
	        response.setHeader("Content-Disposition", "attachment;filename="+ new String((fileName + ".xls").getBytes(), "iso-8859-1"));
	        ServletOutputStream out = response.getOutputStream();
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(out);
            byte[] buff = new byte[2048];
            int bytesRead;
            // Simple read/write loop.
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
        	try {
	            if (bis != null)
					bis.close();
	            if (bos != null)
	                bos.close();
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
	}

	/**
	 * 
	 * @Title: goGroupList
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("showGroupList")
	public String goGroup(ModelMap map) {
		return "/device/deviceGroupList";
	}

	@RequestMapping("deviceGroupList")
	
	public Map<String, Object> getDeviceGroupList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String name, String temporarily, String description) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("temporarily", temporarily);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			if (StringUtils.isNotBlank(description)) {
				paramsMap.put("description",
						URLDecoder.decode(description, "utf-8"));
			}
			List<DeviceGroupVO> deviceGroupList = deviceService.getDeviceGroupList(paramsMap);
			int total = deviceService.getDeviceGroupListCount(paramsMap);
			total = total % pageSize == 0 ? total / pageSize
					: (total / pageSize) + 1;
			resultMap.put("pageNum", pageNum);
			resultMap.put("pageTotal", total);
			resultMap.put("list", deviceGroupList);
			resultMap.put("result", true);

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取会场列表失败", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: allGroups
	 * @Description: 获取所有会场
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("allGroups")
	
	public Map<String, Object> getAllGroups(String name) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			List<DeviceGroupVO> groupList = deviceService.getAllGroups(paramsMap);
			resultMap.put("list", groupList);
			resultMap.put("result", true);

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取所有用户组失败， msg: ", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: addDeviceGroup
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("addDeviceGroup")
	
	public Map<String, Object> addDeviceGroup(@ModelAttribute DeviceGroupVO deviceGroup, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			if (null != deviceGroup) {
				if (StringUtils.isNotBlank(deviceGroup.getName())) {
					deviceGroup.setName(URLDecoder.decode(deviceGroup.getName(), "utf-8"));
				}
				if (StringUtils.isNotBlank(deviceGroup.getDescription())) {
					deviceGroup.setDescription(URLDecoder.decode(
							deviceGroup.getDescription(), "utf-8"));
				}
				int result = deviceService.addDeviceGroup(deviceGroup);
				if (result > 0) {
					resultMap.put("result", true);
				} else {
					resultMap.put("result", false);
				}
			} else {
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("添加会场失败，msg：", e);
			e.printStackTrace();
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.ADD_DEV_GROUP, session, "新增会场："+deviceGroup.getName(), "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.ADD_DEV_GROUP, session, "新增会场："+deviceGroup.getName(), "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: addDevice2Group
	 * @Description: 添加设备到会场
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("addDevice2Group")
	
	public Map<String, Object> addDevice2Group(String uuid, String devices) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if(StringUtils.isNotBlank(devices))
			{
				String[] deviceuuids = devices.split(",");
				if (deviceuuids.length > 0) {
					paramsMap.put("groupuuid", uuid);
					paramsMap.put("deviceuuids", deviceuuids);
					int result = deviceService.addDevice2Group(paramsMap);
					if (result > 0) {
						resultMap.put("result", true);
						//此处给会议管理服务发送推送消息
						paramsMap.put("type", 1);
						ServerVO ser = sysConfigService.getServer(paramsMap);
						if (null != ser) {
							ObjectMapper objectMapper = new ObjectMapper();
							Map<String, Object> jsonRoot = new HashMap<String, Object>();
							jsonRoot.put("cmd", VSMeet.MSG_DEVGROUP_ADD);
							Map<String, Object> jsonContent = new HashMap<String, Object>();
							String[] array = devices.split(",");
							jsonContent.put("data", array);
							jsonRoot.put("content", jsonContent);
							String json = objectMapper
									.writeValueAsString(jsonRoot);
							SocketClient client = new SocketClient(ser.getIp(),
									Integer.parseInt(ser.getPort()));
							client.start();
							client.sendData(VSMeet.MSG_DEVGROUP_ADD, json.getBytes());
							client.close();
						}
					} else {
						resultMap.put("result", false);
					}
				}
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("添加设备到会场失败，msg：", e);
			e.printStackTrace();
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: deleteDevice2Group
	 * @Description: 从会场删除设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("deleteDevice2Group")
	
	public Map<String, Object> deleteDevice2Group(String uuid, String devices) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if(StringUtils.isNotBlank(devices))
			{
				String[] deviceuuids = devices.split(",");
				if (deviceuuids.length > 0) {
					paramsMap.put("groupuuid", uuid);
					paramsMap.put("deviceuuids", deviceuuids);
					int result = deviceService.deleteDevice2Group(paramsMap);
					if (result > 0) {
						resultMap.put("result", true);
						paramsMap.put("type", 1);
						ServerVO ser = sysConfigService.getServer(paramsMap);
						if (null != ser) {
							ObjectMapper objectMapper = new ObjectMapper();
							Map<String, Object> jsonRoot = new HashMap<String, Object>();
							jsonRoot.put("cmd", VSMeet.MSG_DEVGROUP_DEL);
							Map<String, Object> jsonContent = new HashMap<String, Object>();
							String[] array = devices.split(",");
							jsonContent.put("data", array);
							jsonRoot.put("content", jsonContent);
							String json = objectMapper
									.writeValueAsString(jsonRoot);
							SocketClient client = new SocketClient(ser.getIp(),
									Integer.parseInt(ser.getPort()));
							client.start();
							client.sendData(VSMeet.MSG_DEVGROUP_DEL, json.getBytes(), VSMeet.MSG_PUSH);
							client.close();
						}
					} else {
						resultMap.put("result", false);
					}
				}
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("从会场删除设备失败，msg：", e);
			e.printStackTrace();
		}
		return resultMap;
	}
	/**
	 * 
	 * @Title: deleteDeviceGroup
	 * @Description: 删除会场
	 * @param @param deviceid
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("deleteDeviceGroup")
	
	public Map<String, Object> deleteDeviceGroup(String userGroupId, String devGroupId, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			String[] uuids = devGroupId.split(",");
			paramsMap.put("uuids", uuids);
			paramsMap.put("groupId", userGroupId);
			resultMap = deviceService.deleteDeviceGroup(paramsMap);
		} catch (Exception e) {
			resultMap.put("errmsg", "删除会场失败");
			resultMap.put("result", false);
			logger.error("删除会场失败，msg：", e);
			e.printStackTrace();
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.DEL_DEV_GROUP, session, "删除会场："+devGroupId, "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.DEL_DEV_GROUP, session, "删除会场："+devGroupId, "", LogType.OPERATE_ERROR);
		}*/
		return resultMap;
	}

	/**
	 * 
	 * @Title: updateDeviceGroup
	 * @Description: 更新设备
	 * @param @param device
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("updateDeviceGroup")
	
	public Map<String, Object> updateDeviceGroup(@ModelAttribute DeviceGroupVO deviceGroup, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		String errMsg = "编辑成功";
		try {
			if (null != deviceGroup) {
				if (StringUtils.isNotBlank(deviceGroup.getName())) {
					deviceGroup.setName(URLDecoder.decode(deviceGroup.getName(), "utf-8"));
				}

				paramsMap.put("uuid", deviceGroup.getUuid());
				List<DeviceGroupVO> groupList = deviceService.getAllGroups(paramsMap);
				if(groupList.size() > 0 && groupList.get(0).equals(GlobalConstants.DEFAULT_GROUP_NAME)){//默认会场名称不允许修改
					resultMap.put("errmsg", "默认会场名称不允许修改");
					resultMap.put("result", false);
					return resultMap;
				}
				if (StringUtils.isNotBlank(deviceGroup.getDescription())) {
					deviceGroup.setDescription(URLDecoder.decode(
							deviceGroup.getDescription(), "utf-8"));
				}
				Map<String, Object> rtMap = deviceService.updateDeviceGroup(deviceGroup);
				if (Integer.parseInt(rtMap.get("result").toString()) > 0) {
					resultMap.put("result", true);
				} else {
					errMsg = rtMap.get("errMsg").toString();
					logger.error(errMsg);
					resultMap.put("result", false);
				}
			} else {
				errMsg = "编辑失败";
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			errMsg = "编辑失败";
			resultMap.put("result", false);
			logger.error("更新会场失败，msg：", e);
			e.printStackTrace();
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.EDIT_DEV_GROUP, session, "修改会场："+deviceGroup.getUuid(), "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.EDIT_DEV_GROUP, session, "修改会场："+deviceGroup.getUuid(), "", LogType.OPERATE_ERROR);
		}*/
		resultMap.put("errMsg", errMsg);
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: updateDeviceGroupName
	 * @Description: 更新会场名称
	 * @param @param device
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("updateDeviceGroupName")
	
	public Map<String, Object> updateDeviceGroupName(@ModelAttribute DeviceGroupVO deviceGroup, HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		String errMsg = "编辑成功";
		try {
			if (null != deviceGroup) {
				if (StringUtils.isNotBlank(deviceGroup.getName())) {
					deviceGroup.setName(URLDecoder.decode(deviceGroup.getName(), "utf-8"));
				}

				paramsMap.put("uuid", deviceGroup.getUuid());
				List<DeviceGroupVO> groupList = deviceService.getAllGroups(paramsMap);
				if(groupList.size() > 0 && groupList.get(0).getName().equals(GlobalConstants.DEFAULT_GROUP_NAME)){//默认会场名称不允许修改
					resultMap.put("errmsg", "默认会场名称不允许修改");
					resultMap.put("result", false);
					return resultMap;
				}
				Map<String, Object> rtMap = deviceService.updateDeviceGroupName(deviceGroup);
				if (Integer.parseInt(rtMap.get("result").toString()) > 0) {
					resultMap.put("result", true);
				} else {
					errMsg = rtMap.get("errMsg").toString();
					logger.error(errMsg);
					resultMap.put("result", false);
				}
			} else {
				errMsg = "编辑失败";
				resultMap.put("result", false);
			}
		} catch (Exception e) {
			errMsg = "编辑失败";
			resultMap.put("result", false);
			logger.error("更新会场失败，msg：", e);
			e.printStackTrace();
		}
		/*if((Boolean) resultMap.get("result")){
			LogWritter.writeLog(LogType.EDIT_DEV_GROUP, session, "修改会场："+deviceGroup.getUuid(), "", LogType.OPERATE_OK);
		}else{
			LogWritter.writeLog(LogType.EDIT_DEV_GROUP, session, "修改会场："+deviceGroup.getUuid(), "", LogType.OPERATE_ERROR);
		}*/
		resultMap.put("errMsg", errMsg);
		return resultMap;
	}

	/**
	 * 
	 * @Title: getTypeList
	 * @Description: 获取设备类型列表
	 * @param @param device
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("typeList")
	
	public Map<String, Object> getTypeList() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			List<DeviceTypeVO> typeList = deviceService.getDeviceTypeList();
			resultMap.put("list", typeList);
			resultMap.put("result", true);

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("获取设备类型列表失败， msg: ", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: getDeviceTypeList
	 * @Description: 获取设备类型列表
	 * @param @param device
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceTypeVO> getDeviceTypeList(){
		return deviceService.getDeviceTypeList();
	}
	
	
	/** <pre>getMasterBySchedule(以会议主席设备为单位查询所有以此设备为主席的会议)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 上午10:59:24    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 上午10:59:24    
	 * 修改备注： 
	 * @param pageNum
	 * @param pageSize
	 * @param masterNo
	 * @param masterName
	 * @param startTime
	 * @param endTime
	 * @return</pre>    
	 */
	@RequestMapping("getMasterBySchedule")
	
	public Map<String, Object> getMasterBySchedule(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String masterNo, String masterName, String startTime, String endTime) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("id", masterNo);
			paramsMap.put("masterName", masterName == null ? masterName
					: URLDecoder.decode(masterName, "utf-8"));
			paramsMap.put("startTime",startTime);
			paramsMap.put("endTime", endTime);
			List<ScheduleVO> deviceList = deviceService.getMasterBySchedule(paramsMap);
			int total = deviceService.getMasterByScheduleCount(paramsMap);
			total = total % pageSize == 0 ? total / pageSize
					: (total / pageSize) + 1;
			resultMap.put("pageNum", pageNum);
			resultMap.put("pageTotal", total);
			resultMap.put("list", deviceList);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取设备列表失败", e);
		}
		return resultMap;
	}
	
	
	
	/** <pre>getMasterDetail(获取会议详情信息)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 下午1:02:06    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 下午1:02:06    
	 * 修改备注： 
	 * @param scheduleId
	 * @return</pre>    
	 */
	@RequestMapping("getMasterDetail")
	
	public Map<String, Object> getMasterDetail(String scheduleId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (StringUtils.isBlank(scheduleId)) {
				resultMap.put("result", false);
				resultMap.put("errmsg", "预约id不可为null");
				return resultMap;
			}
			paramsMap.put("scheduleId", scheduleId);
			List<ScheduleVO> scheduleList = deviceService.getMasterDetail(paramsMap);
			List<DeviceVO> devListList = deviceService.getScheduleDevice(paramsMap);
			scheduleList.get(0).setDevList(devListList);
			resultMap.put("scheduleList", scheduleList);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取会议详情列表失败", e);
		}
		return resultMap;
	}
}
