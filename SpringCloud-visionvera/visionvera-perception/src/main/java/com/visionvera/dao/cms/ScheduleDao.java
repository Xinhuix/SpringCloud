package com.visionvera.dao.cms;

import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 预约会议相关的数据库操作
 *
 */
public interface ScheduleDao {
	/**
	 * 获取会议所有终端
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> selectScheduleDev(String uuid);
	/**
	 * 获取正在进行中的会议列表，携带会议对应的设备ID, 给大网提供数据
	 * @param paramsMap
	 * @return
	 */
	public List<Map<String, Object>> selectMeetingInfo(Map<String, Object> paramsMap);

	/**
	 *
	 * @Description: TODO
	 * @param @param 视联汇、视联管家获取正在进行以及未来指定时间段将要开的会议列表
	 * @param @return
	 * @throws
	 * @author 谢程算
	 * @date 2018年10月30日
	 */
	public List<Map<String, Object>> getAllMeetingList(Map<String, Object> paramsMap);

	/**
	 * 通过businessType查询预约列表
	 * @param paramsMap 可能包含regionId、createType、name(预约名称)。必有businessType
	 * 		1表示实时会议（状态为4，并且不超时、不掉线）
	 * 		2表示预约会议（状态为2，并且在有效时间之内）
	 * 		3表示历史会议（状态为5）
	 * 		4表示故障会议（状态为4，并且超时或者掉线的）
	 * @return
	 */
	public List<Map<String, Object>> selectScheduleListByBusinessType(Map<String, Object> paramsMap);
	/**
	 * 添加预约会议
	 * @param schedule
	 * @return
	 */
	public int insertScheduleSelective(ScheduleVO schedule);

	/**
	 * 查询设备ID
	 * @param params
	 * @return
	 */
	public Map<String, Object> qureyDevIdByDevNo(Map<String, Object> params);

	/**
	 * @param list
	 * 添加预约与设备关联
	 * @Title: addDevs2Schedule
	 * @throws
	 */
	int addDevs2Schedule(List<Map<String, Object>> list);
	/**
	 * @param params
	 * 添加会议表
	 * @Title: addDevs2Schedule
	 * @throws
	 */
	int addMeetings(Map<String, Object> params);
	/**
	 * @param params
	 * 修改会议表
	 * @Title: addDevs2Schedule
	 * @throws
	 */
	int updateMeetings(Map<String, Object> params);
	/**
	 * @param schedule
	 * 通用实体修改
	 * @Title: addDevs2Schedule
	 * @throws
	 */
	int updateByPrimaryKeySelective(ScheduleVO schedule);
	/**
	 * @param schedule
	 * 通用实体查询
	 * @Title: qureyByEntity
	 * @throws
	 */
	ScheduleVO qureyByEntity(ScheduleVO schedule);
	/**
	 * @param schedule
	 * 连接设备表实体查询
	 * @Title: qureyByEntity
	 * @throws
	 */
	ScheduleVO qureyByScheduleUuid(ScheduleVO schedule);
	/**
	 * @param schedule
	 * 查询用户
	 * @Title: qureyByEntity
	 * @throws
	 */
	UserVO getUserByLoginName(String loginName);
	/**
	 * @param scheduleId
	 *  删除预约与设备关系
	 * @Title: qureyByEntity
	 * @throws
	 */
	int delScheduleDevRel(String scheduleId);

	/**
	 * 根据会议id查询会议信息
	 * @param uuid
	 * @return
	 */
	ScheduleVO queryByUuid(String uuid);
}
