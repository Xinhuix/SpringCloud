package com.visionvera.web.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.PlatformTypeVO;
import com.visionvera.service.PlatformTypeService;

/**
 * 平台类型Controller
 * @author dql714099655
 *
 */
@RestController
public class PlatformTypeController extends BaseReturn {
	
	private Logger logger = LoggerFactory.getLogger(PlatformTypeController.class);
	
	@Autowired
	private PlatformTypeService platformTypeService;
	
	@RequestMapping(value="/platformtype/list",method=RequestMethod.GET)
	public ReturnData getAllPlatformType() {
		try {
			List<PlatformTypeVO> platformTypeList = platformTypeService.getAllPlatformType();
			return super.returnResult(0, "查询平台类型成功", null, platformTypeList);
		} catch(Exception e) {
			logger.error("查询平台类型失败", e);
			return super.returnError("查询平台类型失败");
		}
	}

}
