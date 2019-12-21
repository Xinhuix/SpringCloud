package com.visionvera.service;

import com.visionvera.bean.slweoms.PlatformTypeVO;

import java.util.List;

/**
 * 平台类型Service
 * @author dql
 *
 */
public interface PlatformTypeService {

	List<PlatformTypeVO> getAllPlatformType();

    /**
     * 根据平台Id获取平台类型
     * @param tposPlatformType
     * @return
     */
    PlatformTypeVO getPlatformTypeByTypeId(Integer tposPlatformType);
}
