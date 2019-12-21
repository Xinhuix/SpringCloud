package com.visionvera.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.ywcore.SenseDao;
import com.visionvera.service.SenseService;

/**
 * 感知中心的数据业务
 *
 */
@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class SenseServiceImpl implements SenseService {
	@Autowired
	private SenseDao senseDao;
	
	
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
	public Map<String, Object> getServerRateTrend(String mac, Integer minute,String ip) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try{
			paramsMap.put("minute", minute);
			paramsMap.put("mac", mac);
			paramsMap.put("ip", ip);
			//根据开始时间去数据库查询对应的数据
			List<Map<String, String>> datas = senseDao.getServerRateTrend(paramsMap);
			resultMap.put("items", datas);
			resultMap.put("errcode", WsConstants.OK);
			resultMap.put("errmsg", "获取数据成功");
		}catch(Exception e){
			resultMap.put("errcode", WsConstants.ERROR);
			resultMap.put("errmsg", "获取数据失败");
		}
		return resultMap;
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
	public Map<String, Object> getProvinceRateTrend(String regionId, String ip, Integer minute) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		
		try{
			paramsMap.put("minute", minute);
			paramsMap.put("regionId", regionId);
			paramsMap.put("ip", ip);
			//根据开始时间去数据库查询对应的数据
			List<Map<String, String>> datas = senseDao.getProvinceRateTrend(paramsMap);
			resultMap.put("items", datas);
			resultMap.put("errcode", WsConstants.OK);
			resultMap.put("errmsg", "获取数据成功");
		}catch(Exception e){
			resultMap.put("errcode", WsConstants.ERROR);
			resultMap.put("errmsg", "获取数据失败");
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> getProvinceRateTrendByProcedure(Integer hour,String ip,String regionId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		
		try{
			paramsMap.put("hour", hour);
			paramsMap.put("regionId", regionId);
			paramsMap.put("ip", ip);
			//根据开始时间去数据库查询对应的数据
			List<Map<String, String>> datas = senseDao.getProvinceRateTrendByHour(paramsMap);
			resultMap.put("items", datas);
			resultMap.put("errcode", WsConstants.OK);
			resultMap.put("errmsg", "获取数据成功");
		}catch(Exception e){
			resultMap.put("errcode", WsConstants.ERROR);
			resultMap.put("errmsg", "获取数据失败");
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> getServerRateTrendByProcedure(Integer hour,String ip,String mac) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try{
			paramsMap.put("hour", hour);
			paramsMap.put("mac", mac);
			paramsMap.put("ip", ip);
			//根据开始时间去数据库查询对应的数据
			List<Map<String, String>> datas = senseDao.getServerRateTrendByHour(paramsMap);
			resultMap.put("items", datas);
			resultMap.put("errcode", WsConstants.OK);
			resultMap.put("errmsg", "获取数据成功");
		}catch(Exception e){
			resultMap.put("errcode", WsConstants.ERROR);
			resultMap.put("errmsg", "获取数据失败");
		}
		return resultMap;
	}
}
