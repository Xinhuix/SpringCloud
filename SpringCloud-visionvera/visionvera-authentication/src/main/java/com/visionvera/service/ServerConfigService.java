package com.visionvera.service;

import com.visionvera.basecrud.BaseService;
import com.visionvera.bean.datacore.ServerConfig;

import java.util.List;

/**
 * <p>
 *  服务接口
 * </p>
 *
 * @author zhanghui
 * @since 2018-10-24
 */
public interface ServerConfigService extends BaseService<ServerConfig> {
	/**
	 * 获取同步用户的URL
	 * @param otherPlatformId 其他平台的平台ID 
	 * @param platformType 平台类别。5：流媒体；6：会管
	 * @return
	 */
	String getSyncUrl(String otherPlatformId, String platformType);
	
	/**
	 * 获取服务器的URL
	 * @param otherPlatformId 平台ID
	 * @return http://ip:port 注意最后没有/
	 */
	String getIpPortUrl(String otherPlatformId);
	/**
	 * 查询流媒体配置列表
	 * @return
	 */
	List<ServerConfig> queryLmtConfigList();

	/**
	 * 通过平台ID获取平台信息
	 * @param otherPlatformId 平台ID
	 * @return 配置信息。如果没有返回null
	 */
	ServerConfig getServerConfigByOtherPlatformId(String otherPlatformId);

	/**
	 * 获取配置信息列表
	 * @param serverConfig 查询条件
	 * @return
	 */
	List<ServerConfig> getServerConfigList(ServerConfig serverConfig);

	/**
	 * 根据平台类别批量获取配置信息
	 * @param platformTypeArr 平台类别的数组
	 * @return
	 */
	List<ServerConfig> getServerConfigByPlatformType(String[] platformTypeArr);

	/**
	 * 通过主键ID获取服务配置信息
	 * @param serverConfigId 服务配置的主键ID
	 * @return 服务配置信息。如果没有返回null
	 */
	ServerConfig getServerConfigById(String serverConfigId);
}
