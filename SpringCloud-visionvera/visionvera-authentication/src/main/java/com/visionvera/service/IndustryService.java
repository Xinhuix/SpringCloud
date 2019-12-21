package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.TIndustrybVO;

/**
 * 行业归属业务接口
 *
 */
public interface IndustryService {
	/**
	 * 获取行业归属信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param industry 查询条件
	 * @return
	 */
	public PageInfo<TIndustrybVO> getIndustryList(boolean isPage, Integer pageNum, Integer pageSize, TIndustrybVO industry);
}
