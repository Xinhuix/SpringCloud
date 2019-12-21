package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.ServerConfig;

/**
 * 静态资源服务层
 *
 */
public interface StaticResourcesService {
	/**
	 * 获取服务配置列表
	 * @param serverConfig 查询条件
	 * @param pageNum 页码
	 * @param pageSize 页大小，为-1表示不分页
	 * @return
	 */
	PageInfo<ServerConfig> getServerConfigList(ServerConfig serverConfig, Integer pageNum, Integer pageSize);

	/**
	 * 视联汇获取服务列表，获取到服务列表以后下发该服务的token
	 * @param platformType 平台类别。8表示网管，9表示会易通，10表示一机一档
	 * @param token 访问标识
	 * @return
	 */
	ReturnData getServerListForAPP(String platformType, String token);

}
