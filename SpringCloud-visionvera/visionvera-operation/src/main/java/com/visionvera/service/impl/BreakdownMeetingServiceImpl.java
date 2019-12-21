package com.visionvera.service.impl;


import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.PageHelper;
import com.visionvera.basecrud.CrudService;
import com.visionvera.bean.operation.BreakdownMeeting;
import com.visionvera.dao.operation.BreakdownMeetingDao;
import com.visionvera.service.BreakdownMeetingService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
@Service("breakdownMeetingService")
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class BreakdownMeetingServiceImpl extends CrudService<BreakdownMeetingDao, BreakdownMeeting> implements BreakdownMeetingService {

	@Resource
	private BreakdownMeetingDao breakdownMeetingDao ;
	
	@Override
	public List<BreakdownMeeting> getBreakdownMeetingList(Map<String,Object> paramsMap) {
		PageHelper.startPage((Integer)paramsMap.get("pageNum"), (Integer)paramsMap.get("pageSize"));
		List<BreakdownMeeting> meetingList = breakdownMeetingDao.getBreakdownMeetingList(paramsMap);
		return meetingList;
	}

	@Override
	public Map<String, Object> getBreakdownMeetingInfo(Map<String, Object> paramsMap) {
		return breakdownMeetingDao.getBreakdownMeetingInfo(paramsMap) ;
	}

}
