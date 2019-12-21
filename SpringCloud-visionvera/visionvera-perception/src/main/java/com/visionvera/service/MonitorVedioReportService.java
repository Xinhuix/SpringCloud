package com.visionvera.service;

import com.visionvera.basecrud.BaseService;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.MonitorVedioReport;

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
public interface MonitorVedioReportService extends BaseService<MonitorVedioReport> {

	/**
	 * 根据业务ID获取收看直播、监控、升级、点播终端信息
	 * @param businessId 发布直播、监控、升级、点播的业务ID
	 * @return
	 */
	public List<Map<String, Object>> getDeviceListByBusinessId(String businessId);


}
