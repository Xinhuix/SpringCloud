package com.visionvera.web.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.TIndustrybVO;
import com.visionvera.service.IndustryService;

/**
 * 行业归属
 *
 */
@RestController
@RequestMapping("/rest/industry")
public class IndustryController extends BaseReturn {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private IndustryService industryService;
	
	@RequestMapping(value = "/getAllIndustry", method = RequestMethod.GET)
	public ReturnData getAllIndustry() {
		try {
			PageInfo<TIndustrybVO> industryInfo = this.industryService.getIndustryList(false, null, null, null);
			return super.returnResult(0, "获取成功", null, industryInfo.getList());
		} catch (Exception e) {
			this.LOGGER.error("获取行业归属列表失败 ===== IndustryController ===== getAllIndustry =====> ", e);
			return super.returnError("获取失败");
		}
	}
}
