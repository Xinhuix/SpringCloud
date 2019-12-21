package com.visionvera.service;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.datacore.OperationalDevice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 预约会议相关的业务接口
 *
 */
public interface MeetService {
	/**
	 * 获取正在进行中的会议列表
	 * @param paramsMap
	 * @param serverRegionIdLength 想看服务器行政区域ID的长度
	 * @return
	 */
	public ReturnData getMeetingInfo(Map<String, Object> paramsMap, Integer serverRegionIdLength);
	/**
	 *
	 * @Description: TODO
	 * @param @param 获取参会终端详细信息
	 * @param serverRegionIdLength 想看服务器行政区域ID的长度
	 * @param @return
	 * @return List<DeviceVO>
	 * @throws
	 * @author 谢程算
	 * @date 2018年6月13日
	 */
	public ReturnData getMeetingDevList(Map<String, Object> paramsMap, Integer serverRegionIdLength);


	/**
	 * 调用运维服务获取音视频告警阈值
	 * @return
	 */
	public Map<Integer, Integer> getAlarmThresholdList();

	/**
	 *
	 * @Description: TODO
	 * @param @param 获取参会终端区域信息
	 * @param @return
	 * @return List<DeviceVO>
	 * @throws
	 * @author 谢程算
	 * @date 2018年6月13日
	 */
	public ReturnData getMeetingDevRegionList(Map<String, Object> paramsMap);

	/**
	 * 重启/关闭终端
	 * @param operationalDevice
	 * @return
	 */
	public ReturnData operateDevice(OperationalDevice operationalDevice);

	/**
	 * 获取终端、终端所属服务器情况
	 * @param deviceIds 设备ID，多个使用英文逗号","分隔。最多支持20个
	 * @return
	 */
	public ReturnData getDevAndSvrInfo(List<String> deviceIdList);

	/**
	 * 业务上报的Excel的工作簿对象
	 * @param businessId 业务ID
	 * @param businessType 1表示会议列表，2表示可视电话列表，3表示实时直播列表，4表示实时录制列表，5表示实时点播列表，6表示实时升级列表，7表示实时监控列表
	 * @param serverRegionIdLength 想看服务器行政区域ID的长度
	 * @param request
	 * @param response
	 * @param devNo
     * @param order
     * @param sidx
     * @return
	 */
	public void getBusinessReportingWorkbook(String businessId, String businessType, Integer serverRegionIdLength, Boolean isException, HttpServletRequest request, HttpServletResponse response, String devNo, String order, String sidx);

    List<Map<String, Object>> getAllInfo(List<String> deviceIdList);

    /**
	 * 查询预约会议
	 * @param schedule
	 * @return
	 */
	ScheduleVO queryByScheduleUuid(ScheduleVO schedule);

	/**
	 * @param params
	 * 修改会议表
	 */
	public int updateMeetings(Map<String, Object> params);
	/**
	 * @param schedule
	 * 实体修改
	 */
	public int updateScheduleByUuid(ScheduleVO schedule);

}
