package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.slweoms.PlatformTypeVO;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.bean.slweoms.ServerHardwareVO;

public interface SlweomsService {
	
	/**
	 * 查询服务器信息
	 * @param paramMap
	 * @return 
	 */
	List<ServerHardwareVO> getRegionServerList(Map<String, Object> paramMap);
	
	/**
	 * 查询服务器的最新硬件数据
	 * @param paramMap
	 * @return
	 */
	List<ServerHardwareVO> getServerHardwareInfo(Map<String, Object> paramMap);
	
	/**
	 * 根据条件查询行政区域和服务器列表
	 * @param paramMap 参数 ：行政区域级别，行政区域编码，平台类型
	 * @return
	 */
	Map<String, Object> getRegionAndServerList(Map<String, Object> paramMap);
	
	/**
	 * 获得拥有服务器的省级行政区域
	 * @param paramMap 
	 * @return
	 */
	List<RegionVO> getServerProvinceList(Map<String, Object> paramMap);
	
	/**
	 * 查询平台类别列表
	 * @return
	 */
	List<PlatformTypeVO> getPlatFormTypeList();
	
	/**
	 * 根据服务器ID查询服务器基础信息
	 * @param id
	 * @return
	 */
	ServerBasics getServerBasicsById(Integer id);
	
	/**
	 * 获取所有服务器的在线/总数
	 * @return
	 */
	Map<String, Object> getAllServerCount();
	
	/**
	 * 根据平台查询所属服务器
	 * @param registerid
	 * @return
	 */
	ServerBasics getServerBasicsByRegisterid(String registerid);
	
	/**
	 * 根据平台唯一标识查询平台
	 * @param registerid
	 * @return
	 */
	PlatformVO getPlatformByRegisterid(String registerid);
	
	/**
	 * 更新平台进程状态
	 * @param platformVo
	 */
	void updatePlatform(PlatformVO platformVo);
	
	/**
	 * 操作平台进程
	 * @param registerid 平台唯一标识
	 * @param method
	 * @return
	 */
	Map<String, Object> handleProcess(String registerid, String method);
	
	
	
}
