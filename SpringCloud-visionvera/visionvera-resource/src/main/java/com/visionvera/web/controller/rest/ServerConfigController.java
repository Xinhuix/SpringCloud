package com.visionvera.web.controller.rest;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.ServerConfig;
import com.visionvera.common.api.resource.ServerConfigAPI;
import com.visionvera.constrant.PlatformTypeConstrant;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.StaticResourcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ServerConfigController extends BaseReturn implements ServerConfigAPI {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigController.class);
	
	@Autowired
	private StaticResourcesService staticResourcesService;
	
	/**
	 * 获取服务配置列表
	 * @param serverConfig 查询条件
	 * @param pageNum 页码
	 * @param pageSize 页大小。为-1时表示不分页，查询所有
	 * @return
	 */
	@RequestMapping(value = "/getServerConfigList", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData getServerConfigList(@RequestBody(required = false) ServerConfig serverConfig,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize) {
		Map<String, Object> extraMap = new HashMap<String, Object>();
		try {
			PageInfo<ServerConfig> serverConfigInfo = this.staticResourcesService.getServerConfigList(serverConfig, pageNum, pageSize);
			extraMap.put("totalPage", serverConfigInfo.getPages());//总页数
			extraMap.put("pageSize", serverConfigInfo.getPageSize());//页大小
			extraMap.put("pageNum", serverConfigInfo.getPageNum());//页码
			return super.returnResult(0, "查询成功", null, serverConfigInfo.getList(), extraMap);
		} catch (BusinessException e) {
			LOGGER.error("ServerConfigController ===== getServerConfigList ===== 获取服务配置失败 ===== ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			LOGGER.error("ServerConfigController ===== getServerConfigList ===== 获取服务配置失败 ===== ", e);
			return super.returnError("获取服务配置失败");
		}
	}


	/**
	 * 视联汇获取服务列表，获取到服务列表以后下发该服务的token
	 * @param platformType 平台类别。8表示网管，9表示会易通，10表示一机一档
	 * @param token 访问标识
	 * @return
	 */
	@RequestMapping(value = "/platformType/{platformType}/getServerList")
	public ReturnData getServerList(@PathVariable("platformType") String platformType,
									@RequestParam("access_token") String token) {
		try {
			if (!PlatformTypeConstrant.NETWORK_MANAGER_PLATFORM_TYPE.equals(platformType) &&
					PlatformTypeConstrant.HUIYITONG_PLATFORM_TYPE.equals(platformType) &&
					PlatformTypeConstrant.VSDC_PLATFORM_TYPE.equals(platformType)) {
				return super.returnError("平台类别错误");
			}
			return this.staticResourcesService.getServerListForAPP(platformType, token);
		} catch (BusinessException e) {
			LOGGER.error("视联汇获取服务列表失败, 失败原因: ", e.getMessage(), e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			LOGGER.error("视联汇获取服务列表失败", e);
			return super.returnError("获取服务列表失败");
		}
	}
}
