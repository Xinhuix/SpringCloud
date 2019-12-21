package com.visionvera.service;

import java.util.List;
import java.util.Map;



import com.visionvera.bean.cms.DevDetail;
import com.visionvera.bean.cms.Device;
import com.visionvera.bean.cms.ScheduleStateVO;
import com.visionvera.bean.cms.ServerInfo;
import com.visionvera.bean.cms.SummaryForm;
import com.visionvera.bean.cms.User;
import com.visionvera.bean.cms.Schedule;
import com.visionvera.bean.cms.ScheduleDev;
import com.visionvera.bean.cms.Meeting;
import com.visionvera.bean.cms.ServerSyncVO;


public interface SyncDataService {
	
	/**
	 * 
	 * TODO webservice分级系统-获取子系统设备所属服务器列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<ServerInfo> getServerInfos(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改设备所属服务器列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addServerInfos(List<Map<String, Object>> list);

	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的设备所属服务器列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delServerInfos(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-获取子系统设备列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<Device> getDevices(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改设备列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addDevices(List<Map<String, Object>> list);

	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的设备列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delDevices(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-获取子系统预约列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<Schedule> getSchedules(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的预约列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addSchedules(List<Map<String, Object>> list);

	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的预约列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */

	int delSchedules(List<String> list);
	
	/**
	 * 
	 * TODO webservice分级系统-获取子系统预约设备列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<ScheduleDev> getScheduleDevs(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的预约设备列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addScheduleDevs(List<Map<String, Object>> list);

	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的预约设备列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delScheduleDevs(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-获取子系统总结表列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<SummaryForm> getSummarys(List<String> list);
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的总结表列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addSummarys(List<Map<String, Object>> list);
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的总结表列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delSummarys(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-获取子系统会议列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<Meeting> getMeetings(List<String> list);
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/删除的会议列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addMeetings(List<Map<String, Object>> list);
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的会议列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delMeetings(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-获取子系统用户列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<User> getUsers(List<String> list);
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的用户列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addUsers(List<Map<String, Object>> list);
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的用户列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delUsers(List<String> list);

	/**
	 * 
	 * TODO webservice从会管子系统同步数据
	 * @author 谢程算
	 * @date 2017年10月9日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	Map<String, Object> syncData(ServerSyncVO sv);

	/**
	 * 
	 * TODO webservice分级系统-获取子系统预约主席，发一,发二列表
	 * @author 周逸芳
	 * @date 2017年12月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<ScheduleStateVO> getScheduleState(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-从本系统中删除子系统已删除预约主席，发一,发二uuid信息
	 * @author 周逸芳
	 * @date 2017年12月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delScheduleState(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-同步子系统预约主席，发一,发二uuid信息入库
	 * @author 周逸芳
	 * @date 2017年12月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int  addScheduleState(List<Map<String, Object>> list);

	/**
	 * 
	 * TODO webservice分级系统-启用/禁用数据库外键约束
	 * @author 谢程算
	 * @date 2017年10月16日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int setFKChk(Map<String, Object> map);

	/**
	 * TODO webservice根据uuid或ip获取子系统的行政区域id
	 * @author 谢程算
	 * @param map
	 * @return
	 */
	String getSvrRegionId(Map<String, Object> map);

	
	/**
	 * TODO webservice获取vc_dev_detail数据
	 * @author 谢程算
	 * @param map
	 * @return
	 */
	List<DevDetail> getDevDetail(List<String> list);
}
