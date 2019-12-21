package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.TPlatformTypeVO;

/**
 * 平台类型service
 * @author dql714099655
 *
 */
public interface PlatformTypeService {
	
	/**
	 * 查询平台列表
	 * @return
	 */
	List<TPlatformTypeVO> getPlatformTypeList(Map<String,Object> params);

}
