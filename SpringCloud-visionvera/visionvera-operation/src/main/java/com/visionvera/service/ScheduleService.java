package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.ScheduleVO;

public interface ScheduleService {
	/**
	 * 
	 * @Title: getScheduleList 
	 * @Description: TODO 获取预约列表 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return List<ScheduleBean>    返回类型 
	 * @throws
	 */
	PageInfo<ScheduleVO> getScheduleList(Map<String, Object> map);
	
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
	 * @param rowBounds 
	 * 
	 * @Title: getAccessorDetail 
	 * @Description: TODO 获取预约列表
	 * @param @param map 查询条件集合
	 * @param @return  参数说明 
	 * @return List<ScheduleBean>    返回类型 
	 * @throws
	 */
	PageInfo<ScheduleVO> getAccessorDetail(Map<String, Object> map);
	
	/**
	 * 获取数据所属平台列表
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return
	 */
	PageInfo<RegionVO> getScheduleServers(boolean isPage, Integer pageNum, Integer pageSize);
	
	/**
	 * 删除预约会议
	 * @param uuids 预约会议ID，多个ID使用英文逗号","分隔
	 * @return
	 */
	ReturnData delSchedule(String uuid);
	
	/**
	 * 判断是否可以删除预约会议
	 * @param uuids 预约会议ID，多个ID使用英文逗号","分隔
	 * @return
	 */
	int isOK2Delete(String uuid);
	
	/****
	 * 根据日期查询预约会议列表
	 * @param map
	 * @return
	 */
	List<ScheduleFormVO> getScheduleListByDate(Map<String, Object> paramsMap);
	
	/**
	 * 获取会议详情（含通知）
	 * @author xiechs
	 * @date 2016年11月23日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<ScheduleVO> getSchedule(Map<String, Object> paramsMap);
	
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


}
