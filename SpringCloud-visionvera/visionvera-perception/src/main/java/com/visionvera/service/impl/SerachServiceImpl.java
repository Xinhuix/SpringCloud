package com.visionvera.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.datacore.MonitorVedioReport;
import com.visionvera.bean.datacore.PlatformResourceVO;
import com.visionvera.bean.datacore.VphoneReport;
import com.visionvera.bean.restful.DataInfo;
import com.visionvera.bean.restful.ResponseInfo;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.perception.ServersDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.feign.PlatformResService;
import com.visionvera.feign.ServerConfigService;
import com.visionvera.service.MonitorVedioReportService;
import com.visionvera.service.SerachService;
import com.visionvera.service.VphoneReportService;
import com.visionvera.util.ConversionMapper;
import com.visionvera.util.HttpUtils;
import com.visionvera.util.UUIDGenerator;
import com.visionvera.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Administrator
 * @date 2018年11月23日 10:24
 */
@Service
@Transactional(value = "transactionManager_perception", rollbackFor = Exception.class)
public class SerachServiceImpl implements SerachService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerachServiceImpl.class);

    @Autowired
    private ServersDao serversDao;

    /**
     * 批量获取不同区号的服务器信息
     * @param deviceList
     * @return
     */
    @Override
    public List<Map<String, Object>> getZoneServers(List<Map<String, Object>> deviceList){
    	return this.serversDao.getZoneServers( deviceList);
    }

    /**
	 * 通过行政区域ID获取行政区域信息
	 * @param regionId
	 * @return
	 */
    @Override
	public RegionVO getRegionByRegionId(String regionId) {
    	return this.serversDao.selectRegionByRegionId(regionId);
    }
}
