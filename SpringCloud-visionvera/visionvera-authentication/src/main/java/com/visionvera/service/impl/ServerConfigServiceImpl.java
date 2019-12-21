package com.visionvera.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.basecrud.CrudService;
import com.visionvera.bean.datacore.ServerConfig;
import com.visionvera.config.SysConfig;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.authentication.ServerConfigDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.ServerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2018-10-24
 */
@Service("serverConfigService")
@Transactional(transactionManager = "transactionManager_authentication", rollbackFor = Exception.class)
public class ServerConfigServiceImpl extends CrudService<ServerConfigDao, ServerConfig> implements ServerConfigService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigServiceImpl.class);

	@Autowired
	private ServerConfigDao serverConfigDao;
	
	@Autowired
	SysConfig sysConfig;
	/**
	 * 获取同步用户的URL
	 * @param otherPlatformId 其他平台的平台ID 
	 * @param platformType 平台类别。5：流媒体；6：会管
	 * @return
	 */
	public String getSyncUrl(String otherPlatformId, String platformType) {
		String url = "";
		
		if ("6".equals(platformType)) {//6表示会管平台类别
			String urlPrefix = this.getIpPortUrl(otherPlatformId);
			url = urlPrefix + WsConstants.URL_GET_SAME_PHONE_USER_HUIGUAN;//会管同步用户的URL
		}
		
		return url;
	}
	
	/**
	 * 获取服务器的URL
	 * @param otherPlatformId 平台ID
	 * @return http://ip:port 注意最后没有/
	 */
	@Override
	public String getIpPortUrl(String otherPlatformId) {
		ServerConfig serverConfigParams = new ServerConfig();
		serverConfigParams.setOtherPlatformId(otherPlatformId);
		ServerConfig serverConfig = this.serverConfigDao.get(serverConfigParams);
		
		if (serverConfig == null) {
			throw new BusinessException("请先配置平台信息");
		}
		
		String serverUrl = String.format(sysConfig.getProtocal() + WsConstants.URL_SERVER, serverConfig.getIp(), serverConfig.getPort());
		
		return serverUrl;
	}

	@Override
	public List<ServerConfig> queryLmtConfigList() {
		return serverConfigDao.queryLmtConfigList();
	}

	/**
	 * 通过平台ID获取平台信息
	 * @param otherPlatformId 平台ID
	 * @return 配置信息。如果没有返回null
	 */
	@Override
	public ServerConfig getServerConfigByOtherPlatformId(String otherPlatformId) {
		ServerConfig serverConfig = this.serverConfigDao.selectServerConfigByOtherPlatformId(otherPlatformId);
		if (serverConfig == null) {
			LOGGER.warn("没有找到该平台的配置信息, 要查找的平台是: {}", otherPlatformId);
		}
		return serverConfig;
	}

	/**
	 * 获取配置信息列表
	 * @param serverConfig 查询条件
	 * @return
	 */
	@Override
	public List<ServerConfig> getServerConfigList(ServerConfig serverConfig) {
		List<ServerConfig> serverConfigList = this.serverConfigDao.selectServerConfigByCondition(serverConfig);
		if (serverConfigList == null || serverConfigList.size() == 0) {
			LOGGER.warn("没有该平台的配置信息, 要找的平台是: {}", JSONObject.toJSONString(serverConfig));
		}
		return serverConfigList;
	}

	/**
	 * 根据平台类别批量获取配置信息
	 * @param platformTypeArr 平台类别的数组
	 * @return
	 */
	@Override
	public List<ServerConfig> getServerConfigByPlatformType(String[] platformTypeArr) {
		List<ServerConfig> serverConfigList = this.serverConfigDao.selectServerConfigByPlatformType(platformTypeArr);
		if (serverConfigList == null || serverConfigList.size() == 0) {
			LOGGER.warn("没有该平台的配置信息, 要找的平台是: {}", JSONObject.toJSONString(platformTypeArr));
		}
		return serverConfigList;
	}

	/**
	 * 通过主键ID获取服务配置信息
	 * @param serverConfigId 服务配置的主键ID
	 * @return 服务配置信息。如果没有返回null
	 */
	@Override
	public ServerConfig getServerConfigById(String serverConfigId) {
		ServerConfig serverConfig = this.serverConfigDao.selectServerConfigById(serverConfigId);
		if (serverConfig == null) {
			LOGGER.warn("没有该服务配置信息, 要查找的服务配置信息的ID是: {}", serverConfigId);
		}
		return serverConfig;
	}
}
