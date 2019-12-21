package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.Page;
import com.visionvera.bean.operation.BreakdownHistory;
import com.visionvera.bean.operation.BreakdownInfo;
import com.visionvera.bean.operation.BreakdownMeeting;
import com.visionvera.common.api.operation.BreakDownInfoAPI;
import com.visionvera.service.BreakdownHistoryService;
import com.visionvera.service.BreakdownInfoService;
import com.visionvera.service.BreakdownMeetingService;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
@RestController
public class BreakdownInfoController implements BreakDownInfoAPI{

	private static final Logger logger = LogManager.getLogger(BreakdownInfoController.class);
	@Resource
	private BreakdownMeetingService breakdownMeetingService ;
	@Resource
	private BreakdownInfoService breakdownInfoService ;
	@Resource
	private BreakdownHistoryService breakdownHistoryService ;
	/**
	 * 获取故障会议信息列表
	 * @date 2018年7月11日 下午2:35:38
	 * @author wangqiubao
	 * @param name 会议名称
	 * @param level  会议等级(1-5)
	 * @param compere  主席名称
	 * @param type   即时会议(1:否，2:是)
	 * @param isMaintainBreakdown 是否维护故障(0-否；1-是；)
	 * @param startStartMeetingTime 开始会议的开始时间
	 * @param endStartMeetingTime 开始会议的结束时间
	 * @param visitStatus 回访状态(0-无；1-已回访；2-部分回访；3-已超时；4-未回访)
	 * @param solveStatus 解决状态(0-未解决；1-已经解决)
	 * @param masterNo 主席名称
	 * @param pageNum 页码（从1开始，默认1）
	 * @param pageSize 页大小（默认15）
	 * @return
	 */
	@RequestMapping(value="getBreakDownMeetingList",method=RequestMethod.GET)
	public Map<String, Object> getBreakDownMeetingList(
			//@RequestBody(required=false) BreakdownMeeting bdMeeting,
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="level",required=false) String level,
			@RequestParam(value="compere",required=false) String compere,
			@RequestParam(value="type",required=false) String type,
			@RequestParam(value="isMaintainBreakdown",required=false) String isMaintainBreakdown,
			@RequestParam(value="startStartMeetingTime",required=false) String startStartMeetingTime,
			@RequestParam(value="endStartMeetingTime",required=false) String endStartMeetingTime,
			@RequestParam(value="visitStatus",required=false) String visitStatus,
			@RequestParam(value="solveStatus",required=false) String solveStatus,
			@RequestParam(value="masterNo",required=false) String masterNo,
			@RequestParam(value="pageNum",defaultValue="1",required=false) Integer pageNum,
			@RequestParam(value="pageSize",defaultValue="15",required=false) Integer pageSize){
		Map<String,Object> paramMap = new HashMap<String, Object>() ;
		Map<String,Object> resultMap = new HashMap<String, Object>() ;
		paramMap.put("pageNum", pageNum) ;
		paramMap.put("pageSize", pageSize) ;
		if(StringUtils.isNotBlank(name)) 
			paramMap.put("name", name) ;
		if(StringUtils.isNotBlank(level)) 
			paramMap.put("level", level) ;
		if(StringUtils.isNotBlank(compere)) 
			paramMap.put("compere", compere) ;
		if(StringUtils.isNotBlank(type)) 
			paramMap.put("type", type) ;
		if(StringUtils.isNotBlank(isMaintainBreakdown)) 
			paramMap.put("isMaintainBreakdown", isMaintainBreakdown) ;
		if(StringUtils.isNotBlank(startStartMeetingTime)) 
			paramMap.put("startStartMeetingTime", startStartMeetingTime) ;
		if(StringUtils.isNotBlank(endStartMeetingTime))
			paramMap.put("endStartMeetingTime", endStartMeetingTime) ;
		if(StringUtils.isNotBlank(visitStatus)) 
			paramMap.put("visitStatus", visitStatus) ;
		if(StringUtils.isNotBlank(solveStatus)) 
			paramMap.put("solveStatus", solveStatus) ;
		if(StringUtils.isNotBlank(masterNo))
			paramMap.put("masterNo", masterNo) ;
		try {
			List<BreakdownMeeting> meetingList = breakdownMeetingService.getBreakdownMeetingList(paramMap) ;
			if(meetingList != null && meetingList.size() > 0) {
				Map<String,Object> dataMap = new HashMap<String,Object>();
				Page<BreakdownMeeting> page = (Page<BreakdownMeeting>)meetingList;
				HashMap<String,Object> pageMap = new HashMap<String,Object>();
				pageMap.put("pageNum", page.getPageNum());
				pageMap.put("pageSize", page.getPageSize());
				pageMap.put("total", page.getTotal());
				pageMap.put("pages", page.getPages());
				dataMap.put("extra",pageMap);
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询故障会议信息列表成功");
				dataMap.put("items", meetingList);
				resultMap.put("data", dataMap);
			} else {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "查询故障会议信息列表为空");
			}
		} catch (Exception e) {
			logger.error("查询故障会议信息列表异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询故障会议信息列表异常");
			return resultMap;
		}
		return resultMap ;
	}
	/**
	 * 添加故障信息
	 * @date 2018年7月7日 下午3:46:10
	 * @author wangqiubao
	 * @param breakdownInfo
	 * @return
	 */
	@RequestMapping(value="addBreakdownInfo",method=RequestMethod.POST)
	public Map<String,Object> addBreakdownInfo(@RequestBody BreakdownInfo breakdownInfo){
		Map<String,Object> resultMap = new HashMap<String, Object>() ;
		resultMap.put("errcode", 1);
		if (null == breakdownInfo) {
			resultMap.put("errmsg", "添加故障信息参数不能为空");
			return resultMap;
		}
		if (StringUtils.isBlank(breakdownInfo.getMeetingId())) {
			resultMap.put("errmsg", "会议Id不能为空");
			return resultMap;
		}
		try {
			breakdownInfoService.addBreakdownInfo(breakdownInfo);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "添加故障信息成功");
		} catch (Exception e) {
			logger.error("添加故障信息异常", e);
			resultMap.put("errmsg", "添加故障信息异常");
			return resultMap;
		}
		return resultMap ;
	}
	/**
	 * 修改故障信息
	 * @date 2018年7月11日 上午10:30:10
	 * @author wangqiubao
	 * @param breakdownInfo (id不能为空)
	 * @return
	 */
	@RequestMapping(value="updateBreakdownInfo",method=RequestMethod.POST)
	public Map<String,Object> updateBreakdownInfo(@RequestBody BreakdownInfo breakdownInfo){
		Map<String,Object> resultMap = new HashMap<String, Object>() ;
		resultMap.put("errcode", 1);
		if (null == breakdownInfo || StringUtils.isBlank(breakdownInfo.getId())) {
			resultMap.put("errmsg", "修改故障信息参数Id为空");
			return resultMap;
		}
		try {
			breakdownInfoService.updateBreakdownInfo(breakdownInfo);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "修改故障信息成功");
		} catch (Exception e) {
			logger.error("添加故障信息异常", e);
			resultMap.put("errmsg", "修改故障信息异常");
			return resultMap;
		}
		return resultMap ;
	}
	/**
	 * 删除故障信息
	 * @date 2018年7月10日 上午10:05:52
	 * @author wangqiubao
	 * @param breakdownId 故障ID
	 * @param meetingId 会议ID
	 * @return
	 */
	@RequestMapping(value="deleteBreakdownInfo",method=RequestMethod.GET)
	public Map<String,Object> deleteBreakdownInfo(
			@RequestParam(value="breakdownId",required=false) String breakdownId,
			@RequestParam(value="meetingId",required=false) String meetingId
			){
		Map<String,Object> resultMap = new HashMap<String, Object>() ;
		resultMap.put("errcode", 1);
		if (StringUtils.isBlank(breakdownId)) {
			resultMap.put("errmsg", "删除故障信息ID不能为空");
			return resultMap;
		}
		if (StringUtils.isBlank(meetingId)) {
			resultMap.put("errmsg", "会议ID不能为空");
			return resultMap;
		}
		try {
			BreakdownInfo breakdownInfo = new BreakdownInfo() ;
			breakdownInfo.setId(breakdownId);
			breakdownInfo.setMeetingId(meetingId);
			breakdownInfoService.deleteBreakdownInfo(breakdownInfo);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "删除故障信息成功");
		} catch (Exception e) {
			logger.error("删除故障信息异常", e);
			resultMap.put("errmsg", "删除故障信息异常");
			return resultMap;
		}
		return resultMap ;
	}
	/**
	 * 获取故障信息列表
	 * @date 2018年7月11日 下午3:47:47
	 * @author wangqiubao
	 * @param meetingId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value="getBreakdownInfoList",method=RequestMethod.GET)
	public Map<String,Object> getBreakdownInfoList(
			//@RequestBody BreakdownInfo breakdownInfo,
			@RequestParam(value="meetingId",required=false) String meetingId,
			@RequestParam(value="pageNum",defaultValue="1") Integer pageNum,
			@RequestParam(value="pageSize",defaultValue="15") Integer pageSize){
		Map<String,Object> resultMap = new HashMap<String, Object>() ;
		HashMap<String, Object> paramsMap = new HashMap<String,Object>() ;
		resultMap.put("errcode", 1);
		if(StringUtils.isBlank(meetingId)){
			resultMap.put("errmsg", "获取故障信息列表参数为空");
			return resultMap;
		}
		paramsMap.put("pageNum", pageNum) ;
		paramsMap.put("pageSize", pageSize) ;
		logger.debug("getBreakdownInfoList|params|meetingId="+meetingId);
		try {
			BreakdownInfo breakdownInfo = new BreakdownInfo() ;
			breakdownInfo.setMeetingId(meetingId);
			List<BreakdownInfo> infoList = breakdownInfoService.getBreakdownInfoList(breakdownInfo, paramsMap) ;
			if(infoList != null && infoList.size() > 0) {
				Map<String,Object> dataMap = new HashMap<String,Object>();
				if (null!=pageSize && pageSize!=-1) {
					Page<BreakdownInfo> page = (Page<BreakdownInfo>)infoList;
					HashMap<String,Object> pageMap = new HashMap<String,Object>();
					pageMap.put("pageNum", page.getPageNum());
					pageMap.put("pageSize", page.getPageSize());
					pageMap.put("total", page.getTotal());
					pageMap.put("pages", page.getPages());
					dataMap.put("extra",pageMap);
				}
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询故障信息列表成功");
				dataMap.put("items", infoList);
				resultMap.put("data", dataMap);
			} else {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "查询故障信息列表为空");
			}
		} catch (Exception e) {
			logger.error("查询故障信息列表异常", e);
			resultMap.put("errmsg", "查询故障信息列表异常");
			return resultMap;
		}
		logger.debug("getBreakdownInfoList|result="+resultMap);
		return resultMap ;
	}
	/**
	 * 查询故障信息
	 * @date 2018年7月10日 上午11:01:05
	 * @author wangqiubao
	 * @param breakdownInfo id不能为空
	 * @return
	 */
	@RequestMapping(value="getBreakdownInfo",method=RequestMethod.GET)
	public Map<String,Object> getBreakdownInfo(@RequestParam(value="breakdownId",required=false) String breakdownId){
		HashMap<String, Object> resultMap = new HashMap<String,Object>() ;
		resultMap.put("errcode", 1);
		if(StringUtils.isBlank(breakdownId)){
			resultMap.put("errmsg", "查询故障信息Id为空");
			return resultMap;
		}
		try {
			BreakdownInfo info = new BreakdownInfo() ;
			info.setId(breakdownId);
			info = breakdownInfoService.get(info) ;
			Map<String,Object> dataMap = new HashMap<String,Object>();
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "查询故障信息成功");
			dataMap.put("items", info);
			resultMap.put("data", dataMap);
		} catch (Exception e) {
			logger.error("查询故障信息",e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询故障信息列表异常");
			return resultMap;
		}
		return resultMap ;
	}
	/**
	 * 获取历史故障信息列表
	 * @date 2018年7月10日 上午11:22:27
	 * @author wangqiubao
	 * @param breakdownId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value="getBreakdownHistoryList",method=RequestMethod.GET)
	public Map<String,Object> getBreakdownHistoryList(
			@RequestParam(value="meetingId",required=false) String meetingId,
			@RequestParam(value="pageNum",defaultValue="1") Integer pageNum,
			@RequestParam(value="pageSize",defaultValue="15") Integer pageSize){
		BreakdownHistory history = new BreakdownHistory() ;
		HashMap<String, Object> paramsMap = new HashMap<String,Object>() ;
		paramsMap.put("pageNum", pageNum) ;
		paramsMap.put("pageSize", pageSize) ;
		HashMap<String, Object> resultMap = new HashMap<String,Object>() ;
		resultMap.put("errcode", 1);
		try {
			history.setMeetingId(meetingId);
			List<BreakdownHistory> list = breakdownHistoryService.getBreakdownHistoryList(history,paramsMap) ;
			if(list != null && list.size() > 0) {
				Map<String,Object> dataMap = new HashMap<String,Object>();
				if (null!=pageSize && pageSize!=-1) {
					Page<BreakdownHistory> page = (Page<BreakdownHistory>)list;
					HashMap<String,Object> pageMap = new HashMap<String,Object>();
					pageMap.put("pageNum", page.getPageNum());
					pageMap.put("pageSize", page.getPageSize());
					pageMap.put("total", page.getTotal());
					pageMap.put("pages", page.getPages());
					dataMap.put("extra",pageMap);
				}
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询历史故障信息列表成功");
				dataMap.put("items", list);
				resultMap.put("data", dataMap);
			} else {
				resultMap.put("errmsg", "查询历史故障信息列表为空");
			}
		} catch (Exception e) {
			logger.error("查询历史故障信息列表信息",e);
			resultMap.put("errmsg", "查询历史故障信息列表异常");
			return resultMap;
		}
		return resultMap ;
	}
	/**
	 * 查询故障会议详情
	 * @date 2018年7月10日 下午1:46:53
	 * @author wangqiubao
	 * @param meetingId 会议id
	 * @return
	 */
	@RequestMapping(value="getBreakdownMeetingInfo",method=RequestMethod.GET)
	public Map<String,Object> getBreakdownMeetingInfo(@RequestParam(value="meetingId",required=false) String meetingId){
		HashMap<String, Object> resultMap = new HashMap<String,Object>() ;
		resultMap.put("errcode", 1);
		if(StringUtils.isBlank(meetingId)){
			resultMap.put("errmsg", "查询故障信息Id为空");
			return resultMap;
		}
		Map<String,Object> paramsMap = new HashMap<String, Object>() ;
		paramsMap.put("meetingId", meetingId) ;
		try {
			Map<String,Object> info = breakdownMeetingService.getBreakdownMeetingInfo(paramsMap) ;
			Map<String,Object> dataMap = new HashMap<String,Object>();
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "查询故障会议详情成功");
			dataMap.put("items", info);
			resultMap.put("data", dataMap);
		} catch (Exception e) {
			logger.error("查询故障会议详情出错",e);
			resultMap.put("errmsg", "查询故障会议详情异常");
			return resultMap;
		}
		return resultMap ;
	}
}

