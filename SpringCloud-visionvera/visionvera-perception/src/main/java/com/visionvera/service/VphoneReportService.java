package com.visionvera.service;

import com.visionvera.basecrud.BaseService;
import com.visionvera.bean.datacore.VphoneReport;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务接口
 * </p>
 *
 * @author zhanghui
 * @since 2018-11-21
 */
public interface VphoneReportService extends BaseService<VphoneReport> {

	/**
	 * 查询可视电话列表
	 * @return
	 * @param order
	 * @param sidx
	 */
	List<Map<String, Object>> queryListBySearchText(String order, String sidx);

}
