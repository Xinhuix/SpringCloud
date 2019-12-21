package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;
import com.visionvera.basecrud.CrudDao;
import com.visionvera.bean.operation.BreakdownMeeting;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
public interface BreakdownMeetingDao extends CrudDao<BreakdownMeeting> {
	List<BreakdownMeeting> getBreakdownMeetingList(Map<String,Object> paramMap) ;
	public Map<String, Object> getBreakdownMeetingInfo(Map<String, Object> paramsMap) ;
}
