package com.visionvera.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.basecrud.CrudService;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.Nmsserver;
import com.visionvera.dao.datacore.NmsserverDao;
import com.visionvera.service.NmsserverService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
@Service("nmsserverService")
@Transactional(value = "transactionManager_dataCore", rollbackFor = Exception.class)
public class NmsserverServiceImpl extends CrudService<NmsserverDao, Nmsserver> implements NmsserverService {

	 private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public ReturnData getNmsServersRegionList(Map<String,Object> params) {
		BaseReturn baseReturn = new BaseReturn();
		try {
			List<Map<String,Object>> list = super.dao.getRegionList(params);
			return baseReturn.returnResult(0, "获取成功", null, list);
			
		} catch (Exception e) {
			logger.error("NmsserverServiceImpl--getNmsServersRegionList",e);
		}
		
		return baseReturn.returnError("获取失败");
	}

	@Override
	public ReturnData getNmsServersAndRegionList(Map<String, Object> params) {
		BaseReturn baseReturn = new BaseReturn();
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			//行政区域列表
			List<Map<String,Object>> regionList = super.dao.getRegionList(params);
			//服务器列表
			List<Map<String,Object>> serverList = super.dao.getNmsServerList(params);
			resultMap.put("regionList", regionList);
			resultMap.put("serverList", serverList);
			return baseReturn.returnResult(0, "获取成功", null, null,resultMap);
			
		} catch (Exception e) {
			logger.error("NmsserverServiceImpl--getNmsServersRegionList",e);
		}
		
		return baseReturn.returnError("获取失败");
	}

}
