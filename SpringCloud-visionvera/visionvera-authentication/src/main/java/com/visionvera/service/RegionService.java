package com.visionvera.service;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.TRegionb;

/**
 * 行政区域相关的业务
 * @author Bianjf
 *
 */
public interface RegionService {
    /**
     * 获取行政区域信息，提供给P-Server(掌上通)业务使用
     * @param region {"id":""} 多个id使用英文逗号","隔开
     * @return
     */
    public ReturnData getRegionForPServer(TRegionb region);
}
