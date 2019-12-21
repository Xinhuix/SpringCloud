package com.visionvera.service;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.RegionVO;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @date 2018年11月23日 10:23
 */
public interface SerachService {
    /**
	 * 查询不同区号的服务器信息
	 * @param devZoneIds 查询条件
	 * @return
	 */
	public List<Map<String, Object>> getZoneServers(List<Map<String, Object>>  deviceList);

	/**
	 * 通过行政区域ID获取行政区域信息
	 * @param regionId
	 * @return
	 */
	public RegionVO getRegionByRegionId(String regionId);
}
