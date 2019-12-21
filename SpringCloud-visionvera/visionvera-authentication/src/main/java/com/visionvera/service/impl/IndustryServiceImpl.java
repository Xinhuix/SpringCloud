package com.visionvera.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.TIndustrybVO;
import com.visionvera.dao.authentication.IndustryDao;
import com.visionvera.service.IndustryService;

/**
 * 行业归属业务接口实现类
 *
 */
@Service
@Transactional(transactionManager = "transactionManager_authentication", rollbackFor = Exception.class)
public class IndustryServiceImpl implements IndustryService {
	@Autowired
	private IndustryDao industryDao;
	
	/**
	 * 获取行业归属信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param industry 查询条件
	 * @return
	 */
	@Override
	public PageInfo<TIndustrybVO> getIndustryList(boolean isPage, Integer pageNum, Integer pageSize,
			TIndustrybVO industry) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<TIndustrybVO> industryList = this.industryDao.selectIndustryByCondition(industry);
		PageInfo<TIndustrybVO> industryInfo = new PageInfo<TIndustrybVO>(industryList);
		return industryInfo;
	}

}
