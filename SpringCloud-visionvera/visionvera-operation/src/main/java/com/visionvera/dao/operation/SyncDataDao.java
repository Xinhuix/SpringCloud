package com.visionvera.dao.operation;

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

/**
 * 
 * @ClassName: SyncDataDao 
 * @Description: TODO 分级系统-数据同步Dao接口
 * @author zhaolei
 * @date 2016年8月12日 下午3:15:44 
 *
 */
public interface SyncDataDao {
	
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
	 * 
	 * TODO webservice分级系统-获取子系统的行政区域ID
	 * @author 谢程算
	 * @date 2017年10月13日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	String getSvrRegionId(Map<String, Object> map);

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
	 * TODO webservice分级系统-删除本系统有但子系统中没有的服务器信息
	 * @author 谢程算
	 * @date 2017年12月7日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delServerInfos(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-获取子系统设备所属服务器ID列表（用于对比）
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<String> getServerIds(ServerInfo s);
	
	/**
	 * 
	 * TODO webservice分级系统-保存子系统设备所属服务器列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addServerInfos(List<Map<String, Object>> list);

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
	 * TODO webservice分级系统-删除本系统有但子系统中没有的设备信息
	 * @author 谢程算
	 * @date 2017年12月7日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delDevices(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-获取子系统设备ID列表（用于对比）
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<String> getDevIds(Device s);
	
	/**
	 * 
	 * TODO webservice分级系统-保存子系统设备列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addDevices(List<Map<String, Object>> list);

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
	 * TODO webservice分级系统-获取子系统预约uuid列表（用于对比）
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<String> getScheduleIds(Schedule s);

	/**
	 * 
	 * TODO webservice分级系统-删除本系统有但子系统中没有的预约信息
	 * @author 谢程算
	 * @date 2017年12月7日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delSchedules(List<String> list);

	/**
	 * 
	 * TODO webservice分级系统-保存子系统预约列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addSchedules(List<Map<String, Object>> list);

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
	 * TODO webservice分级系统-获取子系统预约设备ID列表（用于对比）
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<String> getScheduleDevIds(ScheduleDev s);
	
	/**
	 * 
	 * TODO webservice分级系统-删除本系统有但子系统中没有的预约_设备关联信息
	 * @author 谢程算
	 * @date 2017年12月7日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delScheduleDevs(List<String> list);
	
	/**
	 * 
	 * TODO webservice分级系统-保存子系统预约设备列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addScheduleDevs(List<Map<String, Object>> list);

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
	 * TODO webservice分级系统-获取子系统总结表ID列表（用于对比）
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<String> getSummaryIds(SummaryForm s);
	
	/**
	 * 
	 * TODO webservice分级系统-删除本系统有但子系统中没有的总结表信息
	 * @author 谢程算
	 * @date 2017年12月7日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delSummarys(List<String> list);
	
	/**
	 * 
	 * TODO webservice分级系统-保存子系统总结表列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addSummarys(List<Map<String, Object>> list);
	
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
	 * TODO webservice分级系统-获取子系统会议ID列表（用于对比）
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<String> getMeetingIds(Meeting s);
	
	/**
	 * 
	 * TODO webservice分级系统-删除本系统有但子系统中没有的会议信息
	 * @author 谢程算
	 * @date 2017年12月7日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delMeetings(List<String> list);
	
	/**
	 * 
	 * TODO webservice分级系统-保存子系统会议列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addMeetings(List<Map<String, Object>> list);
	
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
	 * TODO webservice分级系统-获取子系统用户id列表（用于对比）
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<String> getUserIds(User s);
	
	/**
	 * 
	 * TODO webservice分级系统-删除本系统有但子系统中没有的用户信息
	 * @author 谢程算
	 * @date 2017年12月7日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delUsers(List<String> list);
	
	/**
	 * 
	 * TODO webservice分级系统-保存子系统用户列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int addUsers(List<Map<String, Object>> list);

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
	 * TODO webservice分级系统-获取子系统预约主席，发一,发二uuid列表
	 * @author 周逸芳
	 * @date 2017年12月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	List<String> getScheduleStateIds(Schedule s);

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
	 * TODO webservice获取vc_dev_detail数据
	 * @author周逸芳
	 * @param map
	 * @return
	 */
	List<DevDetail> getDevDetail(List<String> list);
	/**
	 * TODO 根据子级行政区域id获取本平台所同步的子级设备详情id
	 * @author周逸芳
	 * @param map
	 * @return
	 */
	List<String> getDevDetailIds(DevDetail s);
	
	/**
	 * TODO 根据设备详情id删除设备详情信息
	 * @author周逸芳
	 * @param map
	 * @return
	 */
	void delDevDetail(List<String> myList);
	/**
	 * TODO 根据设备详情id删除设备详情信息
	 * @author周逸芳
	 * @param map
	 * @return
	 */
	int addDevDetail(List<Map<String, Object>> list);
	/**
	 * 
	 * TODO webservice分级系统-删除本系统有但子系统中没有的预约_设备关联信息
	 * @author 谢程算
	 * @date 2017年12月7日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int delScheduleDevsReport(List<String> list);

	List<User> getUserMeeting();
	
}
