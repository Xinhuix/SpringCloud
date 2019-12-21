package com.visionvera.web.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.TRegionb;
import com.visionvera.constrant.WsConstants;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.RegionService;

@RestController
@RequestMapping(value = "/rest/region")
public class RegionController extends BaseReturn {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegionController.class);

    @Autowired
    private RegionService regionService;

    /**
     * 获取行政区域信息，提供给P-Server(掌上通)业务使用
     * @param region {"id":""} 多个id使用英文逗号","隔开
     * @return
     */
    @RequestMapping(value = "/getRegionForPServer")
    public ReturnData getRegionForPServer(@RequestBody(required = false) TRegionb region) {
        try {
            if (region == null) {
                return super.returnResult(WsConstants.OK, "获取成功", null, null);
            }
            return this.regionService.getRegionForPServer(region);
        } catch (BusinessException e) {
            LOGGER.error("P-Server获取行政区域信息失败, 失败的原因: {}", e.getMessage(), e);
            return super.returnError(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("P-Server获取行政区域信息失败", e);
            return super.returnError("获取失败");
        }
    }
}