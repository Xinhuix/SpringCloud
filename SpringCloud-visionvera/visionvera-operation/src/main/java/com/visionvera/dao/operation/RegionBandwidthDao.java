package com.visionvera.dao.operation;

import java.util.List;

import com.visionvera.bean.cms.BandWidthVO;

/**
 * 操作国干带宽表
 *
 */
public interface RegionBandwidthDao {
	/**
	 * 通过条件查询国干带宽的信息
	 * @param bandWidth 查询条件
	 * @return
	 */
	public List<BandWidthVO> selectBandWidthByCondition(BandWidthVO bandWidth);
	
	/**
	 * 批量更新国干带宽
	 * @param bandWidth
	 * @return
	 */
	public int updateBandwidthBatch(List<BandWidthVO> bandWidthList);
}
