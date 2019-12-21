package com.visionvera.dao.ywcore;

import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.ywcore.RemoteReportVO;

import java.util.List;
import java.util.Map;

/**
 * 抓包机表DAO
 *
 */
public interface RemoteReportDao {
	/**
	 * 根据条件查询抓包机列表
	 * @param remoteReport 查询条件
	 * @return
	 */
	public List<RemoteReportVO> selectRemoteReportByCondition(RemoteReportVO remoteReport);

	/**
	 * 查询已经存在的抓包机所在的真实行政区域ID
	 * @return
	 */
	public List<RemoteReportVO> selectExistGrapMachineRegionList(RemoteReportVO remoteReport);

	/**
	 * 通过ID批量查询行政区域信息
	 * @param regionIdList
	 * @return
	 */
	public List<RegionVO> selectRegionBatch(List<String> regionIdList);

	/**
	 * 查询抓包机的数量
	 * @param remoteReport
	 * @return
	 */
	public Map<String,Long> selectGrapMachineCountByCondition(RemoteReportVO remoteReport);

	/**
	 * 通过行政区域ID查询抓包机已经存在的行政区域，不包含省级行政区域
	 * @param regionId 行政区域ID
	 * @return
	 */
	public List<RegionVO> selectExistRegionByRegionIdWithoutOneLevel(String regionId);

	/**
	 * 通过行政区域ID查询抓包机信息:左侧树信息
	 * @param regionId 行政区域ID
	 * @return
	 */
	public List<RemoteReportVO> selectRemoteReportNodeByRegionIdWithoutBlob(String regionId);

	/**
	 * 根据行政区域ID做Like查询
	 * @param region
	 * @return
	 */
	public List<RegionVO> selectRegionLikeRegionId(RegionVO region);
}
