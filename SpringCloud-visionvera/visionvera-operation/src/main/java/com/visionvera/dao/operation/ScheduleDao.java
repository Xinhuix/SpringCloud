package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DevicePropVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.MeetingDevVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleChartVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.ScheduleMsgVO;
import com.visionvera.bean.cms.ScheduleReportVO;
import com.visionvera.bean.cms.ScheduleSmsVO;
import com.visionvera.bean.cms.ScheduleStateVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.TaskFormVO;
import com.visionvera.bean.cms.TaskVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.restful.VphoneVO;


/**
 * 
 * @ClassName: ScheduleDao 
 * @Description: TODO 预约Dao接口
 * @author zhaolei
 * @date 2016年8月12日 下午3:15:44 
 *
 */
public interface ScheduleDao {

	/**
	 * @param rowBounds 
	 * 
	 * @Title: getScheduleList 
	 * @Description: TODO 获取预约列表
	 * @param @param map 查询条件集合
	 * @param @return  参数说明 
	 * @return List<ScheduleBean>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getScheduleList(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: getValidSchedules 
	 * @Description: TODO webservice获取会议列表 （有效预约）
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleVO>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getValidSchedules(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 
	 * @Title: getValidSchedules 
	 * @Description: TODO webservice获取我的会议列表（由userId发起的会议）
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleVO>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getMySchedules(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 
	 * @Title: schedulesInProcess 
	 * @Description: TODO 查询正在开会的会议列表
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleVO>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> schedulesInProcess(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 
	 * @Title: getMyScheduleHistory 
	 * @Description: TODO webservice获取我的会议预约记录（未过时的，含审批和未审批）
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleVO>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getMyScheduleHistory(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 
	 * @Title: getApproveSchedules 
	 * @Description: TODO webservice获取待审批预约列表
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleVO>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getApproveSchedules(Map<String, Object> map, RowBounds rowBounds);
	
	/***
	 * 
	 * @Description: 获取预约列表总条数
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 * @author wangrz
	 * @date 2016年8月12日
	 */
	int getScheduleListCount(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: addSchedule 
	 * @Description: TODO 新增预约
	 * @param @param schedule
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addSchedule(ScheduleVO schedule);

	/**
	 * 
	 * @Title: setPcmmetId 
	 * @Description: TODO 设置pcmeet_id
	 * @param @param schedule
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int setPcmmetId(ScheduleVO schedule);

	/**
	 * 
	 * @Title: getPcmmetId 
	 * @Description: TODO 获取pcmeet_id
	 * @param @param schedule
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getPcmmetId(ScheduleVO schedule);
	
	/**
	 * 
	 * @Title: updateSchedule 
	 * @Description: TODO 更新预约 
	 * @param @param schedule
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateSchedule(ScheduleVO schedule);
	
	/**
	 * 
	 * @Title: checkSchedule 
	 * @Description: TODO 校验预约冲突，用于新增预约之前。名称+开始时间+结束时间。
	 * @param @param map
	 * @param @return  参数说明 
	 * @return Integer    返回类型 
	 * @throws
	 */
	int checkSchedule(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: deleteSchedule 
	 * @Description: TODO 删除预约 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteSchedule(Map<String, Object> paramsMap);
	
	
	/**
	 * 
	 * @Title: auditSchedule 
	 * @Description: 审核预约
	 * @param map uuid 预约的uuid
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int auditSchedule(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: rejectSchedule 
	 * @Description: 驳回预约 
	 * @param map uuid
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int rejectSchedule(Map<String, Object> map);
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: scheduleCount 
	 * @Description: 预约状态统计
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int scheduleCount(Map<String, Object> paramsMap);
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: approveCount 
	 * @Description: 审批状态 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int approveCount(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: getScheduleStatus 
	 * @Description: 获取预约状态 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<ScheduleSmsVO> getScheduleStatus(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: getScheduleName 
	 * @Description: 获取预约名称通过uuid 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return String    返回类型 
	 * @throws
	 */
	String getScheduleName(Map<String, Object> map);
	
	
	/**
	 * 
	 * @Title: getAllAudits 
	 * @Description: 获取所有审核人员手机号码 
	 * @param @return  参数说明 
	 * @return List<String>    返回类型 
	 * @throws
	 */
	List<String> getAllAudits(Map<String, Object> map);
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: scheduleAllCount 
	 * @Description: 统计预约个数，结束时间大于当前时间
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int scheduleAllCount(Map<String, Object> paramsMap);

	/**
	 * @param rowBounds 
	 * 
	 * @Title: devices
	 * @Description: 获取预约关联设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getDevices(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: getUsers
	 * @Description: 获取预约关联用户
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<UserVO> getUsers(Map<String, Object> paramsMap);
	
	/**
	 * 获取短信服务可用状态
	 * @Title: getSMSStatus 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getSMSStatus();

	/**
	 * 
	 * @Title: isScheduleDeletable
	 * @Description: 判断预约是否可以被删除
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int isScheduleDeletable(Map<String, Object> map);

	/**
	 * @param map 
	 * 获取预约关联的设备分组（会场）列表
	 * @Title: getDevGroups
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getDevGroups(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * @param map 
	 * 获取预约关联的设备分组（会场）总数
	 * @Title: getDevGroupCount
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	int getDevGroupCount(Map<String, Object> map);

	/**
	 * @param map 
	 * 预约冲突检测（勾选会场）
	 * @Title: chkConflict
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<Integer> chkConflictByDevGroups(Map<String, Object> map);
	
	/**
	 * @param map 
	 * 预约冲突检测（勾选终端）
	 * @Title: chkConflict
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<Integer> chkConflictByDevices(Map<String, Object> map);

	/**
	 * @param map 
	 * 预约冲突检测（不包含会议等级校验；只检验主席点位）
	 * @Title: chkConflict
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	Integer chkConflictByMaster(Map<String, Object> map);

	/**
	 * @param map 
	 * 添加预约与设备关联
	 * @Title: addDevice2Schedule
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDevice2Schedule(Map<String, Object> paramsMap);

	/**
	 * 对会场进行签名操作
	 * @Title: getDevGroups
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int sign(Map<String, Object> paramsMap);

	/**
	 * 获取某一用户已签到的会场列表
	 * @Title: getSigns
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getSigns(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 获取某一会议已签到的会场列表（拼接名字）
	 * @Title: getAllSignsByGroup
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getAllSignsByGroup(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 获取某一会议已签到的会场列表（逐条显示）
	 * @Title: getSigns
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getAllSigns(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 获取某一用户已签到的会议列表(未过期的）
	 * @Title: getUserSigns
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getUserSigns(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 获取某一用户已签到的会议列表（已过期的）
	 * @Title: getUserSigns
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getUserSignsHistory(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 获取某一会议已签到的会场总数
	 * @Title: getSigns
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	int getAllSignsCount(Map<String, Object> map);
	
	/**
	 * 获取未签到的会场列表
	 * @Title: getUnsigns
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getUnsigns(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 获取未签到的会场总数
	 * @Title: getUnsignsCount
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	int getUnsignsCount(Map<String, Object> map);

	/**
	 * 上报终端状态
	 * @Title: report
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	int report(ScheduleReportVO report);
	
	/**
	 * 获取已上报会场列表
	 * @Title: getReportDevGroups
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getReportDevGroups(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 获取个人上报记录
	 * @Title: getReportsRecord
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<ScheduleReportVO> getReportsRecord(Map<String, Object> map, RowBounds rowBounds);
	/**
	 * 获取上报信息
	 * @Title: getReports
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<ScheduleReportVO> getReports(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 获取某一会场已签到用户列表
	 * @Title: getSignedUsers
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<UserVO> getSignedUsers(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 获取已上终端总数
	 * @Title: getReportsCount
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getReportsCount(Map<String, Object> map);

	/**
	 * 获取个人已上报次数
	 * @Title: getReportTimes
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getReportTimes(ScheduleReportVO schedule);

	/**
	 * 保存留言（一个会议可以有多条留言）
	 * @Title: message
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int message(ScheduleMsgVO message);

	
	/**
	 * 审批留言（一次可以审批多条留言）
	 * @Title: getReports
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int permitMessage(Map<String, Object> map);
	/**
	 * 获取留言
	 * @Title: getMessages
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<ScheduleMsgVO> getMessages(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 保存通知（一个会议只能有一条通知，可更新）
	 * @Title: message
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int notice(ScheduleVO schedule);

	/**
	 * 获取会议详情（含通知）
	 * @author xiechs
	 * @date 2016年11月23日  
	 * @version 1.0.0 
	 * @param map
	 * @return
	 */
	List<ScheduleVO> getSchedule(Map<String, Object> map);

	/**
	 * 删除预约和设备的关联关系
	 * @author xiechs
	 * @date 2016年11月23日  
	 * @version 1.0.0 
	 * @param int
	 * @return
	 */
	int delScheduleDev(Map<String, Object> paramsMap);

	/**
	 * 删除会议保障总结表
	 * @author xiechs
	 * @date 2017年07月27日  
	 * @version 1.0.0 
	 * @param int
	 * @return
	 */
	int deleteSumForm(Map<String, Object> paramsMap);

	/**
	 * 设置会议群聊信息
	 * @author xiechs
	 * @date 2016年11月23日  
	 * @version 1.0.0 
	 * @param map
	 * @return
	 */
	int setScheduleChart(Map<String, Object> map);

	/**
	 * 获取会议群聊信息
	 * @author xiechs
	 * @date 2016年11月23日  
	 * @version 1.0.0 
	 * @param map
	 * @return
	 */
	List<ScheduleChartVO> getScheduleChart(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: getDevPropByIds 
	 * @Description: 获取设备属性值通过设备id集合
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<MeetingDevVO>    返回类型 
	 * @throws
	 */
	List<MeetingDevVO> getDevPropByIds(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: updateMeetinfInfo 
	 * @Description: 更新会议信息 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateMeetinfInfo(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: deleteScheduleById 
	 * @Description: 删除预约 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteScheduleById(Map<String, Object> map);

	/**
	 * @param map 
	 * 从cmsweb数据库的vc_dev_prop表获取设备属性
	 * @Title: getDevProps
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<DevicePropVO> getDevProps(List<Integer> addNumList);
	
	/**
	 * 
	 * @Title: getGisMeetings 
	 * @Description: 获取gis开始的会议 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleVO>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getGisMeetings(Map<String, Object> map);
	
	/**
	 * 
	 * TODO WebService获取用户开会记录
	 * @author 谢程算
	 * @date 2017年9月26日  
	 * @version 1.0.0 
	 * @param map
	 * @param rowBounds 
	 * @return
	 */
	List<ScheduleVO> getMeetingList(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 
	 * @Title: addGisSchedule 
	 * @Description: Gis添加会议
	 * @param @param schedule
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addGisSchedule(ScheduleVO schedule);
	
	/**
	 * 
	 * @Title: checkUserMeeting 
	 * @Description: 验证Gis用户是否已经开会
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int checkUserMeeting(Map<String, Object> map);
	
	/**
	 * 获取正在进行的会议通过设备编号
	 * @param map
	 * @return
	 */
	List<ScheduleVO> getMeetingByDevice(Map<String, Object> map);
	
	/**
	 * 获取会议详细信息
	 * @param map
	 * @return
	 */
	List<ScheduleVO> getMeetingDetalInfo(Map<String, Object> map);
	
	/**
	 * 获取高于当前预约等级冲突的会议基本信息
	 * @param map
	 * @return
	 */
	List<ScheduleVO> getConflictMeetings(Map<String, Object> map);
	
	/**
	 * 获取低于当前预约等级并且冲突的设备主键
	 * @param map
	 * @return
	 */
	List<String> getLowLevelMeetingDevs(Map<String, Object> map);
	
	/**
	 * 删除低于当前预约等级并且冲突的会议设备
	 * @param map
	 * @return
	 */
	int delLowConflictMeetDevs(Map<String, Object> map);
	
	/**
	 * 获取会议预约的所有设备号码
	 * @param map
	 * @return
	 */
	List<Integer> getMeetingDevs(Map<String, Object> map);
	
	/**
	 * 删除会议变更申请记录
	 * @param map
	 * @return
	 */
	int delMeetModifyStatus(Map<String, Object> map);
	/**
	 * 获取已审预约列表
	 * @param paramsMap
	 * @return
	 * */
	List<TaskFormVO> getDoneList(Map<String, Object> paramsMap, RowBounds rowBounds);
	/**
	 * 获取已审预约列表总条数
	 * @param paramsMap
	 * @return
	 * */
	int getDoneListCount(Map<String, Object> paramsMap);
	/**
	 * 获取预约相关设备总条数
	 * @param paramsMap
	 * @return
	 * */
	int getDeviceCount(Map<String, Object> paramsMap);
	/**
	 * 获取预约列表相关设备
	 * @param paramsMap
	 * @return
	 * */
	List<DeviceVO> getDevicesPag(Map<String, Object> paramsMap,
			RowBounds rowBounds);

	/**
	 * 
	 * TODO WebService获取用户开会记录总数
	 * @author 谢程算
	 * @date 2017年9月26日  
	 * @version 1.0.0 
	 * @param map
	 * @return
	 */
	int getMeetingListCount(Map<String, Object> map);
	
	/**
	 * 
	 * TODO WebService修改会议状态
	 * @author 谢程算
	 * @date 2017年9月26日  
	 * @version 1.0.0 
	 * @param map
	 * @return
	 */
	int updateScheduleStatus(ScheduleVO schedule);
	
	/**
	 * @param rowBounds 
	 * 
	 * @Title: getAccessorDetail 
	 * @Description: TODO 获取预约列表
	 * @param @param map 查询条件集合
	 * @param @return  参数说明 
	 * @return List<ScheduleBean>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getAccessorDetail(Map<String, Object> map);
	
	/**
	 * @param rowBounds 
	 * 
	 * @Title: getAccessorDetailCount 
	 * @Description: TODO 获取预约列表
	 * @param @param map 查询条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getAccessorDetailCount(Map<String, Object> map);
	
	/**
	 * @param rowBounds 
	 * 
	 * @Title: getMeetingCount 
	 * @Description: TODO 根据会议类型获取指定状态的会议数量
	 * @param @param map 查询条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getMeetingCount(Map<String, Object> map);

	/**
	 *
	 * @Title: getMeetingStopList 
	 * @Description: TODO 获取开会中，已审批未开会数据列表
	 * @param @param map 查询条件集合
	 * @param @return  参数说明 
	 * @return List    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getMeetingStopList(Map<String, Object> map,
			RowBounds rowBounds);
	/**
	 *
	 * @Title: getMeetingStopListCount 
	 * @Description: TODO 获取开会中，已审批未开会数据列表
	 * @param @param map 查询条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getMeetingStopListCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取系统消息
	 * @author 谢程算
	 * @date 2017年11月9日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @param rowBounds
	 * @return
	 */
	List<TaskVO> getSysInfo(Map<String, Object> paramsMap,
			RowBounds rowBounds);

	/**
	 * 
	 * TODO 获取会议的调度服务器url、账号、密码
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<ScheduleStateVO> getScheduleRoute(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 记录会议的调度服务器url、账号、密码
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int updateScheduleRoute(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * TODO 删除会议的调度服务器url、账号、密码
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int delScheduleRoute(Map<String, Object> paramsMap);
	

	/***
	 * 会议异常告警，终端未入网数据
	 */
	List<ScheduleVO> getMeetingAlarm(Map<String, Object> paramsMaps);

	/**
	 * 
	 * TODO 获取数据所属平台列表
	 * @author 谢程算
	 * @date 2017年12月12日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<RegionVO> getScheduleServers();

	/**
	 * 
	 * TODO 获取数据所属平台列表
	 * @author 谢程算
	 * @date 2017年12月12日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<RegionVO> getDetailServers();
	/**
	 * 
	 * TODO 获取数据所属区域
	 * @author 周逸芳
	 * @date 2017年12月25日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	RegionVO selectRegionId(ScheduleVO scheduleVO);
	
	/***
	 * 统计有效预约
	 */
	int getValidScheduleCount();

	/**
	 * @param rowBounds 
	 * 
	 * @Title: getScheduleHistory 
	 * @Description: TODO 大数据首页获取开会记录
	 * @param @param map 查询条件集合
	 * @param @return  参数说明 
	 * @return List<ScheduleBean>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getScheduleHistory(Map<String, Object> map, RowBounds rowBounds);

	/***
	 * 
	 * @Description: 大数据首页获取开会记录总条数
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 * @author 谢程算
	 * @date 2018年1月8日
	 */
	int getScheduleHistoryCount(Map<String, Object> map);
	
	/***
	 * （运维接口）
	 * 统计会议中的会议总数
	 * @return
	 */
	int getOperatingMeet();
	
	/****
	 * 根据设备号码获取设备所在地区信息
	 * @param map
	 * @return
	 */
	List<RegionVO> getRegionInfo(Map<String, Object> map);
	
	/***
	 * 根据预约id查询预约详情
	 */
	List<ScheduleFormVO> getSchedulesInUUidList(Map<String, Object> map);

	/** <pre>getMeetingInfo(获取会议列表，给大网提供数据)   
	 * 创建人：谢程算      
	 * 创建时间：2018年1月25日 下午1:43:46    
	 * 修改人：谢程算       
	 * 修改时间：2018年1月25日 下午1:43:46    
	 * 修改备注： 
	 * @param paramsMap
	 * @param rowBounds 
	 * @return</pre>    
	 */
	List<Map<String, Object>> getMeetingInfo(Map<String, Object> paramsMap, RowBounds rowBounds);
	
	/** <pre>getMeetingInfoCount(获取会议列表总数，给大网提供数据)   
	 * 创建人：谢程算      
	 * 创建时间：2018年1月25日 下午1:43:46    
	 * 修改人：谢程算       
	 * 修改时间：2018年1月25日 下午1:43:46    
	 * 修改备注： 
	 * @param paramsMap
	 * @param rowBounds 
	 * @return</pre>    
	 */
	int getMeetingInfoCount(Map<String, Object> paramsMap);
	/** <pre>getScheduleState(获取会议主席发一发二)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年5月16日 上午11:13:11    
	 * 修改人：周逸芳        
	 * 修改时间：2018年5月16日 上午11:13:11    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<ScheduleStateVO> getScheduleState(Map<String, Object> paramsMap);
	/** <pre>getScheduleMaster(获取所有正在开会的主席)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年5月16日 下午5:59:48    
	 * 修改人：周逸芳        
	 * 修改时间：2018年5月16日 下午5:59:48    
	 * 修改备注： 
	 * @return</pre>    
	 */
	List<ScheduleVO> getScheduleMaster(Map<String, Object> paramsMap);

	/** <pre>addScheduleDev(这里用一句话描述这个方法的作用)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月4日 下午3:36:40    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月4日 下午3:36:40    
	 * 修改备注： 
	 * @param schedule
	 * @return</pre>    
	 */
	//int addScheduleDev(Map<String, Object> schedule);

	/** <pre>getDevStatus(查看设备是否在会议中)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月4日 下午6:08:16    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月4日 下午6:08:16    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	//List<ScheduleVO> getDevStatus(Map<String, Object> paramsMap);
	/****
	 * 根据日期查询预约会议列表
	 * @param map
	 * @return
	 */
	List<ScheduleFormVO> getScheduleListByDate(Map<String, Object> paramsMap);
	/**
	 * 获取会议中可视电话的数据
	 */
	List<VphoneVO> getPhoneMeet(Map<String,Object> map);
	/**
	 * 
	 * @Description: TODO
	 * @param @param 视联汇、视联管家获取正在进行以及未来指定时间段将要开的会议列表
	 * @param @return   
	 * @throws
	 */
	public List<Map<String, Object>> getAllMeetingList(Map<String, Object> paramsMap,RowBounds rowBounds);
	/**
	 *  视联汇、视联管家获取正在进行以及未来指定时间段将要开的会议总数
	 * @param paramsMap
	 * @return
	 */
	int getAllMeetingCount(Map<String, Object> paramsMap);
	
	/**
	 * 通过businessType查询预约列表
	 * @param paramsMap 可能包含regionId、createType、name(预约名称)。必有businessType
	 * 		1表示实时会议（状态为4，并且不超时、不掉线）
	 * 		2表示预约会议（状态为2，并且在有效时间之内）
	 * 		3表示历史会议（状态为5）
	 * 		4表示故障会议（状态为4，并且超时或者掉线的）
	 * @return
	 */
	public List<Map<String, Object>> selectScheduleListByBusinessType(Map<String, Object> paramsMap,RowBounds rowBounds);
	/**
	 * 通过businessType查询预约总数
	 * @param paramsMap 可能包含regionId、createType、name(预约名称)。必有businessType
	 * @param paramsMap
	 * @return
	 */
	int selectScheduleListByBusinessTypeCount(Map<String, Object> paramsMap);
}
