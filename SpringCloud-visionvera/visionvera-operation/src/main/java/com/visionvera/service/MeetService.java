package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.cms.LogVO;
import com.visionvera.bean.cms.MeetRecordVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.restful.DataInfo;
import com.visionvera.bean.restful.ResponseInfo;


public interface MeetService {

	/**
	 * 
	 * @Title: getMeetList 
	 * @Description: TODO 获取会议列表 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleBean>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getMeetList(Map<String, Object> map);
	
	/***
	 * 
	 * @Description: 获取会议列表总条数
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 */
	int getMeetListCount(Map<String, Object> map);

	/**
	 * 
	 * @Title: getLogList
	 * @Description: 根据会议ID查询会议操作日志（不分页）
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	List<LogVO> getLogList(Map<String, Object> map);

	/**
	 * 
	 * @Title: getRecordList
	 * @Description: 根据会议ID查询会议纪要列表
	 * @param @param map
	 * @param @return 参数说明
	 * @return  list 返回类型
	 * @throws
	 */
	List<MeetRecordVO> getRecordList(Map<String, Object> paramsMap);
	
	/***
	 * 
	 * @Description: 添加会议记录
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 */
	int addMeeting(ScheduleVO schedule);

	/***
	 * 
	 * @Description: 更新会议记录
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 */
	int updateMeeting(ScheduleVO schedule);

	/**
	 * 
	 * @Title: stopMeeting
	 * @Description: TODO 根据会议ID停止会议
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	Map<String, Object> stopMeeting(Map<String, Object> paramsMap);
	
	/***
	 * 获取地区ID下拉
	 */
	List<RegionVO> getConferenceSelectionArea();

	/** <pre>devAttMeeting(终端入会)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月4日 下午2:40:55    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月4日 下午2:40:55    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	//Map<String, Object> devAttMeeting(Map<String, Object> paramsMap);
	/** <pre>devDelMeeting(终端退会)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月4日 下午2:40:55    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月4日 下午2:40:55    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	//Map<String, Object> devDelMeeting(Map<String, Object> paramsMap);
	/** 2019/2/20 周逸芳合并16位与64位代码时增加  原16位没有
	 * 视联汇、视联管家获取正在进行以及未来指定时间段将要开的会议列表
	 * @param paramsMap
	 * @return
	 */
	public ResponseInfo<DataInfo<Map<String, Object>>> getAllMeetingList(Map<String, Object> paramsMap);
}
