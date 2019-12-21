package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;


/**
 * 
 * @ClassName: MeetDao 
 * @Description: TODO 会议Dao接口
 * @author xiechengsuan
 * @date 2016年11月1日
 *
 */
public interface MeetDao {

	/**
	 * 
	 * @Title: getMeetList 
	 * @Description: TODO 获取会议列表 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleBean>    返回类型 
	 * @throws
	 */
	List<ScheduleVO> getMeetList(Map<String, Object> map, RowBounds rowBounds);
	
	/***
	 * 
	 * @Description: 获取会议列表总条数
	 * @param @param map
	 * @param @return   
	 * @return int  
	 * @throws
	 */
	int getMeetListCount(Map<String, Object> map);

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
	
	/***
	 * 获取地区ID下拉
	 */
	List<RegionVO> getConferenceSelectionArea();
}
