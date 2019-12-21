package com.visionvera.dao.ywcore;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.slweoms.PlatformTypeVO;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.bean.slweoms.ServerHardwareVO;

public interface SlweomsDao {

	/**
	 * 根据行政区域编码查询服务器信息
	 * @param paramMap
	 * @return
	 */
	List<ServerHardwareVO> getRegionServerList(Map<String, Object> paramMap);

	/**
	 * 获取指定服务器的最新硬件数据
	 * @param paramMap
	 * @return
	 */
	List<ServerHardwareVO> getServerHardwareInfo(Map<String, Object> paramMap);
	
	
	/**
	 * 根据行政区域编码和平台类型查询行政区域列表
	 * @param paramMap
	 * @return
	 */
	List<RegionVO> getRegionList(Map<String, Object> paramMap);
	
	/**
	 * 查询拥有服务器的省份
	 * @param paramMap
	 * @return
	 */
	List<RegionVO> getServerProvinceList(Map<String, Object> paramMap);
	
	/**
	 * 查询平台类型列表
	 * @return
	 */
	List<PlatformTypeVO> getPlatFormTypeList();
	
	/**
	 * 查询该区域包含的服务器基本信息列表
	 * @param paramMap
	 * @return
	 */
	List<ServerBasics> getServerBasicsListOfRegion(Map<String, Object> paramMap);
	
	/**
	 * 根据服务器唯一标识查询平台列表
	 * @param serverKey
	 * @return
	 */
	List<PlatformVO> getPlatFormVOListByServerKey(String serverKey);
	
	/**
	 * 根据服务器id查询服务器基础信息
	 * @param id 服务器id
	 * @return
	 */
	ServerBasics getServerBasicsById(@Param("id")Integer id);
	
	/**
	 * 根据区域id,查询区域
	 * @param districtCode
	 * @return
	 */
	RegionVO getRegionVOById(String districtCode);
	
	/**
	 * 查询行政区域下的服务器在线/总数
	 * @param paramMap
	 * @return
	 */
	List<Map<String, Object>> getRegionServerCount(Map<String, Object> paramMap);
	
	/**
	 * 查询区域下服务器在线数量
	 * @param paramMap
	 * @return
	 */
	List<Map<String, Object>> getRegionServerOnlineCount(Map<String, Object> paramMap);
	
	/**
	 * 查询全国服务器的在线/总数
	 * @return
	 */
	List<Map<String, Object>> getAllServerCount();
	
	/**
	 * 根据平台查询服务器
	 * @param registerid
	 * @return
	 */
	ServerBasics getServerBasicsByRegisterid(String registerid);
	
	/**
	 * 根据平台标识查询平台
	 * @param registerid
	 * @return
	 */
	PlatformVO getPlatformVoByRegisterid(String registerid);
	
	/**
	 * 修改平台
	 * @param platformVo
	 */
	void updatePlatform(PlatformVO platformVo);
	
	/**
	 * 根据行政区域名称获取行政区域对象
	 * @param districtName
	 * @return
	 */
	RegionVO getRegionVOByName(@Param("districtName")String districtName,@Param("pid")String pid);

}
