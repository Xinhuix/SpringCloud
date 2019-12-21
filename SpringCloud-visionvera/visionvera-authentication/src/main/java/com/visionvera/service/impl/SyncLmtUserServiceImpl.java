package com.visionvera.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.LmtUserVo;
import com.visionvera.bean.datacore.ServerConfig;
import com.visionvera.bean.restful.client.RestClient;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.authentication.ServerConfigDao;
import com.visionvera.service.SyncLmtUserService;

@Service
public class SyncLmtUserServiceImpl implements SyncLmtUserService {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ServerConfigDao serverConfigDao;
	
	/**
	 * 添加用户同步至流媒体
	 */
	@Async
	public Map<String,Object> lmtUserAdd(UserVO user){
		LOGGER.info("调用流媒体用户注册接口开始-------------------");
		Map<String, Object> resultMap = new HashMap<String,Object>();
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		try {
			List<ServerConfig> configList =serverConfigDao.queryLmtConfigList();
			if(configList==null||configList.size()==0){
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "流媒体配置信息不能为空");
				return resultMap;
			}
			for(ServerConfig config :configList){
				LmtUserVo lmtUser = new LmtUserVo();
				lmtUser.setUuid(user.getUuid());
				lmtUser.setLoginName(user.getLoginName());//用户登录名
				lmtUser.setLoginPwd(user.getLoginPwd());//用户密码
				lmtUser.setPhone(user.getPhone());//用户手机号
				lmtUser.setRealName(user.getName());//用户真实姓名
				lmtUser.setAreaId(user.getAreaId());//用户行政区域ID
				lmtUser.setAreaName(user.getAreaName());//用户行政区域名称
				Map<String, Object> mapWithoutNull = this.getMapWithoutNull(lmtUser);
				mapWithoutNull.put("token", config.getToken());
				mapWithoutNull.put("platformId", config.getPlatformId());
				Map<String, Object> result =RestClient.post(config.getLmtUrl() + String.format(WsConstants.URL_LMT_PLATFORMUSER_REGISTER), null, mapWithoutNull);
				LOGGER.info("流媒体用户注册返回结果：{}", JSONObject.toJSONString(result));
				resultList.add(result);
			}
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "访问远端服务器成功");
			resultMap.put("data", resultList);
			return resultMap;
		} catch (Exception e) {
			LOGGER.error("流媒体用户注册系统异常:", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "访问远端服务器失败");
		}
		LOGGER.info("调用流媒体用户注册接口结束-------------------");
		return resultMap;
	}
	
	@Override
	@Async
	public Map<String,Object> lmtUpdateUser(UserVO user) {
		LOGGER.info("调用流媒体用户修改接口开始-------------------");
		Map<String, Object> resultMap = new HashMap<String,Object>();
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		try {
			List<ServerConfig> configList =serverConfigDao.queryLmtConfigList();
			if(configList==null||configList.size()==0){
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "流媒体配置信息不能为空");
				return resultMap;
			}
			for(ServerConfig config :configList){
				LmtUserVo lmtUser = new LmtUserVo();
				lmtUser.setUuid(user.getUuid());
				lmtUser.setLoginName(user.getLoginName());//用户登录名
				lmtUser.setLoginPwd(user.getLoginPwd());//用户密码
				lmtUser.setPhone(user.getPhone());//用户手机号
				lmtUser.setRealName(user.getName());//用户真实姓名
				lmtUser.setPlatformId(user.getPlatformId());
				lmtUser.setAreaId(user.getAreaId());//用户行政区域ID
				lmtUser.setAreaName(user.getAreaName());//用户行政区域名称
				Map<String, Object> mapWithoutNull = this.getMapWithoutNull(lmtUser);
				mapWithoutNull.put("token", config.getToken());
				mapWithoutNull.put("platformId", config.getPlatformId());
				//String json = JSON.toJSONString(mapWithoutNull);
				Map<String, Object> result =RestClient.post(config.getLmtUrl() + String.format(WsConstants.URL_LMT_PLATFORMUSER_UPDATEUSER), null, mapWithoutNull);
				LOGGER.info("流媒体用户修改返回结果："+result);
				resultList.add(result);
			}
			
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "访问远端服务器成功");
			resultMap.put("data", resultList);
			LOGGER.info("调用流媒体用户修改接口结束-------------------");
			return resultMap;
		} catch (Exception e) {
			LOGGER.error("流媒体用户修改系统异常:"+e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "访问远端服务器失败");
		}
		return resultMap;
		
	}

	@Override
	@Async
	public Map<String,Object> lmtDeleteUser(List<UserVO> list) {
		LOGGER.info("调用流媒体用户删除接口开始-------------------");
		Map<String, Object> resultMap = new HashMap<String,Object>();
		Map<String, Object> params = new HashMap<String,Object>();
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		try {
			if(list==null||list.size()==0){
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "删除的用户不存在");
				return resultMap;
			}
			List<ServerConfig> configList =serverConfigDao.queryLmtConfigList();
			if(configList==null||configList.size()==0){
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "流媒体配置信息不能为空");
				return resultMap;
			}
            for(ServerConfig config :configList){
    			for(UserVO user:list){
    				params.put("loginName", user.getLoginName());
    				params.put("token", config.getToken());
    				params.put("platformId", config.getPlatformId());
    				Map<String, Object> result =RestClient.post(config.getLmtUrl() + String.format(WsConstants.URL_LMT_PLATFORMUSER_DELETEUSER), null, params);
    				LOGGER.info("流媒体用户删除返回结果："+result);	
    				resultList.add(result);
			}
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "访问远端服务器成功");
			resultMap.put("data", resultList);
			}
		} catch (Exception e) {
			LOGGER.error("流媒体用户删除系统异常:"+e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "访问远端服务器失败");
		}
		LOGGER.info("调用流媒体用户删除接口结束-------------------");
		return resultMap;
		
		
	}
	
	@Override
	public Map<String, Object> lmtLoginUser(UserVO user) {
		Map<String, Object> resultMap = new HashMap<String,Object>();
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		try {
			List<ServerConfig> configList =serverConfigDao.queryLmtConfigList();
			if(configList==null||configList.size()==0){
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "流媒体配置信息不能为空");
				return resultMap;
			}
			for(ServerConfig config :configList){
				Map<String, Object> mapWithoutNull = this.getMapWithoutNull(user);
				mapWithoutNull.put("loginType", 1);
				Map<String, Object> result =RestClient.post(config.getLmtUrl() + String.format(WsConstants.URL_LMT_PLATFORMUSER_LOGIN), null, mapWithoutNull);
				resultList.add(result);
				LOGGER.info("流媒体用户登录返回结果："+result);	
			}
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "访问远端服务器成功");
			resultMap.put("data", resultList);
			
		} catch (Exception e) {
			LOGGER.error("流媒体用户登录系统异常:"+e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "访问远端服务器失败");
		}
		return resultMap;
		
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getMapWithoutNull(Object args) {
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	
    	if (args != null) {
    		String jsonStr = JSONObject.toJSONString(args);//去掉NULL
    		resultMap = JSONObject.parseObject(jsonStr, Map.class);
    	}
    	
    	return resultMap;
    }
	
}
