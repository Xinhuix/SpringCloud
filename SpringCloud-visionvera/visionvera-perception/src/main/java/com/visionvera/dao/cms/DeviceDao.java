package com.visionvera.dao.cms;

import com.visionvera.bean.cms.DeviceVO;

import java.util.List;
import java.util.Map;

/**
 * 设备的数据库操作
 *
 */
public interface DeviceDao {
	/**
	 * 根据会议ID获取会议终端列表（详细信息）
	 * @param paramsMap {"scheduleId": ""}
	 * @return
	 */
	public List<Map<String, Object>> selectDeviceListByMeetingId(Map<String, Object> paramsMap);

	/**
	 * 通过8位号码批量查询设备信息
	 * @param zonedevnoArr 8位号码数组
	 * @return
	 */
	public List<DeviceVO> selectDeviceByZonedevnoBatch(String[] zonedevnoArr);

	/**
	 * 根据会议ID获取会议终端区域列表
	 * @param paramsMap {"scheduleId": ""}
	 * @return
	 */
	public List<Map<String, Object>> selectDeviceRegionListByMeetingId(Map<String, Object> paramsMap);
}
