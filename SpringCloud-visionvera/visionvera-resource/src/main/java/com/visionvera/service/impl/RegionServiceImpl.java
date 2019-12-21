package com.visionvera.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.TRegionb;
import com.visionvera.dao.resource.RegionDao;
import com.visionvera.service.RegionService;

@Service
@Transactional(transactionManager = "transactionManager_resource", rollbackFor = Exception.class)
public class RegionServiceImpl implements RegionService {
	@Autowired
	private RegionDao regionDao;
	
	/**
	 * 通过条件获取行政区域列表
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param region 条件
	 * @return
	 */
	@Override
	public PageInfo<TRegionb> getRegionList(boolean isPage, Integer pageNum, Integer pageSize, TRegionb region) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<TRegionb> regionList = this.regionDao.selectRegions(region);
		PageInfo<TRegionb> regionInfo = new PageInfo<TRegionb>(regionList);
		return regionInfo;
	}
}
