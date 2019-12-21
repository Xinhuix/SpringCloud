/**  
 * @Title: SenseController.java
 * @Package com.visionvera.union.SenseController
 * @Description: TODO
 * @author 谢程算
 * @date 2018年6月13日
 */
package com.visionvera.web.controller.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.restful.client.RestClient;
import com.visionvera.config.SysConfig;
import com.visionvera.constrant.WsConstants;
import com.visionvera.service.SenseService;

/**
 * ClassName: SenseController 
 * @Description: 感知中心接口管理
 * @author 谢程算
 * @date 2018年6月11日 
 */
@RestController  
@RequestMapping("/rest/sense") 
public class SenseController {
	private static final Logger logger = LogManager.getLogger(SenseController.class);
	@Autowired
	private SenseService senseService;
	@Autowired
	private SysConfig sysConfig;
    
    /**
     * @Description: 获取某视联网服务器的流量趋势接口
     * @param @param mac
     * @param @param minute
     * @param @return   
     * @return Map<String,Object>  
     * @throws
     * @author 谢程算
     * @date 2018年6月14日
     */
    @RequestMapping(value = "/rate/server/{mac}/{ip}/trend/{minute}", method = RequestMethod.GET)  
    public Map<String, Object> getServerRateTrend(@PathVariable(value="mac") String mac, @PathVariable(value="minute") Integer minute,@PathVariable(value="ip") String ip){ 
		return senseService.getServerRateTrend(mac, minute,ip);
    }
    /**
     * 获取某视联网服务器的流量趋势接口(通过小时)
     * @date 2018年9月11日 下午6:40:00
     * @author wangqiubao
     * @param hour
     * @return
     */
    @RequestMapping(value = "/rate/server/trend/{hour}/{ip}/{mac}", method = RequestMethod.GET)  
    public Map<String, Object> getServerRateTrendByProcedure(@PathVariable(value="hour") Integer hour,@PathVariable(value="ip") String ip,@PathVariable(value="mac") String mac){ 
		return senseService.getServerRateTrendByProcedure(hour,ip,mac);
    }
    /**
     * @Description: 获取某省的流量趋势接口
     * @param @param regionId
     * @param @param ip
     * @param @param minute
     * @param @return   
     * @return Map<String,Object>  
     * @throws
     * @author 谢程算
     * @date 2018年6月14日
     */
    @RequestMapping(value = "/rate/province/{regionId}/{ip}/trend/{minute}", method = RequestMethod.GET, consumes = "application/json;charset=utf-8")  
    public Map<String, Object> getProvinceRateTrend(@PathVariable(value="regionId") String regionId, @PathVariable(value="ip") String ip, 
    		@PathVariable(value="minute") Integer minute){  
    	return senseService.getProvinceRateTrend(regionId, ip, minute);
    }
    /**
     * 获取某省的流量趋势接口(通过小时)
     * @date 2018年9月11日 下午6:27:23
     * @author wangqiubao
     * @param regionId
     * @param ip
     * @param minute
     * @return
     */
    @RequestMapping(value = "/rate/province/trend/{hour}/{ip}/{regionId}", method = RequestMethod.GET)  
    public Map<String, Object> getProvinceRateTrendByProcedure(@PathVariable(value="hour") Integer hour, @PathVariable(value="ip") String ip,@PathVariable(value="regionId") String regionId){  
    	return senseService.getProvinceRateTrendByProcedure(hour,ip,regionId);
    }
    
    /**
     * @Description: 用户登录接口
     * @return Map<String,Object>  
     */
    @RequestMapping(value = "/userLogon", method = RequestMethod.POST)  
    public Map<String, Object> userLogon(
    		@RequestParam(value = "name") String name,
    		@RequestParam(value = "pwd") String pwd){
    	Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    	resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap =RestClient.post(sysConfig.getContentUrl() + String.format(WsConstants.URL_CONTENT_LOGIN,name,pwd),null, null);
		boolean returnResult = (boolean) paramMap.get("result");
		if(returnResult==true){
			Map<String, Object> mapId = new LinkedHashMap<String, Object>();
			ArrayList<Map<String, Object>> userLogon = new ArrayList<Map<String, Object>>();
			Map<String, Object> data = new HashMap<String, Object>();
			mapId.put("sessionID",paramMap.get("sessionID").toString());
			userLogon.add(mapId);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", paramMap.get("msg").toString());
			resultMap.put("access_token",null);
			data.put("items",userLogon);
			data.put("extra",null);
			resultMap.put("data",data);
		}else{
			resultMap.put("errmsg",paramMap.get("msg").toString());
		}
		return resultMap;
    }
    
    /**
     * @Description: 用户退出接口
     * @return Map<String,Object>  
     */
    @RequestMapping(value = "/userLogout", method = RequestMethod.POST)  
    public Map<String, Object> userLogout(@RequestParam(value = "sessionID") String sessionID){ 
    	Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    	resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap =RestClient.post(sysConfig.getContentUrl() + String.format(WsConstants.URL_CONTENT_LOGOUT,sessionID),null, null);
		boolean returnResult = (boolean) paramMap.get("result");
		if(returnResult==true){
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", paramMap.get("msg").toString());
		}else{
			resultMap.put("errmsg",paramMap.get("msg").toString());
		}
		return resultMap;
    }
    /**
     * @Description: 获取录像资源列表
     * @return Map<String,Object>  
     */
    @RequestMapping(value = "/videoResource/list", method = RequestMethod.POST)  
    public Map<String, Object> videoResourceList(@RequestParam(value = "sessionID") String sessionID,
    		@RequestParam(value = "pageNo",defaultValue="1", required = false) String pageNo,
    		@RequestParam(value = "pageSize",defaultValue="10", required = false) String pageSize,
    		@RequestParam(value = "videoName", required = false) String videoName,
    		@RequestParam(value = "regionName", required = false) String regionName,
    		@RequestParam(value = "createTimeStart", required = false) String createTimeStart,
    		@RequestParam(value = "createTimeEnd", required = false) String createTimeEnd,
    		@RequestParam(value = "createUser", required = false) String createUser,
    		@RequestParam(value = "createTimeSort", required = false) String createTimeSort,
    		@RequestParam(value = "videoId", required = false) String videoId,
    		@RequestParam(value = "fromType", required = false) String fromType){ 
    	Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    	resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("pageNo", pageNo);
    	paramMap.put("pageSize", pageSize);
    	if(sessionID != null)
			paramMap.put("sessionID", sessionID);
    	if(videoName != null)
			paramMap.put("videoName", videoName);
    	if(regionName != null)
			paramMap.put("regionName", regionName);
    	if(createTimeStart != null)
			paramMap.put("createTimeStart", createTimeStart);
    	if(createTimeEnd != null)
			paramMap.put("createTimeEnd", createTimeEnd);
    	if(createUser != null)
			paramMap.put("createUser", createUser);
    	if(createTimeSort != null)
			paramMap.put("createTimeSort", createTimeSort);
    	if(videoId != null)
			paramMap.put("videoId", videoId);
    	if(fromType != null)
			paramMap.put("fromType", fromType);
		paramMap =RestClient.postUrl(sysConfig.getContentUrl() + String.format(WsConstants.URL_VEDIO_GET_BY_PAGE),null, paramMap);
		boolean returnResult = (boolean) paramMap.get("result");
		if(returnResult==true){
			Map<String, Object> data = new LinkedHashMap<String, Object>();
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "获取资源列表成功");
			resultMap.put("access_token",null);
			data.put("pageNo",paramMap.get("pageNo").toString());
			data.put("totalPage",paramMap.get("totalPage").toString());
			data.put("totalCount",paramMap.get("totalCount").toString());
			data.put("items",paramMap.get("list"));
			data.put("extra",null);
			resultMap.put("data",data);
		}else{
			resultMap.put("errmsg", "获取资源列表失败");
		}
		return resultMap;
    }
    
    /**
     * @Description: 手机点播开始接口
     * @return Map<String,Object>  
     */
    @RequestMapping(value = "/videoVodPlay/startPhoneVod", method = RequestMethod.POST)  
    public Map<String, Object> startPhoneVod(
    		@RequestParam(value = "sessionID") String sessionID,
    		@RequestParam(value = "id") Integer id,
    		@RequestParam(value = "from") Integer from,
    		@RequestParam(value = "v2vnum") String v2vnum,
    		@RequestParam(value = "type",required = false) Integer type){ 
    	Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    	resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
		Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("id", id);
    	paramMap.put("from", from);
    	paramMap.put("v2vnum", v2vnum);
    	paramMap.put("sessionID", sessionID);
    	if(type != null)
			paramMap.put("type", type);
    	paramMap =RestClient.postUrl(sysConfig.getContentUrl() + String.format(WsConstants.URL_VEDIO_PLAY_BY_PHONE),null, paramMap);
		boolean returnResult = (boolean) paramMap.get("result");
		if(returnResult==true){
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", paramMap.get("msg").toString());
		}else{
			resultMap.put("errmsg",paramMap.get("msg").toString());
		}
		return resultMap;
    }
    
    /**
     * @Description: 手机点播暂停或继续播放接口
     * @return Map<String,Object>  
     */
    @RequestMapping(value = "/videoVodPlay/operatePhoneVod", method = RequestMethod.POST)  
    public Map<String, Object> operatePhoneVod(
    		@RequestParam(value = "sessionID") String sessionID,
    		@RequestParam(value = "v2vnum") String v2vnum,
    		@RequestParam(value = "type") Integer type){ 
    	Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    	resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("type", type);
    	paramMap.put("v2vnum", v2vnum);
    	paramMap.put("sessionID", sessionID);
    	paramMap =RestClient.postUrl(sysConfig.getContentUrl() + String.format(WsConstants.URL_VEDIO_PAUSE_OR_CONTINUE_BY_PHONE),null, paramMap);
		boolean returnResult = (boolean) paramMap.get("result");
		if(returnResult==true){
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", paramMap.get("msg").toString());
		}else{
			resultMap.put("errmsg",paramMap.get("msg").toString());
		}
		return resultMap;
    }
    
    /**
     * @Description: 手机点播快进或快退(seek)接口
     * @return Map<String,Object>  
     */
    @RequestMapping(value = "/videoVodPlay/seekPhoneVod", method = RequestMethod.POST)  
    public Map<String, Object> seekPhoneVod(
    		@RequestParam(value = "sessionID") String sessionID,
    		@RequestParam(value = "v2vnum") String v2vnum,
    		@RequestParam(value = "type") Integer type,
    		@RequestParam(value = "second")Long second){ 
    	Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    	resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("type", type);
    	paramMap.put("v2vnum", v2vnum);
    	paramMap.put("second", second);
    	paramMap.put("sessionID", sessionID);
    	paramMap =RestClient.postUrl(sysConfig.getContentUrl() + String.format(WsConstants.URL_VEDIO_SEEK_BY_PHONE),null, paramMap);
		boolean returnResult = (boolean) paramMap.get("result");
		if(returnResult==true){
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", paramMap.get("msg").toString());
		}else{
			resultMap.put("errmsg",paramMap.get("msg").toString());
		}
		return resultMap;
    }
    
    /**
     * @Description: 手机点播查询当前播放状态接口
     * @return Map<String,Object>  
     */
    @RequestMapping(value = "/videoVodPlay/queryPhoneVod", method = RequestMethod.POST)  
    public Map<String, Object> queryPhoneVod(
    		@RequestParam(value = "sessionID") String sessionID,
    		@RequestParam(value = "v2vnum") String v2vnum){ 
    	Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
    	resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);
		Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("v2vnum", v2vnum);
    	paramMap.put("sessionID", sessionID);
    	paramMap =RestClient.postUrl(sysConfig.getContentUrl() + String.format(WsConstants.URL_VEDIO_STATUS_GET_BY_PHONE),null, paramMap);
		boolean returnResult = (boolean) paramMap.get("result");
		if(returnResult==true){
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", paramMap.get("msg").toString());
		}else{
			resultMap.put("errmsg",paramMap.get("msg").toString());
		}
		return resultMap;
    }
    
    
    
    
    
    
}



