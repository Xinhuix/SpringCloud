package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.TRegionb;

/**
 * 行政区域业务接口
 *
 */
public interface RegionService {
	/**
	 * 通过条件获取行政区域列表
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param region 条件
	 * @return
	 */
	public PageInfo<TRegionb> getRegionList(boolean isPage, Integer pageNum, Integer pageSize, TRegionb region);
}
