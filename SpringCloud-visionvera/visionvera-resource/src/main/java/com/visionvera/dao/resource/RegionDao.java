package com.visionvera.dao.resource;

import java.util.List;

import com.visionvera.bean.datacore.TRegionb;

/**
 * 行政区域
 *
 */
public interface RegionDao {
	/**
	 * 获取行政区域列表
	 * @param paramsMap
	 * @return
	 */
	public List<TRegionb> selectRegions(TRegionb region);
}
