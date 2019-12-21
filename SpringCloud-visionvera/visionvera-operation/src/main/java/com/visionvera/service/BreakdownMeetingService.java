package com.visionvera.service;

import java.util.List;
import java.util.Map;
import com.visionvera.basecrud.BaseService;
import com.visionvera.bean.operation.BreakdownMeeting;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
public interface BreakdownMeetingService extends BaseService<BreakdownMeeting> {
	
	/**
	 * 获取故障会议列表
	 * @date 2018年7月6日 下午5:31:58
	 * @author wangqiubao
	 * @param paramMap
	 * @return
	 */
	List<BreakdownMeeting> getBreakdownMeetingList(Map<String,Object> paramMap);
	/**
	 * 查询会议详情
	 * @date 2018年7月12日 下午2:49:30
	 * @author wangqiubao
	 * @param paramsMap
	 * @return
	 */
	Map<String,Object> getBreakdownMeetingInfo(Map<String,Object> paramsMap ) ;

}
