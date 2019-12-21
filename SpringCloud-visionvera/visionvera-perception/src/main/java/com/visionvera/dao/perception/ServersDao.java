package com.visionvera.dao.perception;

import com.visionvera.bean.cms.RegionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 查询不同区号的服务器信息
 * @author xiechengsuan
 *
 */
public interface ServersDao {
	/**
	 * 查询不同区号的服务器信息
	 * @param zoneIds 查询条件
	 * @return
	 */
	public List<Map<String, Object>> getZoneServers(@Param("deviceList")List<Map<String, Object>> deviceList);

	/**
	 * 通过行政区域ID查询行政区域
	 * @param regionId 行政区域ID
	 * @return
	 */
	public RegionVO selectRegionByRegionId(String regionId);

    Integer selectYwTreeByUUid(String serverUnique);
}
