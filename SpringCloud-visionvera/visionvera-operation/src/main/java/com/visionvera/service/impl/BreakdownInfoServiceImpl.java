package com.visionvera.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.visionvera.basecrud.CrudService;
import com.visionvera.bean.operation.BreakdownHistory;
import com.visionvera.bean.operation.BreakdownInfo;
import com.visionvera.bean.operation.BreakdownMeeting;
import com.visionvera.dao.operation.BreakdownHistoryDao;
import com.visionvera.dao.operation.BreakdownInfoDao;
import com.visionvera.dao.operation.BreakdownMeetingDao;
import com.visionvera.service.BreakdownInfoService;
import com.visionvera.util.DateUtil;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
@Service("breakdownInfoService")
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class BreakdownInfoServiceImpl extends CrudService<BreakdownInfoDao, BreakdownInfo> implements BreakdownInfoService {
	private static final Logger logger = LogManager.getLogger(BreakdownInfoServiceImpl.class) ;
	@Resource
	private BreakdownInfoDao breakdownInfoDao ;
	@Resource
	private BreakdownMeetingDao breakdownMeetingDao ;
	@Resource
	private BreakdownHistoryDao breakdownHistoryDao ;
	@Override
	public void addBreakdownInfo(BreakdownInfo breakdownInfo) {
		try {
			/******************向故障信息表保存数据******************/
			//获取回访状态
			int visitStatus = this.getVisitStatus(breakdownInfo) ;
			breakdownInfo.setVisitStatus(visitStatus);
			breakdownInfoDao.insertSelective(breakdownInfo) ;
			
			/*******************触发breakdownmeeting和breakdownhistory表********************/
			manageBreakdownInfo(breakdownInfo,"insert") ;
			
		} catch (Exception e) {
			logger.error("新增故障信息出错",e);
			throw new RuntimeException("新增故障信息出错！");//为了使事务回滚
		}
	}
	@Override
	public void updateBreakdownInfo(BreakdownInfo breakdownInfo) {
		try {
			//获取回访状态
			int visitStatus = this.getVisitStatus(breakdownInfo) ;
			breakdownInfo.setVisitStatus(visitStatus);
			//修改表
			breakdownInfoDao.updateByPrimaryKey(breakdownInfo) ;
			//查询修改后的值
			breakdownInfo = breakdownInfoDao.selectByPrimaryKey(breakdownInfo.getId()) ;
			this.manageBreakdownInfo(breakdownInfo,"update");
		} catch (Exception e) {
			logger.error("修改故障信息出错",e);
			throw new RuntimeException("修改故障信息出错！");//为了使事务回滚
		}
	}
	@Override
	public void deleteBreakdownInfo(BreakdownInfo breakdownInfo) {
		try {
			//删除breakdowninfo表
			breakdownInfoDao.delete(breakdownInfo) ;
			//删除breakdownhistory表
			BreakdownHistory history = new BreakdownHistory() ;
			history.setBreakdownId(breakdownInfo.getId());
			breakdownHistoryDao.delete(history) ;
			//修改或者删除breakdownmeeting表
			manageBreakdownInfo(breakdownInfo,"delete") ;
		} catch (Exception e) {
			logger.error("删除故障信息时出错",e);
			throw new RuntimeException("删除故障信息时出错！");//为了使事务回滚
		}
	}
	/**
	 * 新增、修改或者删除故障信息时，触发breakdownmeeting表和breakdownhistory表数据的变化
	 * @date 2018年7月9日 下午4:20:23
	 * @author wangqiubao
	 * @param breakdownInfo
	 * @param operate:insert,update,delete
	 */
	private void manageBreakdownInfo(BreakdownInfo breakdownInfo,String operate){
		String meetingId = breakdownInfo.getMeetingId() ;
		/******************向故障历史表保存数据******************/
		//删除breakdownInfo表时，不向breakdownHistory表插入数据
		if (StringUtils.isNotBlank(operate) && !"delete".equals(operate)) {
			BreakdownHistory history = new BreakdownHistory() ;
			BeanUtils.copyProperties(breakdownInfo,history) ;
			history.setBreakdownId(breakdownInfo.getId());
			breakdownHistoryDao.insertSelective(history) ;
		}
		
		/******************修改故障会议表数据(第一次新增)******************/
		BreakdownMeeting meeting = new BreakdownMeeting() ;
		meeting.setMeetingId(meetingId);
		meeting = breakdownMeetingDao.get(meeting) ;
		//查询该会议原有的故障信息(同一个事务未提交时，也会读取到该条数据)
		BreakdownInfo infoQuery = new BreakdownInfo() ;
		infoQuery.setMeetingId(meetingId);
		
		//获取故障信息的统计数据
		Map<String, Object> countMap = breakdownInfoDao.getBreakdownInfoCount(infoQuery) ;
		int breakdownCount = (null!=countMap.get("breakdownCount") ? Integer.valueOf(countMap.get("breakdownCount").toString()) : 0) ;//故障数量
		int solveCount = (null!=countMap.get("solveCount") ? Integer.valueOf(countMap.get("solveCount").toString()) : 0) ;//已经解决的数量
		int needVisitTotal = (null!=countMap.get("needVisitTotal") ?Integer.valueOf(countMap.get("needVisitTotal").toString()) : 0) ;//所需回访总数
		int visitCount = (null!=countMap.get("visitCount") ? Integer.valueOf(countMap.get("visitCount").toString()) : 0) ;//已回访数量
		int isExpect = (null!=countMap.get("isExpect") ? Integer.valueOf(countMap.get("isExpect").toString()) : 0) ;//是否超时(>0是；0否)
		int solveStatus = 0 ;//解决状态(0-未解决；1-已经解决)
		int visitStatus = 0 ;//回访状态（0-无；1-已回访；2-部分回访；3-已超时；4-未回访）
		//不同状态的优先级别（已超时 -  部分回访  -  已回访  - 未回访  - 无）
		if(needVisitTotal <= 0){
			visitStatus = 0 ;//无(需要回访的个数小于等于0)
		}else if(isExpect > 0){
			visitStatus = 3 ;//已超时(需要回访的个数大于0，并且是超时状态)
		}else if(needVisitTotal > visitCount){
			visitStatus = 2 ;//部分回访
		}else if(needVisitTotal == visitCount){
			visitStatus = 1 ;//已回访
		}else if(visitCount == 0){
			visitStatus = 4 ;//未回访
		}else{
			visitStatus = 0 ;//无
		}
		//如果已经解决的数量==该会议故障总数，则解决状态是已经解决
		if(solveCount == breakdownCount){
			solveStatus = 1 ;
		}
		BreakdownMeeting breakdownMeeting = new BreakdownMeeting() ;
		breakdownMeeting.setMeetingId(meetingId) ; 
		breakdownMeeting.setBreakDownCount(breakdownCount);
		breakdownMeeting.setMeetingId(meetingId);
		breakdownMeeting.setSolveCount(solveCount);
		breakdownMeeting.setNeedVisitTotal(needVisitTotal);
		breakdownMeeting.setVisitCount(visitCount);
		breakdownMeeting.setSolveStatus(solveStatus);
		breakdownMeeting.setVisitStatus(visitStatus);
		if (null == meeting) {
			//insert(第一次新增)
			breakdownMeetingDao.insertSelective(breakdownMeeting) ;
		}else{
			//update
			breakdownMeetingDao.updateByPrimaryKeySelective(breakdownMeeting) ;
		}
	}
	@Override
	public List<BreakdownInfo> getBreakdownInfoList(BreakdownInfo breakdownInfo, Map<String, Object> paramMap) {
		if(paramMap.get("pageSize") == null || Integer.parseInt(paramMap.get("pageSize").toString()) == -1){
			List<BreakdownInfo> breakdownInfoList = breakdownInfoDao.queryList(breakdownInfo) ;
			return breakdownInfoList;
		}
		PageHelper.startPage((Integer)paramMap.get("pageNum"), (Integer)paramMap.get("pageSize")) ;
		List<BreakdownInfo> breakdownInfoList = breakdownInfoDao.queryList(breakdownInfo) ;
		return breakdownInfoList;
	}
	/**
	 * 判断故障信息的回访状态（单条记录）
	 * @date 2018年7月11日 下午5:28:17
	 * @author wangqiubao
	 * @param info
	 * @return
	 */
	private int getVisitStatus(BreakdownInfo info){
		int visitStatus = 0 ;
		if(null == info.getIsNeedVisit() || info.getIsNeedVisit() == 0){
			//无(是否需要回访状态等于null或者是否需要回访状态等于否)
			visitStatus = 0 ;
		}else if(info.getIsNeedVisit() == 1 
				&& (DateUtil.string2Long(info.getActualVisitTime()) > DateUtil.string2Long(info.getExpectVisitTime()))){
			//已超时(是否需要回访状态等于是，并且实际回访时间大于预期回访时间)
			visitStatus = 3 ;
		}else if(info.getIsNeedVisit() == 1 && (null == info.getIsVisit() || info.getIsVisit() == 0)){
			//未回访(是否需要回访状态等于是，并且是否回访是否)
			visitStatus = 4 ;
		}else if(info.getIsNeedVisit() == 1 && info.getIsVisit() == 1){
			//已回访(是否需要回访状态等于是,并且是否回访等是)
			visitStatus = 1 ;
		}
		
		return visitStatus ;
	}
}
