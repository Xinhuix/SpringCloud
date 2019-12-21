package com.visionvera.dao.ywcore;

import com.visionvera.bean.slweoms.PlatformTypeVO;

import java.util.List;

/**
 * 平台类型Dao
 * @author dql
 *
 */
public interface PlatformTypeDao {

	List<PlatformTypeVO> getAllPlatformType();

	/**
	 * 根据平台类型id查询平台信息
	 * @param tposPlatformType
	 * @return
	 */
	PlatformTypeVO getPlatformTypeByTypeId(Integer tposPlatformType);
}
