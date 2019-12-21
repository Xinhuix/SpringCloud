package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.datacore.TPlatformTypeVO;
import com.visionvera.service.PlatformTypeService;

/**
 * 平台类型Controller
 * @author dql
 *
 */
@RestController
public class PlatformTypeController {
	
	private static final Logger logger = LogManager.getLogger(PlatformResController.class);
	
	@Autowired
	private PlatformTypeService platformTypeService;
	
	/**
	 * 获取平台类型列表
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/getPlatformTypeList")
	public Map<String,Object> getPlatformTypeList(HttpServletRequest request,HttpServletResponse response,
		@RequestParam(value="platformType",required=false) String platformType) {
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>();
		Map<String,Object> params = new HashMap<String, Object>();
		try {
			params.put("platformType",platformType==null?"": platformType.split(","));
			List<TPlatformTypeVO> platformTypeVOList = platformTypeService.getPlatformTypeList(params);
			if(platformTypeVOList != null && platformTypeVOList.size() > 0) {
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询平台列表成功");
				Map<String,Object> dataMap = new HashMap<String,Object>();
				dataMap.put("items", platformTypeVOList);
				resultMap.put("data", dataMap);
			} else {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "查询平台列表为空");
			}
		}catch(Exception e) {
			logger.error("查询平台列表异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询平台列表异常");
			return resultMap;
		}
		return resultMap;
	}
}
